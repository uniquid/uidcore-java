package com.uniquid.node.impl.utils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.jni.NativePeerEventListener;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.net.discovery.SeedPeers;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.google.common.collect.ImmutableList;
import com.uniquid.node.impl.UniquidNodeConfiguration;

/**
 * NodeUtils contains some static useful methods
 */
public class NodeUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeUtils.class.getName());
	
	/**
	 * Generates a {@code DeterministicKey} root key from DeterministicSeed
	 * 
	 * @param seed the DeterministicSeed
	 * @return returns an instance of {@code DeterministicKey} representing the root key
	 */
	public static DeterministicKey createDeterministicKeyFromDeterministicSeed(DeterministicSeed seed) {

		DeterministicKey rootKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
		rootKey.setCreationTimeSeconds(seed.getCreationTimeSeconds());
		
		return rootKey;
	}
	
	/**
	 * Synchronize a list of {@code Wallet} against the BlockChain.
	 * 
	 * @param params the NetworkParameters to use
	 * @param wallets the list of Wallet
	 * @param chainFile the chain file to use
	 * @param listener the listener to inform for status changes
	 */
	public static void syncBlockChain(UniquidNodeConfiguration configuration, final List<Wallet> wallets, final File chainFile, 
			final DownloadProgressTracker listener, final NativePeerEventListener peerListener) {
		
		try {
			NetworkParameters params = configuration.getNetworkParameters();

			BlockStore chainStore = new SPVBlockStore(params, chainFile);
			
			for (Wallet wallet : wallets) {
				
				if ((wallet.getLastBlockSeenHeight() < 1) &&
						(openStream(params) != null)) {

					try {
						
						CheckpointManager.checkpoint(params, openStream(params), chainStore,
								configuration.getCreationTime());
						
						StoredBlock head = chainStore.getChainHead();
						LOGGER.info("Skipped to checkpoint " + head.getHeight() + " at "
		                         + Utils.dateTimeFormat(head.getHeader().getTimeSeconds() * 1000));
					
					} catch (Throwable t) {
		
						LOGGER.warn("Problem using checkpoints", t);
		
					}

					break;
				}
				
			}
			
			BlockChain chain = new BlockChain(params, wallets, chainStore);
			
			final PeerGroup peerGroup = new PeerGroup(params, chain);
			peerGroup.setUserAgent("UNIQUID", "0.1");
			peerGroup.setMaxPeersToDiscoverCount(3);
			peerGroup.setMaxConnections(2);

			if (params.getDnsSeeds() != null &&
					params.getDnsSeeds().length > 0) {
				peerGroup.addPeerDiscovery(new DnsDiscovery(params));
			} else if (params.getAddrSeeds() != null &&
						params.getAddrSeeds().length > 0) {
							peerGroup.addPeerDiscovery(new SeedPeers(params.getAddrSeeds(), params));
			} else {
				throw new Exception("Problem with Peers discovery!");
			}

			LOGGER.info("BLOCKCHAIN Preparing to download blockchain...");
			
			peerGroup.addConnectedEventListener(peerListener);
			peerGroup.addDisconnectedEventListener(peerListener);
			peerGroup.addDiscoveredEventListener(peerListener);
			peerGroup.start();
			peerGroup.startBlockChainDownload(listener);
			listener.await();
			peerGroup.stop();
			chainStore.close();
			
			LOGGER.info("BLOCKCHAIN downloaded.");

		} catch (Exception ex) {

			LOGGER.error("Exception catched ", ex);

		}
	}
	
	/**
	 * Synchronize a single of {@code Wallet} against the BlockChain.
	 * 
	 * @param params the NetworkParameters to use
	 * @param wallet the Wallet to synchronize
	 * @param chainFile the chain file to use
	 * @param listener the listener to inform for status changes
	 */
	public static void syncBlockChain(UniquidNodeConfiguration configuration, final Wallet wallet, final File chainFile, 
			final DownloadProgressTracker listener, final NativePeerEventListener peerListener) {

		syncBlockChain(configuration, Arrays.asList(new Wallet[] { wallet }), chainFile, listener, peerListener);

	}
	
	/**
	 * Broadcast a transaction to the peer to peer network and return the txid
	 * 
	 * @param params the NetworkParameters to use
	 * @param sendRequest the transaction to broadcast
	 * @return a string representation of the txid
	 * @throws ExecutionException in case a problem occurs
	 */
	public static String sendTransaction(NetworkParameters params, final SendRequest sendRequest)
			throws ExecutionException {

		PeerGroup peerGroup = null;

		try {

			peerGroup = new PeerGroup(params);
			peerGroup.setUserAgent("UNIQUID", "0.1");
			peerGroup.setMaxPeersToDiscoverCount(3);
			peerGroup.setMaxConnections(2);

			if (params.getDnsSeeds() != null &&
					params.getDnsSeeds().length > 0) {
				peerGroup.addPeerDiscovery(new DnsDiscovery(params));
			} else if (params.getAddrSeeds() != null &&
						params.getAddrSeeds().length > 0) {
							peerGroup.addPeerDiscovery(new SeedPeers(params.getAddrSeeds(), params));
			} else {
				throw new Exception("Problem with Peers discovery!");
			}
			
			LOGGER.info("BLOCKCHAIN Preparing to send TX...");

			peerGroup.start();

			Transaction t = peerGroup.broadcastTransaction(sendRequest.tx, 2).future().get(2, TimeUnit.MINUTES);
			
			return t.getHashAsString();

		} catch (Throwable t) {
			
			LOGGER.error("Catched throwable", t);
			
			throw new ExecutionException("Problem sending transaction: " + t.getMessage(), t);
			
		} finally {

			if (peerGroup != null) {
				peerGroup.stop();
			}
			
		}
	}

	private static InputStream openStream(NetworkParameters params) {
        return NodeUtils.class.getResourceAsStream("/" + params.getId() + ".uniquidcheckpoints.txt");
    }
	
	/**
	 * Returns a {@code BitSet} representing the bitmask parameter
	 * @param bitmask the bitmask to transform
	 * @return a {@code BitSet} representing the bitmask parameter
	 */
	public static BitSet toBitset(String bitmask) {

		byte[] bitset = Hex.decode(bitmask);
		return BitSet.valueOf(Arrays.copyOfRange(bitset, 1, bitset.length));

	}

	/**
	 * Calculates the imprint address from a node's public key
	 * 
	 * @param xpub the node's public key 
	 * @param networkParameters the network parameters to use
	 * @return the imprint address of the node
	 */
	public static Address calculateImprintAddress(final String xpub, final NetworkParameters networkParameters) {

		final DeterministicKey deterministicKey = DeterministicKey.deserializeB58(xpub, networkParameters);

		return calculateImprintAddress(deterministicKey, networkParameters);

	}

	/**
	 * Calculates the imprint address from a node's deterministic key
	 * 
	 * @param deterministicKey the node's deterministicKey key 
	 * @param networkParameters the network parameters to use
	 * @return the imprint address of the node
	 */
	public static Address calculateImprintAddress(final DeterministicKey deterministicKey, final NetworkParameters networkParameters) {

		final DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(deterministicKey);

		List<ChildNumber> imprintingChild = null;
		if (deterministicKey.getDepth() == 2) {

			/* M/44'/0' node tpub */

			imprintingChild = ImmutableList.of(new ChildNumber(0, false), new ChildNumber(0, false),
					new ChildNumber(0, false), new ChildNumber(0, false));

		} else if (deterministicKey.getDepth() == 3) {

			/* M/44'/0'/X context tpub */
			imprintingChild = ImmutableList.of(new ChildNumber(0, false), new ChildNumber(0, false),
					new ChildNumber(0, false));
		}

		DeterministicKey imprintingKey = deterministicHierarchy.get(imprintingChild, true, true);
		return imprintingKey.toAddress(networkParameters);

	}
	
	/**
	 * Allow to return an ImmutableList<ChildNumber> from a string represeting path.
	 * 
	 * @param path
	 * @return
	 */
	public static ImmutableList<ChildNumber> listFromPath(String path) {
		
		// Remove M/ prefix
		if (path.startsWith("M/")) {
			
			path = path.substring(2);
			
		}
		
		StringTokenizer tokenizer = new StringTokenizer(path, "/");
		
		List<ChildNumber> start = new ArrayList<ChildNumber>();
		
		start.add(new ChildNumber(44, true));
		start.add(new ChildNumber(0, true));
		start.add(new ChildNumber(0, false));
		
		while (tokenizer.hasMoreTokens()) {
			
			String next = tokenizer.nextToken();
			
			start.add(new ChildNumber(Integer.valueOf(next), false));
			
		}
		
		return ImmutableList.copyOf(start);
		
	}

}
