package com.uniquid.node;

import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.store.BlockStoreException;

import com.uniquid.node.exception.NodeException;
import com.uniquid.node.listeners.UniquidNodeEventListener;

/**
 * An Uniquid node is a network node that use the blockchain to establish trust and creates and use smart contracts.
 * 
 * @author Giuseppe Magnotta
 */
public interface UniquidNode {

	/**
	 * Returns the imprinting address of this node
	 * 
	 * @return imprinting address of this node
	 */
	public String getImprintingAddress();

	/**
	 * Returns the public key of this node
	 * 
	 * @return public key of this node
	 */
	public String getPublicKey();

	/**
	 * Returns the node name
	 * 
	 * @return the name of this node
	 */
	public String getNodeName();
	
	/**
	 * Returns the creation time of this node
	 * 
	 * @return creation time of this node
	 */
	public long getCreationTime();
	
	/**
	 * Returns the hex seed of this node
	 * @return the hex seed of this node
	 */
	public String getHexSeed();
	
	/**
	 * Return the spendable balance of this node
	 * 
	 * @return the spendable balance of this node
	 */
	public String getSpendableBalance();

	/**
	 * Initialize this node
	 */
	public void initNode() throws NodeException;
	
	/**
	 * Synchronize the node against the blockchain.
	 */
	public void updateNode() throws NodeException;

	/**
	 * Returns a description of the current state of this node.
	 * 
	 * @return the description of the current state of this node
	 */
	public UniquidNodeState getNodeState();

	/**
	 * Register an event listener
	 * 
	 * @param uniquidNodeEventListener the event listener that will receive callbacks
	 */
	public void addUniquidNodeEventListener(final UniquidNodeEventListener uniquidNodeEventListener);

	/**
	 * Unregister an event listener
	 * 
	 * @param uniquidNodeEventListener the event listener that will be removed
	 */
	public void removeUniquidNodeEventListener(final UniquidNodeEventListener uniquidNodeEventListener);
	
	/**
	 * Sign transaction
	 * @param s_tx
	 * @param path
	 * @return
	 * @throws NodeException
	 */
	public String signTransaction(final String s_tx, final String path) throws NodeException;
	
}