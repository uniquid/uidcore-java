package com.uniquid.node.state;

import java.util.List;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.Wallet;

import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.register.RegisterFactory;

/**
 * Interface that allow to decouple the State design pattern
 * 
 * @author Giuseppe Magnotta
 *
 */
public interface UniquidNodeStateContext {
	
	/**
	 * Change the internal state 

	 * @param uniquidNodeState
	 */
	public void setUniquidNodeState(UniquidNodeState uniquidNodeState);
	
	/**
	 * Return the {@link NetworkParameters} used by the UniquidNode
	 * 
	 */
	public NetworkParameters getNetworkParameters();
	
	/**
	 * Return the imprinting address
	 * @return
	 */
	public String getImprintingAddress();
	public Wallet getProviderWallet();
	public Wallet getUserWallet();
	
	public RegisterFactory getRegisterFactory();
	
	public List<UniquidNodeEventListener> getUniquidNodeEventListeners();
	
}
