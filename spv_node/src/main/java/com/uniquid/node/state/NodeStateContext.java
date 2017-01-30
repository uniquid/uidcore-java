package com.uniquid.node.state;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.Wallet;

import com.uniquid.register.RegisterFactory;

public interface NodeStateContext {
	
	public NodeState getNodeState();
	public void setNodeState(NodeState nodeState);

	public NetworkParameters getNetworkParameters();
	public Wallet getProviderWallet();
	public Wallet getProviderRevokeWallet();
	public Wallet getUserWallet();
	public Wallet getUserRevokeWallet();
	
	public RegisterFactory getRegisterFactory();

}
