package com.uniquid.node.state.impl;

import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.Listener;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.node.state.UniquidNodeState;
import com.uniquid.node.state.UniquidNodeStateContext;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;

/**
 * This class represent an Uniquid Node just created (empty) to imprint.
 * 
 */
public class ImprintingState implements UniquidNodeState {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ImprintingState.class.getName());
	
	private static final String CONTRACT_FUNCTION = "000000400000000000000000000000000000";
	
	private boolean alreadyImprinted;
	
	public ImprintingState() {

		this.alreadyImprinted = false;

	}
	
	@Override
	public void onCoinsSent(UniquidNodeStateContext nodeStateContext, Wallet wallet, Transaction tx) {
		
		/*if (wallet.equals(nodeStateContext.getRevokeWallet())) {
			
			LOGGER.info("A contract was revoked!!!");
			
		} else {*/
			
			LOGGER.info("We sent coins from a wallet that we don't expect!");
			
		//}
	}

	@Override
	public void onCoinsReceived(UniquidNodeStateContext nodeStateContext, Wallet wallet, Transaction tx) {
		
		if (wallet.equals(nodeStateContext.getProviderWallet())) {
			
			LOGGER.info("Received coins on provider wallet");
			
			try {
				
				// If is imprinting transaction...
				if (UniquidNodeStateUtils.isValidImprintingTransaction(tx, nodeStateContext) && !alreadyImprinted) {
					
					LOGGER.info("Imprinting contract");
					
					// imprint!
					makeImprintContract(tx, nodeStateContext);
					
					alreadyImprinted = true;

				} else {
					
					LOGGER.info("Invalid imprinting contract");
					
				}
	
			} catch (Exception ex) {
	
				LOGGER.error("Exception while imprinting", ex);
	
			}
		
		} else if (wallet.equals(nodeStateContext.getUserWallet())) {
			
			LOGGER.info("Received coins on user wallet");
			
			try {
				
				UniquidNodeStateUtils.makeUserContract(wallet, tx, nodeStateContext);
					
			} catch (Exception ex) {
	
				LOGGER.error("Exception while creating user contract", ex);
	
			}
			
		} else {
			
			LOGGER.warn("We received coins on a wallet that we don't expect!");
			
		}

	}
	
	public String toString() {

		return "Imprinting State";

	}

	@Override
	public EnumState getState() {
		
		return EnumState.IMPRINTING;
		
	}
	
	private static void makeImprintContract(final Transaction tx, final UniquidNodeStateContext nodeStateContext)
			throws Exception {

		// Transaction already confirmed
		if (tx.getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)) {

			doImprint(tx, nodeStateContext);

			// DONE

		} else {

			final Listener listener = new Listener() {

				@Override
				public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {

					try {

						if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)
								&& reason.equals(ChangeReason.TYPE)) {

							doImprint(tx, nodeStateContext);

							tx.getConfidence().removeEventListener(this);

							LOGGER.info("Imprinting Done!");

						} else if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.DEAD)
								&& reason.equals(ChangeReason.TYPE)) {

							LOGGER.error("Something bad happened! TRansaction is DEAD!");

							tx.getConfidence().removeEventListener(this);

						}

					} catch (Exception ex) {

						LOGGER.error("Exception while populating Register", ex);

					}

				}

			};

			// Transaction not yet confirmed! Register callback!
			tx.getConfidence().addEventListener(listener);

		}

	}

	private static void doImprint(Transaction tx, UniquidNodeStateContext nodeStateContext) throws Exception {

		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();

		// Check output
		List<TransactionOutput> transactionOutputs = tx.getOutputs();
		for (TransactionOutput to : transactionOutputs) {
			Address address = to.getAddressFromP2PKHScript(nodeStateContext.getNetworkParameters());
			if (address != null && address.equals(Address.fromBase58(nodeStateContext.getNetworkParameters(),
					nodeStateContext.getImprintingAddress()))) {

				// This is our imprinter!!!

				ProviderRegister providerRegister = nodeStateContext.getRegisterFactory().getProviderRegister();

				ProviderChannel providerChannel = new ProviderChannel();
				providerChannel.setUserAddress(sender);
				providerChannel.setProviderAddress(nodeStateContext.getImprintingAddress());
				providerChannel.setBitmask(CONTRACT_FUNCTION);
				providerChannel.setRevokeAddress("IMPRINTING");
				providerChannel.setRevokeTxId(tx.getHashAsString());

				// persist channel
				providerRegister.insertChannel(providerChannel);

				// We can move now to ReadyState
				nodeStateContext.setUniquidNodeState(new ReadyState());

				// Send event to listeners
				for (UniquidNodeEventListener listener : nodeStateContext.getUniquidNodeEventListeners()) {

					listener.onProviderContractCreated(providerChannel);

				}

				LOGGER.info("Machine IMPRINTED!");

				break;

			}

		}
	}

}
