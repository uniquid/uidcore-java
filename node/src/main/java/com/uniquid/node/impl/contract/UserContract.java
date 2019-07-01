/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.node.impl.contract;

import com.uniquid.node.impl.UniquidNodeStateContext;
import com.uniquid.node.impl.utils.WalletUtils;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;
import com.uniquid.registry.RegistryDAO;
import com.uniquid.registry.exception.RegistryException;
import com.uniquid.registry.impl.RegistryDAOImpl;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptPattern;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.uniquid.node.impl.utils.NodeUtils.getAddressFromTransactionOutput;

@SuppressWarnings("rawtypes")
public class UserContract extends AbstractContract {

    @SuppressWarnings("unchecked")
    public UserContract(UniquidNodeStateContext uniquidNodeStateContext) {
        super(uniquidNodeStateContext);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UserContract.class);

    @Override
    public void doRealContract(final Transaction tx) throws Exception {

        LOGGER.info("Making user contract from TX {}", tx.getTxId().toString());

        List<TransactionOutput> transactionOutputs = tx.getOutputs();

        if (transactionOutputs.size() != 4) {
            LOGGER.error("Contract not valid! size is not 4");
            return;
        }

        Script script = tx.getInput(0).getScriptSig();

        LegacyAddress providerAddress = LegacyAddress.fromPubKeyHash(uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters(),
                org.bitcoinj.core.Utils.sha256hash160(ScriptPattern.extractHashFromP2SH(script)));

        List<TransactionOutput> ts = new ArrayList<>(transactionOutputs);

        LegacyAddress userAddress = getAddressFromTransactionOutput(ts.get(0), uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters());
        if (userAddress == null || !uniquidNodeStateContext.getUserWallet().isPubKeyHashMine(userAddress.getHash(), Script.ScriptType.P2PKH)) {
            LOGGER.error("Contract not valid! User address is null or we are not the user");
            return;
        }

        if (!WalletUtils.isValidOpReturn(tx)) {
            LOGGER.error("Contract not valid! OPRETURN not valid");
            return;
        }

        LegacyAddress revoke = getAddressFromTransactionOutput(ts.get(2), uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters());
        if (revoke == null /*|| !WalletUtils.isUnspent(tx.getHashAsString(), revoke.toBase58())*/) {
            LOGGER.error("Contract not valid! Revoke address is null or contract revoked");
            return;
        }

        String providerName = retrieveNameFromProvider(providerAddress, uniquidNodeStateContext);
        if (providerName == null) {
            LOGGER.error("Contract not valid! Provider name is null");
            return;
        }

        ECKey key = uniquidNodeStateContext.getUserWallet().findKeyFromPubKeyHash(userAddress.getHash(), Script.ScriptType.P2PKH);
        String path = null;
        if (key != null) {
            path = ((DeterministicKey) key).getPathAsString();
        }

        LOGGER.info("Contract is valid. Inserting in register");

        // Create channel
        final UserChannel userChannel = new UserChannel();
        userChannel.setProviderAddress(providerAddress.toBase58());
        userChannel.setUserAddress(userAddress.toBase58());
        userChannel.setProviderName(providerName);
        userChannel.setRevokeAddress(revoke.toBase58());
        userChannel.setRevokeTxId(tx.getTxId().toString());
        userChannel.setSince(0);
        userChannel.setUntil(Long.MAX_VALUE);
        userChannel.setPath(path);

        String opreturn = WalletUtils.getOpReturn(tx);

        byte[] op_to_byte = Hex.decode(opreturn);

        byte[] bitmask = Arrays.copyOfRange(op_to_byte, 0, 19);

        // encode to be saved on db
        String bitmaskToString = new String(Hex.encode(bitmask));

        userChannel.setBitmask(bitmaskToString);

        UserRegister userRegister = uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getUserRegister();

        uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().startTransaction();

        try {

            userRegister.insertChannel(userChannel);

            LOGGER.trace("Inserted user register: " + userRegister);

            uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().commitTransaction();

        } catch (RegisterException ex) {

            LOGGER.error("Error while inserting channel", ex);

            uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().rollbackTransaction();

            // ReThrow
            throw ex;

        }

        // Inform listeners
        uniquidNodeStateContext.getUniquidNodeEventService().onUserContractCreated(userChannel);

    }

    @Override
    public void revokeRealContract(final Transaction tx) throws Exception {

        LegacyAddress address = LegacyAddress.fromPubKeyHash(uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters(),
                org.bitcoinj.core.Utils.sha256hash160(ScriptPattern.extractHashFromP2SH(tx.getInput(0).getScriptSig())));

        // Retrieve sender
        String sender = address.toBase58();

        UserRegister userRegister;
        try {

            userRegister = uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getUserRegister();
            final UserChannel channel = userRegister.getUserChannelByRevokeAddress(sender);

            if (channel != null) {

                LOGGER.info("Found an user contract to revoke!");
                // contract revoked
                userRegister.deleteChannel(channel);

                LOGGER.info("Contract revoked! " + channel);

                // Inform listeners
                uniquidNodeStateContext.getUniquidNodeEventService().onUserContractRevoked(channel);

            } else {

                LOGGER.warn("No contract found to revoke!");
            }

        } catch (Exception e) {

            LOGGER.error("Exception", e);

        }

    }

    protected String retrieveNameFromProvider(LegacyAddress providerAddress, UniquidNodeStateContext uniquidNodeStateContext) throws RegistryException {

        RegistryDAO registryDAO = new RegistryDAOImpl(uniquidNodeStateContext.getUniquidNodeConfiguration().getRegistryUrl());

        return registryDAO.retrieveProviderName(providerAddress.toBase58());

    }

}
