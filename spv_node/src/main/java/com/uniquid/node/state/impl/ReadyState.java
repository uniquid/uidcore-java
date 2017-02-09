package com.uniquid.node.state.impl;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.node.state.NodeState;
import com.uniquid.node.state.NodeStateContext;

/**
 * This class represents an Uniquid Node imprinted and ready to reeive/sign contracts.
 * 
 * @author giuseppe
 *
 */
public class ReadyState implements NodeState {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReadyState.class.getName());
    
	@Override
	public void onCoinsSent(NodeStateContext nodeStateContext, Wallet wallet, Transaction tx) {
		
		// We sent some coins. Probably we create a contract as Provider
		if (wallet.equals(nodeStateContext.getProviderWallet())) {
			
			LOGGER.info("Sent coins from provider wallet");

			try {
				
				LOGGER.info("Creating provider contract!");
				com.uniquid.node.utils.Utils.makeProviderContract(wallet, tx, nodeStateContext);
				
			} catch (Exception ex) {
	
				LOGGER.error("Exception while creating provider contract", ex);
	
			}

		} else if (wallet.equals(nodeStateContext.getUserWallet())) {
			
			LOGGER.info("Sent coins from user wallet");
			
			if (com.uniquid.node.utils.Utils.isValidRevokeContract(tx, nodeStateContext)) {
			
				LOGGER.info("Revoking contract!");
				com.uniquid.node.utils.Utils.revokeContract(wallet, tx, nodeStateContext);
				
			}
			
		} else {
			
			LOGGER.info("We sent coins from a wallet that we don't expect!");
			
		}
		
	}

	@Override
	public void onCoinsReceived(NodeStateContext nodeStateContext, Wallet wallet, Transaction tx) {

		// Received a contract!!!
		if (wallet.equals(nodeStateContext.getProviderWallet())) {

			LOGGER.info("Received coins on provider wallet");

			// If is imprinting transaction...
			if (com.uniquid.node.utils.Utils.isValidImprintingTransaction(tx, nodeStateContext)) {

				// imprint!
				LOGGER.warn("Attention! Another machine tried to imprint US! Skip request!");

			} /*else if (com.uniquid.node.utils.Utils.isValidRevokeContract(tx, networkParameters, nodeStateContext)) {

				LOGGER.info("Revoking contract!");
				com.uniquid.node.utils.Utils.revokeContract(wallet, tx, networkParameters, nodeStateContext);

			} */ else {

				LOGGER.info("Unknown contract");

			}

		} else if (wallet.equals(nodeStateContext.getUserWallet())) {

			LOGGER.info("Received coins on user wallet");

			/*if (com.uniquid.node.utils.Utils.isValidRevokeContract(tx, networkParameters, nodeStateContext)) {

				LOGGER.info("Revoking contract!");
				com.uniquid.node.utils.Utils.revokeContract(wallet, tx, networkParameters, nodeStateContext);

			} else {*/

				try {

					LOGGER.info("Creating user contract!");
					com.uniquid.node.utils.Utils.makeUserContract(wallet, tx, nodeStateContext);

				} catch (Exception ex) {

					LOGGER.error("Exception while creating provider contract", ex);

				}

			//}

		} else {
			
			LOGGER.warn("We received coins on a wallet that we don't expect!");
			
		}
	}
	
	public String toString() {
		return "Initialized! Ready";
	}

	@Override
	public State getState() {
		
		return State.READY;
		
	}

}
