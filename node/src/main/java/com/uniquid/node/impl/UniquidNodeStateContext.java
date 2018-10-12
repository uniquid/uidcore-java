package com.uniquid.node.impl;

import com.uniquid.node.impl.state.UniquidNodeState;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.wallet.Wallet;

/**
 * {@link UniquidNodeStateContext} allows to implement State design Pattern: it present a single interface to the
 * outside world. This interface is used by the particular state to ask for needed parameters to perform its job.
 */
public interface UniquidNodeStateContext<T extends UniquidNodeConfiguration> {

	/**
	 * Change internal state
	 * @param nodeState new internal state
	 */
	void setUniquidNodeState(final UniquidNodeState nodeState);
	
	/**
	 * Return provider wallet
	 * @return provider wallet
	 */
	Wallet getProviderWallet();
	
	/**
	 * Return user wallet
	 * @return user wallet
	 */
	Wallet getUserWallet();
	
	/**
	 * Return UniquidNodeConfiguration
	 * @return node configuration
	 */
	T getUniquidNodeConfiguration();
	
	/**
	 * Returns imprinting address
	 * @return address to the imprinting path
	 */
	LegacyAddress getImprintingAddress();
	
	/**
	 * Return Event Service
	 * @return event service
	 */
	UniquidNodeEventService getUniquidNodeEventService();
	
	/**
	 * Return node public key
	 * @return node public key
	 */
	String getPublicKey();
	
}
