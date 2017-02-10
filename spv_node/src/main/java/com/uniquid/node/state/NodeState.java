package com.uniquid.node.state;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;

/**
 * This interface defines the methods that can be executed by the node in a particular states
 * 
 * @author giuseppe
 *
 */
public interface NodeState {
	
	public enum State {
		
		IMPRINTING, READY;
		
	}
	
	public void onCoinsSent(NodeStateContext nodeStateContext, Wallet wallet, Transaction tx);
	
	public void onCoinsReceived(NodeStateContext nodeStateContext, Wallet wallet, Transaction tx);
	
	public State getState();

}
