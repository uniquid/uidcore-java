package com.uniquid.node.impl;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.Wallet;

import com.uniquid.node.impl.state.UniquidNodeState;
import com.uniquid.register.RegisterFactory;

/**
 * UniquidNodeStateContext allow to implement State design pattern
 */
public interface UniquidNodeStateContext {

	public void setUniquidNodeState(final UniquidNodeState nodeState);
	
	public Wallet getProviderWallet();
	
	public Wallet getUserWallet();
	
	public NetworkParameters getNetworkParameters();
	
	public Address getImprintingAddressValue();
	
	public RegisterFactory getRegisterFactory();
	
	public UniquidNodeEventService getUniquidNodeEventService();
	
	public String getPublicKeyValue();
	
}
