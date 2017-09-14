package com.uniquid.node;

import java.util.List;

import com.uniquid.node.exception.NodeException;
import com.uniquid.node.listeners.UniquidNodeEventListener;

/**
 * Represents an entity that owns an ID-based cryptography that uses the BlockChain to manage trust relationship with
 * other entities. It is capable to open a connection to the peer to peer network of the BlockChain and:
 * <ul>
 *  <li>synchronize/update the local BlockChain files</li>
 *  <li>listen, verify and re-broadcast valid Transactions inside the peer to peer network</li>
 * </ul>
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
	 * Synchronize the node against the BlockChain.
	 */
	public void updateNode() throws NodeException;

	/**
	 * Returns the current state of this node.
	 * 
	 * @return the {@code UniquidNodeState} representing the current state of this node.
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
	 * Allow to sign an unsigned serialized blockchain transaction.
	 * 
	 * @param serializedTx the unsigned serialized transaction to sign
	 * @param path the bip32 path to use to sign
	 * @return the serialized signed transaction
	 * @throws NodeException in case a problem occurs.
	 */
	public String signTransaction(final String serializedTx, final List<String> paths) throws NodeException;
	
	/**
	 * Sign the input message with the key derived from path specified
	 * 
	 * @param message the message to sign
	 * @param path the path to use to derive HD key
	 * @return the message signed with the derived HD key
	 * @throws NodeException in case a problem occurs.
	 */
	public String signMessage(String message, String path) throws NodeException;
	
	/**
	 * Sign the input message with the private key corresponding to the public key hash specified
	 * 
	 * @param message the message to sign
	 * @param pubKeyHash the hash of the corresponding public key 
	 * @return the message signed with the derived HD key
	 * @throws NodeException in case a problem occurs.
	 */
	public String signMessage(String message, byte[] pubKeyHash) throws NodeException;
	
	/**
	 * Allow to propagate a serialized Tx on the peer2peer network
	 * @param serializedTx
	 * @return
	 * @throws NodeException
	 */
	public String broadCastTransaction(final String serializedTx) throws NodeException;
	
}