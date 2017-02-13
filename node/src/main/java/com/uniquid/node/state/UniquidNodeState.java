package com.uniquid.node.state;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;

import com.uniquid.node.UniquidNode;

/**
 * Represents the internal State of an {@link UniquidNode}. Implementaton of State design pattern
 * 
 * @author Giuseppe Magnotta
 *
 */
public interface UniquidNodeState {
	
	/**
	 * The enumeration of possible states
	 * 
	 */
	public enum EnumState {
		
		IMPRINTING, READY;
		
	}
	
	/**
	 * Called when coins are received
	 */
	public void onCoinsSent(UniquidNodeStateContext nodeStateContext, Wallet wallet, Transaction tx);
	
	/**
	 * Called when coins are sent 
	 */
	public void onCoinsReceived(UniquidNodeStateContext nodeStateContext, Wallet wallet, Transaction tx);
	
	/**
	 * Returns the current EnumState
	 * 
	 * @return the current EnumState
	 */
	public EnumState getState();

}
