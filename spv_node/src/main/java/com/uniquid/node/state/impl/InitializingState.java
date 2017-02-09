package com.uniquid.node.state.impl;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.node.state.NodeState;
import com.uniquid.node.state.NodeStateContext;

/**
 * This class represent an Uniquid Node just created (empty).
 * 
 */
public class InitializingState implements NodeState {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InitializingState.class.getName());
	
	private NodeStateContext nodeStateContext;
	private NetworkParameters networkParameters;
	private Address imprintingAddress;
	private boolean alreadyImprinted;
	
	public InitializingState(NodeStateContext nodeStateContext, Address imprintingAddress) {
		this.nodeStateContext = nodeStateContext;
		this.networkParameters = nodeStateContext.getNetworkParameters();
		this.imprintingAddress = imprintingAddress;
		this.alreadyImprinted = false;
	}
	
	@Override
	public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		/*if (wallet.equals(nodeStateContext.getRevokeWallet())) {
			
			LOGGER.info("A contract was revoked!!!");
			
		} else {*/
			
			LOGGER.info("We sent coins from a wallet that we don't expect!");
			
		//}
	}

	@Override
	public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		if (wallet.equals(nodeStateContext.getProviderWallet())) {
			
			LOGGER.info("Received coins on provider wallet");
			
			try {
				
				// If is imprinting transaction...
				if (com.uniquid.node.utils.Utils.isValidImprintingTransaction(tx, networkParameters, imprintingAddress) && !alreadyImprinted) {
					
					LOGGER.info("Imprinting contract");
					
					// imprint!
					com.uniquid.node.utils.Utils.makeImprintContract(tx, networkParameters,  nodeStateContext, imprintingAddress);
					
					alreadyImprinted = true;

				} else {
					
					LOGGER.info("Invalid imprit contract");
					
				}
	
			} catch (Exception ex) {
	
				LOGGER.error("Exception while imprinting", ex);
	
			}
		
		} else if (wallet.equals(nodeStateContext.getUserWallet())) {
			
			LOGGER.info("Received coins on user wallet");
			
			try {
				
				com.uniquid.node.utils.Utils.makeUserContract(wallet, tx, networkParameters, nodeStateContext);
					
			} catch (Exception ex) {
	
				LOGGER.error("Exception while creating user contract", ex);
	
			}
			
		} else {
			
			LOGGER.warn("We received coins on a wallet that we don't expect!");
			
		}
	}
	
	public String toString() {

		return "Initializing State";

	}

}
