package com.uniquid.node.state.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
 * This class represents an Uniquid Node imprinted and ready to reeive/sign contracts.
 * 
 * @author giuseppe
 *
 */
public class ReadyState implements NodeState {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReadyState.class.getName());
    
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	
    private NodeStateContext nodeStateContext;
	private NetworkParameters networkParameters;
	private Address imprintingAddress;
	
	public ReadyState(NodeStateContext nodeStateContext, Address imprintingAddress) {
		this.nodeStateContext = nodeStateContext;
		this.networkParameters = nodeStateContext.getNetworkParameters();
		this.imprintingAddress = imprintingAddress;
	}

	@Override
	public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		// We sent some coins. Probably we create a contract as Provider
		if (wallet.equals(nodeStateContext.getProviderWallet())) {
			
			LOGGER.info("Sent coins from provider wallet");

			try {
				
				com.uniquid.node.utils.Utils.makeProviderContract(tx, networkParameters, nodeStateContext);
				
			} catch (Exception ex) {
	
				LOGGER.error("Exception while creating provider contract", ex);
	
			}
		} else if (wallet.equals(nodeStateContext.getProviderRevokeWallet())) {
			
			LOGGER.info("A Provider contract was revoked!!!");
			
		} else if (wallet.equals(nodeStateContext.getUserRevokeWallet())) {
			
			LOGGER.info("A User contract was revoked!!!");
			
		} else {
			
			LOGGER.info("We sent coins from a wallet that we don't expect!");
			
		}
		
	}

	@Override
	public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		// Received a contract!!!
		if (wallet.equals(nodeStateContext.getProviderWallet())) {
			
			LOGGER.info("Received coins on provider wallet");
				
			// If is imprinting transaction...
			if (com.uniquid.node.utils.Utils.isValidImprintingTransaction(tx, networkParameters, imprintingAddress)) {
				
				// imprint!
				LOGGER.warn("Attention! Another machine tried to imprint US! Skip request!");

			} else {
				
				LOGGER.info("Unknown contract");
				
			}
	
		} else if (wallet.equals(nodeStateContext.getUserWallet())) {
			
			LOGGER.info("Received coins on user wallet");
			
			try {
				
				com.uniquid.node.utils.Utils.makeUserContract(tx, networkParameters, nodeStateContext);
					
			} catch (Exception ex) {
	
				LOGGER.error("Exception while creating provider contract", ex);
	
			}
			
		} else {
			
			LOGGER.warn("We received coins on a wallet that we don't expect!");
			
		}
	}
	
	public String toString() {
		return "Initialized! Ready";
	}

}
