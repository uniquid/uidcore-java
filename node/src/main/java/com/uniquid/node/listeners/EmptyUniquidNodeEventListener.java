package com.uniquid.node.listeners;

import java.util.Date;
import java.util.Set;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;

import com.uniquid.node.UniquidNodeState;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.user.UserChannel;

/**
 * Utility class that implements UniquidNodeEventListener but doesn't provide any implementation for the callbacks.
 */
public class EmptyUniquidNodeEventListener implements UniquidNodeEventListener {

	@Override
	public void onProviderContractCreated(ProviderChannel providerChannel) {
		// NOTHING TO DO
	}

	@Override
	public void onProviderContractRevoked(ProviderChannel providerChannel) {
		// NOTHING TO DO
	}

	@Override
	public void onUserContractCreated(UserChannel userChannel) {
		// NOTHING TO DO		
	}

	@Override
	public void onUserContractRevoked(UserChannel userChannel) {
		// NOTHING TO DO		
	}

	@Override
	public void onSyncNodeStart() {
		// NOTHING TO DO		
	}

	@Override
	public void onSyncNodeEnd() {
		// NOTHING TO DO		
	}

	@Override
	public void onSyncStarted(int blocks) {
		// NOTHING TO DO		
	}

	@Override
	public void onSyncProgress(double pct, int blocksSoFar, Date date) {
		// NOTHING TO DO		
	}

	@Override
	public void onSyncEnded() {
		// NOTHING TO DO		
	}

	@Override
	public void onNodeStateChange(UniquidNodeState newState) {
		// NOTHING TO DO		
	}

	@Override
	public void onPeerConnected(Peer peer, int peerCount) {
		// NOTHING TO DO		
	}

	@Override
	public void onPeerDisconnected(Peer peer, int peerCount) {
		// NOTHING TO DO		
	}

	@Override
	public void onPeersDiscovered(Set<PeerAddress> peerAddresses) {
		// NOTHING TO DO		
	}

}
