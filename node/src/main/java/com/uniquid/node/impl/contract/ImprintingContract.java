package com.uniquid.node.impl.contract;

import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.node.impl.UniquidNodeEventService;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;

/**
 * Class that manage imprinting contracts
 * 
 * @author giuseppe
 *
 */
public class ImprintingContract extends AbstractContract {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ImprintingContract.class);
	
	private static final String CONTRACT_FUNCTION = "00000000400000000000000000000000000000";

	private Address imprintingAddress;
	
	public ImprintingContract(NetworkParameters networkParameters, Wallet userWallet, Wallet providerWallet, RegisterFactory registerFactory,
			UniquidNodeEventService uniquidNodeEventService, String pubKey, Address imprintingAddres) {
		
		super(networkParameters, userWallet, providerWallet, registerFactory, uniquidNodeEventService, pubKey);
		
		this.imprintingAddress = imprintingAddres;
	}

	@Override
	public void doRealContract(final Transaction tx) throws Exception {

		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();

		// Check output
		List<TransactionOutput> transactionOutputs = tx.getOutputs();
		for (TransactionOutput to : transactionOutputs) {

			Address address = to.getAddressFromP2PKHScript(networkParameters);
			if (address != null && address.equals(imprintingAddress)) {

				// This is our imprinter!!!

				ProviderRegister providerRegister = registerFactory.getProviderRegister();

				// Create provider channel
				final ProviderChannel providerChannel = new ProviderChannel();
				providerChannel.setUserAddress(sender);
				providerChannel.setProviderAddress(imprintingAddress.toBase58());
				providerChannel.setBitmask(CONTRACT_FUNCTION);
				providerChannel.setRevokeAddress("IMPRINTING");
				providerChannel.setRevokeTxId(tx.getHashAsString());
				providerChannel.setCreationTime(tx.getUpdateTime().getTime()/1000);

				// persist channel
				providerRegister.insertChannel(providerChannel);

				// send event
				uniquidNodeEventService.onProviderContractCreated(providerChannel);
				
				break;

			}

		}

	}

	@Override
	public void revokeRealContract(final Transaction tx) throws Exception {
		// DO NOTHING
	}
	
}
