package com.uniquid.node.impl.utils;

import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;

public class UniquidNodeStateUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeStateUtils.class.getName());

	public static boolean isValidImprintingTransaction(Transaction tx, NetworkParameters networkParameters, Address imprintingAddress) {
		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();

		// Check output
		List<TransactionOutput> transactionOutputs = tx.getOutputs();
		for (TransactionOutput to : transactionOutputs) {
			Address address = to.getAddressFromP2PKHScript(networkParameters);
			if (address != null && address.equals(imprintingAddress)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isValidRevokeContract(Transaction tx, RegisterFactory registerFactory) {

		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();

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

}
