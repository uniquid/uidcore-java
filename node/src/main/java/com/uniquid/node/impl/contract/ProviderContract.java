package com.uniquid.node.impl.contract;

import com.uniquid.node.impl.UniquidNodeStateContext;
import com.uniquid.node.impl.utils.WalletUtils;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.uniquid.node.impl.utils.NodeUtils.getAddressFromTransactionOutput;

/**
 * Class that manage provider contracts
 *
 * @author giuseppe
 *
 */
@SuppressWarnings("rawtypes")
public class ProviderContract extends AbstractContract {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderContract.class);

    @SuppressWarnings("unchecked")
    public ProviderContract(UniquidNodeStateContext uniquidNodeStateContext) {
        super(uniquidNodeStateContext);
    }

    @Override
    public void doRealContract(final Transaction tx) throws Exception {

        LOGGER.info("Making provider contract from TX {}", tx.getHashAsString());

        List<TransactionOutput> transactionOutputs = tx.getOutputs();

        if (transactionOutputs.size() != 4) {
            LOGGER.error("Contract not valid! output size is not 4");
            return;
        }

        Script script = tx.getInput(0).getScriptSig();
        LegacyAddress providerAddress = LegacyAddress.fromPubKeyHash(uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters(),
                org.bitcoinj.core.Utils.sha256hash160(ScriptPattern.extractHashFromPayToScriptHash(script)));

        if (!uniquidNodeStateContext.getProviderWallet().isPubKeyHashMine(providerAddress.getHash())) {
            LOGGER.error("Contract not valid! We are not the provider");
            return;
        }

        List<TransactionOutput> ts = new ArrayList<>(transactionOutputs);

        LegacyAddress userAddress = getAddressFromTransactionOutput(ts.get(0), uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters());

        // We are provider!!!
        if (userAddress == null) {
            LOGGER.error("Contract not valid! User address is null");
            return;
        }

        if (!WalletUtils.isValidOpReturn(tx)) {
            LOGGER.error("Contract not valid! OPRETURN not valid");
            return;
        }

        LegacyAddress revoke = getAddressFromTransactionOutput(ts.get(2), uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters());

        if (revoke == null /*
         * ||
         * !WalletUtils.isUnspent(tx.getHashAsString(),
         * revoke.toBase58())
         */) {
            LOGGER.error("Contract not valid! Revoke address is null");
            return;
        }

        LOGGER.info("Contract is valid. Inserting in register");

        ECKey key = uniquidNodeStateContext.getProviderWallet().findKeyFromPubHash(providerAddress.getHash());
        String path = null;
        if (key != null) {
            path = ((DeterministicKey) key).getPathAsString();
        }

        // Create provider channel
        final ProviderChannel providerChannel = new ProviderChannel();
        providerChannel.setProviderAddress(providerAddress.toBase58());
        providerChannel.setUserAddress(userAddress.toBase58());
        providerChannel.setRevokeAddress(revoke.toBase58());
        providerChannel.setRevokeTxId(tx.getHashAsString());
        providerChannel.setCreationTime(tx.getUpdateTime().getTime()/1000);
        providerChannel.setSince(0);
        providerChannel.setUntil(Long.MAX_VALUE);
        providerChannel.setPath(path);

        String opreturn = WalletUtils.getOpReturn(tx);

        byte[] op_to_byte = Hex.decode(opreturn);

        byte[] bitmask = Arrays.copyOfRange(op_to_byte, 0, 19);

        // encode to be saved on db
        String bitmaskToString = new String(Hex.encode(bitmask));

        // persist
        providerChannel.setBitmask(bitmaskToString);

        ProviderRegister providerRegister = uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getProviderRegister();

        uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().startTransaction();

        try {

            List<ProviderChannel> channels = providerRegister.getAllChannels();

            // If this is the first "normal" contract then remove the
            // imprinting
            // contract
            if (channels.size() == 1 && channels.get(0).getRevokeAddress().equals("IMPRINTING")) {

                providerRegister.deleteChannel(channels.get(0));

            }

            providerRegister.insertChannel(providerChannel);

            LOGGER.trace("Inserted provider register: " + providerRegister);

            uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().commitTransaction();

        } catch (RegisterException ex) {

            LOGGER.error("Error while inserting channel", ex);

            uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().rollbackTransaction();

            // ReThrow
            throw ex;

        }

        // Inform listeners
        uniquidNodeStateContext.getUniquidNodeEventService().onProviderContractCreated(providerChannel);

    }

    @Override
    public void revokeRealContract(final Transaction tx) throws Exception {

        LegacyAddress address = LegacyAddress.fromPubKeyHash(uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters(),
                org.bitcoinj.core.Utils.sha256hash160(ScriptPattern.extractHashFromPayToScriptHash(tx.getInput(0).getScriptSig())));

        // Retrieve sender
        String sender = address.toBase58();

        ProviderRegister providerRegister;
        try {

            providerRegister = uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getProviderRegister();
            final ProviderChannel channel = providerRegister.getChannelByRevokeAddress(sender);

            if (channel != null) {

                LOGGER.info("Found a contract to revoke!");
                // contract revoked
                providerRegister.deleteChannel(channel);

                LOGGER.info("Contract revoked! " + channel);

                // Inform listeners
                uniquidNodeStateContext.getUniquidNodeEventService().onProviderContractRevoked(channel);

            } else {

                LOGGER.warn("No contract found to revoke!");
            }

        } catch (Exception e) {

            LOGGER.error("Exception", e);

        }
    }

}
