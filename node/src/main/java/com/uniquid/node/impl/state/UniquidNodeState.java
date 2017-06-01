package com.uniquid.node.impl.state;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;

/**
 * Implementation of State Design pattern: most public methods will be delegated to current state
 */
public interface UniquidNodeState {

	public void onCoinsSent(final Wallet wallet, final Transaction tx);

	public void onCoinsReceived(final Wallet wallet, final Transaction tx);

	public com.uniquid.node.UniquidNodeState getNodeState();
	
	public String getImprintingAddress();

	public String getPublicKey();
}