package com.uniquid.node.state;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;

import com.uniquid.register.RegisterFactory;

public interface NodeStateContext {
	
	public void setNodeState(NodeState nodeState);
	
	public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance);
	
	public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance);

	public NetworkParameters getNetworkParameters();
	public Wallet getProviderWallet();
//	public Wallet getRevokeWallet();
	public Wallet getUserWallet();
	
	public RegisterFactory getRegisterFactory();

}
