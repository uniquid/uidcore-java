package com.uniquid.node;

import org.bitcoinj.core.Address;

import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.node.state.NodeState.State;

public interface UniquidNode {

	Address getImprintingAddress();

	String getPublicKey();

	String getMachineName();

	void initNode() throws Exception;

	void addUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener);

	void removeUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener);

	State getNodeState();

	/**
	 * This method will blocks
	 * @throws Exception
	 */
	void updateNode() throws Exception;

}