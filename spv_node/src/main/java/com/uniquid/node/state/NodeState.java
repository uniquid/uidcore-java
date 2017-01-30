package com.uniquid.node.state;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;

/**
 * This interface defines the methods that can be executed by the node in a particular state
 * 
 * @author giuseppe
 *
 */
public interface NodeState {
	
	void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance);
	
	void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance);

}
