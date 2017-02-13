package com.uniquid.node.state;

import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.Wallet;

import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.register.RegisterFactory;

public interface NodeStateContext {
	
	public void setNodeState(NodeState nodeState);
	
	public NetworkParameters getNetworkParameters();
	public Address getImprintingAddress();
	public Wallet getProviderWallet();
	public Wallet getUserWallet();
	
	public RegisterFactory getRegisterFactory();
	
	public List<UniquidNodeEventListener> getUniquidNodeEventListeners();
	
}
