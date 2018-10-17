package com.uniquid.node.impl.utils;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;
import org.bitcoinj.core.*;
import org.bitcoinj.script.ScriptPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.uniquid.node.impl.utils.NodeUtils.getAddressFromTransactionOutput;

/**
 * UniquidNodeStateUtils contains some useful methods used by UniquidNodeState class
 */
public class UniquidNodeStateUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeStateUtils.class.getName());

    /**
     * Return true if the transaction in input is a valid imprinting transaction and contains the specified address in
     * one of its output, otherwise false.
     * @param tx the transaction to check if is valid imprinting
     * @param networkParameters the {@link NetworkParameters}
     * @param imprintingAddress the address to check for
     * @return true if it's an imprinting transaction otherwise false
     */
    public static boolean isValidImprintingTransaction(Transaction tx, NetworkParameters networkParameters, Address imprintingAddress) {

        // Check output
        List<TransactionOutput> transactionOutputs = tx.getOutputs();
        for (TransactionOutput to : transactionOutputs) {
            Address address = getAddressFromTransactionOutput(to, networkParameters);
            if (address != null && address.equals(imprintingAddress)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the transaction is a valid revoke transaction
     * @param tx the transaction to check
     * @param registerFactory the {@link RegisterFactory} to use to access the data store
     * @return true if the revoke address is present in the data store
     */
    public static boolean isValidRevokeContract(Transaction tx, NetworkParameters parameters, RegisterFactory registerFactory) {

        LegacyAddress address = LegacyAddress.fromPubKeyHash(parameters,
                org.bitcoinj.core.Utils.sha256hash160(ScriptPattern.extractHashFromPayToScriptHash(tx.getInput(0).getScriptSig())));

        // Retrieve sender
        String sender = address.toBase58();

        ProviderRegister providerRegister;
        try {
            providerRegister = registerFactory.getProviderRegister();
            ProviderChannel channel = providerRegister.getChannelByRevokeAddress(sender);

            if (channel != null) {
                return true;
            }

        } catch (Exception e) {

            LOGGER.error("Exception", e);

        }

        return false;

    }

    /**
     * Returns true if the transaction is a valid revoke transaction
     * @param tx the transaction to check
     * @param registerFactory the {@link RegisterFactory} to use to access the data store
     * @return true if the revoke address is present in the data store
     */
    public static boolean isValidRevokeUserContract(Transaction tx, NetworkParameters parameters, RegisterFactory registerFactory) {

        LegacyAddress address = LegacyAddress.fromPubKeyHash(parameters,
                org.bitcoinj.core.Utils.sha256hash160(ScriptPattern.extractHashFromPayToScriptHash(tx.getInput(0).getScriptSig())));

        // Retrieve sender
        String sender = address.toBase58();

        UserRegister userRegister;
        try {
            userRegister = registerFactory.getUserRegister();
            UserChannel channel = userRegister.getUserChannelByRevokeAddress(sender);

            if (channel != null) {
                return true;
            }

        } catch (Exception e) {

            LOGGER.error("Exception", e);

        }

        return false;

    }

}
