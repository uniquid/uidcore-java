package com.uniquid.node.impl;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.Wallet;

import com.uniquid.node.impl.state.UniquidNodeState;
import com.uniquid.register.RegisterFactory;

/**
 * {@link UniquidNodeStateContext} allows to implement State design Pattern: it present a single interface to the
 * outside world. This interface is used by the particular state to ask for needed parameters to perform its job.
 */
public interface UniquidNodeStateContext {

	/**
	 * Change internal state
	 * @param nodeState new internal state
	 */
	public void setUniquidNodeState(final UniquidNodeState nodeState);
	
	/**
	 * Return provider wallet
	 * @return provider wallet
	 */
	public Wallet getProviderWallet();
	
	/**
	 * Return user wallet
	 * @return
	 */
	public Wallet getUserWallet();
	
	/**
	 * Return NetworkParameters
	 * @return
	 */
	public NetworkParameters getNetworkParameters();
	
	/**
	 * Returns imprinting address
	 * @return
	 */
	public Address getImprintingAddress();
	
	/**
	 * Returns register factory
	 * @return
	 */
	public RegisterFactory getRegisterFactory();
	
	/**
	 * Return Event Service
	 * @return
	 */
	public UniquidNodeEventService getUniquidNodeEventService();
	
	/**
	 * Return node publick key
	 * @return
	 */
	public String getPublicKey();
	
}
