package com.uniquid.node;

import org.bitcoinj.core.Address;

import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.node.state.NodeState.State;

public interface UniquidNode {

	public Address getImprintingAddress();

	public String getPublicKey();

	public String getMachineName();

	public void initNode() throws Exception;
	
	public void initNodeFromHexEntropy(String hexEntropy, long creationTime) throws Exception;

	public void addUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener);

	public void removeUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener);

	public State getNodeState();

	/**
	 * This method will blocks
	 * @throws Exception
	 */
	public void updateNode() throws Exception;

}