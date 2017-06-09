package com.uniquid.node.impl.state;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;

/**
 * Implementation of State Design pattern: methods that are influenced by a particular state are defined in this
 * interface.
 */
public interface UniquidNodeState {

	/**
	 * Allow to implement logic to send coins to blockchain
	 * @param wallet
	 * @param tx
	 */
	public void onCoinsSent(final Wallet wallet, final Transaction tx);

	/**
	 * Allow to implement logic to receive from from blockchain
	 * @param wallet
	 * @param tx
	 */
	public void onCoinsReceived(final Wallet wallet, final Transaction tx);

	/**
	 * Returns the {@link com.uniquid.node.UniquidNodeState} related to this state. 
	 * @return
	 */
	public com.uniquid.node.UniquidNodeState getNodeState();
	
	/**
	 * Returns the imprinting address
	 * @return
	 */
	public String getImprintingAddress();

	/**
	 * Returns the public key
	 * @return
	 */
	public String getPublicKey();
}