package com.uniquid.node.listeners;

import java.util.Date;
import java.util.Set;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;

import com.uniquid.node.UniquidNodeState;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.user.UserChannel;

/**
 * UniquidNodeEventListener allows to receive callbacks from Uniquid Node when something happens.
 */
public interface UniquidNodeEventListener {
	
	/**
	 * Called when a provider channel is created
	 * 
	 * @param providerChannel the ProviderChannel created
	 */
	public void onProviderContractCreated(final ProviderChannel providerChannel);
	
	/**
	 * Called when a provider channel is revoked
	 * 
	 * @param providerChannel the ProviderChannel revoked
	 */
	public void onProviderContractRevoked(final ProviderChannel providerChannel);
	
	/**
	 * Called when a user channel is created
	 * 
	 * @param userChannel the UserChannel created
	 */
	public void onUserContractCreated(final UserChannel userChannel);
	
	/**
	 * Called when a user channel is revoked
	 * 
	 * @param userChannel the UserChannel revoked
	 */
	public void onUserContractRevoked(final UserChannel userChannel);

	/**
	 * Called when the node start synchronization with the BlockChain
	 */
	public void onSyncNodeStart();
	
	/**
	 * Called when the node end synchronization with the BlockChain
	 */
	public void onSyncNodeEnd();
	
	/**
	 * Called when the node start synchronization with the BlockChain
	 * 
	 *  @param blocks the number of blocks to download, estimated
	 */
	public void onSyncStarted(final int blocks);
	
	/**
	 * Called when download progress is made.
	 * 
	 * @param pct  the percentage of chain downloaded, estimated
     * @param date the date of the last block downloaded
	 */
	public void onSyncProgress(final double pct, final int blocksSoFar, final Date date);
	
	/**
	 * Called when a sync from blockchain terminates.
	 * 
	 */
	public void onSyncEnded();
	
	/**
	 * Called when a Node's internal state changes.
	 * 
	 * @param newState the new state of the Node.
	 */
	public void onNodeStateChange(final UniquidNodeState newState);

	
	/**
     * Called when a peer is connected. If this listener is registered to a {@link Peer} instead of a {@link PeerGroup},
     * peerCount will always be 1.
     *
     * @param peer
     * @param peerCount the total number of connected peers
     */
	public void onPeerConnected(Peer peer, int peerCount);

	/**
     * Called when a peer is disconnected. Note that this won't be called if the listener is registered on a
     * {@link PeerGroup} and the group is in the process of shutting down. If this listener is registered to a
     * {@link Peer} instead of a {@link PeerGroup}, peerCount will always be 0. This handler can be called without
     * a corresponding invocation of onPeerConnected if the initial connection is never successful.
     *
     * @param peer
     * @param peerCount the total number of connected peers
     */
	public void onPeerDisconnected(Peer peer, int peerCount);

	/**
     * Called when peers are discovered, this happens at startup of {@link PeerGroup} or if we run out of
     * suitable {@link Peer}s to connect to.
     *
     * @param peerAddresses the set of discovered {@link PeerAddress}
     */
	public void onPeersDiscovered(Set<PeerAddress> peerAddresses);

}
