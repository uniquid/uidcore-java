package com.uniquid.node;

import org.bitcoinj.core.Address;

import com.uniquid.node.exception.NodeException;
import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.node.state.NodeState.State;

/**
 * The Uniquid node interface
 */
public interface UniquidNode {

	/**
	 * Returns the impriting address
	 */
	public Address getImprintingAddress();

	/**
	 * Returns the public key of this node
	 */
	public String getPublicKey();

	/**
	 * Returns the machine name
	 */
	public String getMachineName();

	/**
	 * Initialize this node with some random byte entropy 
	 */
	public void initNode() throws NodeException;
	
	/**
	 * Initialize this node starting with specified byte array entropy and creation date
	 */
	public void initNodeFromHexEntropy(String hexEntropy, long creationTime) throws NodeException;

	/**
	 * Synchronize the node against the blockchain.
	 */
	public void updateNode() throws NodeException;

	/**
	 * Returns the state of this node.
	 */
	public State getNodeState();

	/**
	 * Register an event listener
	 */
	public void addUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener);

	/**
	 * Unregister an event listener
	 */
	public void removeUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener);

}