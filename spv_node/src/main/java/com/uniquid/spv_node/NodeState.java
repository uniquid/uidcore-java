package com.uniquid.spv_node;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;

public interface NodeState {
	
	void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance);
	
	void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance);

}
