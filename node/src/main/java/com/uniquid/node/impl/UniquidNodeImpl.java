/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.node.impl;

import com.google.common.collect.ImmutableList;
import com.uniquid.node.UniquidCapability;
import com.uniquid.node.exception.NodeException;
import com.uniquid.node.impl.utils.NodeUtils;
import com.uniquid.register.user.UserChannel;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.signers.MissingSigResolutionSigner;
import org.bitcoinj.signers.TransactionSigner;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.RedeemData;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * Implementation of an Uniquid Node with BitcoinJ library
 */
public class UniquidNodeImpl<T extends UniquidNodeConfiguration> extends UniquidWatchingNodeImpl<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeImpl.class.getName());

    private final DeterministicSeed deterministicSeed;

    private final DeterministicHierarchy deterministicHierarchy;

    /**
     * Creates a new instance
     *
     * @param uniquidNodeConfiguration
     * @throws NodeException
     */
    protected UniquidNodeImpl(T uniquidNodeConfiguration, DeterministicSeed deterministicSeed) throws NodeException {

        super(uniquidNodeConfiguration);

        this.deterministicSeed = deterministicSeed;

        DeterministicKey deterministicKey = NodeUtils
                .createDeterministicKeyFromDeterministicSeed(deterministicSeed);

        deterministicHierarchy = new DeterministicHierarchy(deterministicKey);

    }

    /*
     *
     * Begin of public part for implementing UniquidNode
     *
     */

    @Override
    public Transaction createTransaction(final String serializedTx) throws NodeException {
        return uniquidNodeConfiguration.getNetworkParameters().getDefaultSerializer()
                .makeTransaction(Hex.decode(serializedTx));
    }

    @Override
    public synchronized Transaction signTransaction(final Transaction tx, final List<String> paths) throws NodeException {

        try {

            Wallet wallet;

            if (paths.get(0).startsWith("0")) {
                wallet = providerWallet;
            } else if (paths.get(0).startsWith("1")) {
                wallet = userWallet;
            } else {
                throw new NodeException("Unknown paths!");
            }

            UniquidKeyBag keyBag = new UniquidKeyBag();

            for (String path : paths) {

                ImmutableList<ChildNumber> list = NodeUtils.listFromPath(NodeUtils.M_BASE_PATH, path);

                DeterministicKey signingKey = deterministicHierarchy.get(list, true, true);

                keyBag.addDeterministicKey(signingKey);

            }

            int numInputs = tx.getInputs().size();
            for (int i = 0; i < numInputs; i++) {
                TransactionInput txIn = tx.getInput(i);

                // No connected output - try to fetch input tx from proper wallet
                if (txIn.getConnectedOutput() == null) {
                    Transaction inputTransaction = wallet.getTransaction(txIn.getOutpoint().getHash());

                    if (inputTransaction == null) {
                        throw new NodeException("Input TX not found in any wallet!");
                    }

                    TransactionOutput outputToUse = inputTransaction.getOutput(txIn.getOutpoint().getIndex());
                    tx.getInput(i).connect(outputToUse);

                    Script scriptPubKey = txIn.getConnectedOutput().getScriptPubKey();
                    RedeemData redeemData = txIn.getConnectedRedeemData(keyBag);
                    txIn.setScriptSig(scriptPubKey.createEmptyInputScript(redeemData.keys.get(0), redeemData.redeemScript));

                    // Since we are not sure that singed transaction confirmed we have to mark output as unspent,
                    // to use in next attempt to sign transaction
                    outputToUse.markAsUnspent();
                } else {

                    Script scriptPubKey = txIn.getConnectedOutput().getScriptPubKey();
                    RedeemData redeemData = txIn.getConnectedRedeemData(keyBag);
                    txIn.setScriptSig(scriptPubKey.createEmptyInputScript(redeemData.keys.get(0), redeemData.redeemScript));
                }
            }

            TransactionSigner.ProposedTransaction proposal = new TransactionSigner.ProposedTransaction(tx);
            for (TransactionSigner signer : wallet.getTransactionSigners()) {
                if (!signer.signInputs(proposal, keyBag)) {
                    LOGGER.info("{} returned false for the tx", signer.getClass().getName());
                    throw new NodeException("Cannot sign TX!");
                }
            }

            // resolve missing sigs if any
            new MissingSigResolutionSigner(Wallet.MissingSigsMode.THROW).signInputs(proposal, keyBag);

            // commit tx in wallet!
            // This is not necessary! Kept here to remember
//			wallet.commitTx(originalTransaction);

            return tx;

        } catch (Exception ex) {

            throw new NodeException("Exception while signing", ex);

        }
    }

    @Override
    public synchronized void recoverUnspent(final Transaction tx, final List<String> paths) throws NodeException {

        try {

            Wallet wallet;

            if (paths.get(0).startsWith("0")) {
                wallet = providerWallet;
            } else if (paths.get(0).startsWith("1")) {
                wallet = userWallet;
            } else {
                throw new NodeException("Unknown paths!");
            }

            int numInputs = tx.getInputs().size();
            for (int i = 0; i < numInputs; i++) {
                TransactionInput txIn = tx.getInput(i);

                // Fetch input tx from proper wallet
                Transaction inputTransaction = wallet.getTransaction(txIn.getOutpoint().getHash());

                if (inputTransaction == null) {

                    throw new NodeException("Input TX not found in any wallet!");

                }

                TransactionOutput outputToUse = inputTransaction.getOutput(txIn.getOutpoint().getIndex());

                outputToUse.markAsUnspent();

            }

        } catch (Exception ex) {

            throw new NodeException("Exception while recovering", ex);

        }
    }

    public DeterministicSeed getDeterministicSeed() {
        return deterministicSeed;
    }

    @Override
    public String signMessage(String message, String path) throws NodeException {

        ImmutableList<ChildNumber> list = NodeUtils.listFromPath(NodeUtils.M_BASE_PATH, path);

        DeterministicKey signingKey = deterministicHierarchy.get(list, true, true);

        return signingKey.signMessage(message);

    }

    @Override
    public UniquidCapability createCapability(String providerName, String userPublicKey, byte[] rights,
                                              long since, long until) throws NodeException {

        LOGGER.info("Creating capability");

        try {
            // Retrieve contract
            UserChannel userChannel = uniquidNodeConfiguration.getRegisterFactory().getUserRegister().getChannelByName(providerName);

            if (userChannel == null) {
                throw new NodeException("Channel not found!");
            }

            // Should verify that 'owner bit' (29) is set to one
            String bitmask = userChannel.getBitmask();

            // decode
            byte[] b = Hex.decode(bitmask);

            // first byte at 0 means original contract with bitmask
            BitSet bitset = BitSet.valueOf(Arrays.copyOfRange(b, 1, b.length));

            if (!bitset.get(29)) {

                throw new Exception("User not authorized to issue capabilites!");

            }

            // Extract path
            String path = userChannel.getPath();

            UniquidCapability capability = new UniquidCapability.UniquidCapabilityBuilder()
                    .setResourceID(userChannel.getProviderAddress())
                    .setAssigner(userChannel.getUserAddress())
                    .setAssignee(userPublicKey)
                    .setRights(rights)
                    .setSince(since)
                    .setUntil(until)
                    .build();

            String signature = signMessage(capability.prepareToSign(), path);

            capability.setAssignerSignature(signature);

            LOGGER.info("Capability created correctly {}", capability);

            return capability;

        } catch (Exception ex) {

            throw new NodeException("Exception while creating capability", ex);

        }

    }

    @Override
    public String getAddressAtPath(String path) throws NodeException {

        ImmutableList<ChildNumber> list = NodeUtils.listFromPath(NodeUtils.M_BASE_PATH, path);

        DeterministicKey signingKey = deterministicHierarchy.get(list, true, true);

        return LegacyAddress.fromKey(uniquidNodeConfiguration.getNetworkParameters(), signingKey).toBase58();


    }

    /**
     * Builder for UniquidNodeImpl
     */
    public static class UniquidNodeBuilder<B extends UniquidNodeBuilder<B, T, C>, T extends UniquidNodeConfiguration, C extends UniquidNodeConfigurationImpl> extends UniquidWatchingNodeImpl.WatchingNodeBuilder<B, T, C> {

        /**
         * Build a new instance
         *
         * @return a new {@link UniquidNodeImpl}
         * @throws NodeException in case a problem occurs
         */
        public UniquidNodeImpl<T> build() throws NodeException {

            SecureRandom random = new SecureRandom();
            byte[] entropy = new byte[32];
            random.nextBytes(entropy);
            long creationTime = System.currentTimeMillis() / 1000;

            DeterministicSeed detSeed = new DeterministicSeed(entropy, "", creationTime);

            _uniquidNodeConfiguration.setCreationTime(creationTime);
            _uniquidNodeConfiguration.setPublicKey(deriveXpub(_uniquidNodeConfiguration.getNetworkParameters(), detSeed));

            return createUniquidNode(_uniquidNodeConfiguration, detSeed);

        }

        @Deprecated
        public UniquidNodeImpl<T> buildFromHexSeed(final String hexSeed, final long creationTime) throws NodeException {

            try {

                DeterministicSeed detSeed = new DeterministicSeed("", org.bitcoinj.core.Utils.HEX.decode(hexSeed), "", creationTime);

                _uniquidNodeConfiguration.setCreationTime(creationTime);
                _uniquidNodeConfiguration.setPublicKey(deriveXpub(_uniquidNodeConfiguration.getNetworkParameters(), detSeed));

                return createUniquidNode(_uniquidNodeConfiguration, detSeed);

            } catch (Exception ex) {

                throw new NodeException("Exception while building node from hex seed", ex);

            }

        }

        public UniquidNodeImpl<T> buildFromMnemonic(final String mnemonic, final long creationTime) throws NodeException {

            try {
                DeterministicSeed detSeed = new DeterministicSeed(mnemonic, null, "", creationTime);

                _uniquidNodeConfiguration.setCreationTime(creationTime);
                _uniquidNodeConfiguration.setPublicKey(deriveXpub(_uniquidNodeConfiguration.getNetworkParameters(), detSeed));

                return createUniquidNode(_uniquidNodeConfiguration, detSeed);

            } catch (Exception ex) {

                throw new NodeException("Exception while building node from mnemonic", ex);

            }

        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        protected UniquidNodeImpl<T> createUniquidNode(C uniquidNodeConfiguration, DeterministicSeed deterministicSeed) throws NodeException {
            return new UniquidNodeImpl(uniquidNodeConfiguration, deterministicSeed);
        }

        private static final String deriveXpub(NetworkParameters networkParameters, DeterministicSeed detSeed) {

            DeterministicKey deterministicKey = NodeUtils.createDeterministicKeyFromDeterministicSeed(detSeed);

            DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(deterministicKey);

            ImmutableList<ChildNumber> IMPRINTING_PATH = ImmutableList.of(new ChildNumber(44, true),
                    new ChildNumber(0, true));

            DeterministicKey imprintingKey = deterministicHierarchy.get(IMPRINTING_PATH, true, true);

            return imprintingKey.serializePubB58(networkParameters);

        }

    }

}