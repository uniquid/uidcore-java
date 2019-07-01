package com.uniquid.node.listeners;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.listeners.PeerConnectedEventListener;
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.bitcoinj.core.listeners.PeerDiscoveredEventListener;

import java.util.Set;

/**
 * @author Beatrice Formai
 */
public class UniquidPeerEventListener implements PeerConnectedEventListener, PeerDisconnectedEventListener,
        PeerDiscoveredEventListener {
    @Override
    public void onPeerConnected(Peer peer, int peerCount) {

    }

    @Override
    public void onPeerDisconnected(Peer peer, int peerCount) {

    }

    @Override
    public void onPeersDiscovered(Set<PeerAddress> peerAddresses) {

    }
}
