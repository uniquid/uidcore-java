package com.uniquid.spv_node;

import java.io.File;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.Wallet;

import com.uniquid.register.RegisterFactory;

public interface NodeStateContext {
	
	public String getSeed();
	public long getCreationTime();
	public NetworkParameters getNetworkParameters();
	public File getProviderFile();
	public File getUserFile();
	public File getChainFile();
	public Wallet getProviderWallet();
	public void setProviderWallet(Wallet providerWallet);
	public Wallet getUserWallet();
	public void setUserWallet(Wallet userWallet);
	
	public void setNodeState(NodeState nodeState);
	
	public NodeState getNodeState();
	
	public RegisterFactory getRegisterFactory();

}
