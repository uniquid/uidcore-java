package com.uniquid.node.impl.utils;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.net.discovery.SeedPeers;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

/**
 * NodeUtils contains some static useful methods
 */
public class NodeUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeUtils.class.getName());
	
	/**
	 * Generate a {@code DeterministicSeed} from a byte array and  a creatiom time
	 * 
	 * @param seed byte array representing seed
	 * @param creationTime creation time of seed
	 * @return returns an instance of {@code DeterministicSeed}
	 */
	public static DeterministicSeed createDeterministicSeed(final byte[] seed, final long creationTime) throws UnreadableWalletException {
		return new DeterministicSeed("", seed, "", creationTime);
	}
	
//	/**
//	 * Create from brain wallet
//	 * @param string
//	 * @return
//	 * @throws NoSuchAlgorithmException
//	 * @throws UnsupportedEncodingException
//	 */
//	public static DeterministicKey createDeterministicKeyFromBrainWallet(String string)
//			throws NoSuchAlgorithmException, UnsupportedEncodingException {
//		MessageDigest md = MessageDigest.getInstance("SHA-256");
//
//		md.update(string.getBytes("UTF-8"));
//		byte[] hash = md.digest();
//
//		return HDKeyDerivation.createMasterPrivateKey(hash);
//
//	}
	
	/**
	 * Generates a {@code DeterministicKey} from a byte array seed
	 * 
	 * @param seed the seed represented as byte array
	 * @return returns an instance of {@code DeterministicKey}
	 */
	public static DeterministicKey createDeterministicKeyFromByteArray(byte[] seed) {
		return HDKeyDerivation.createMasterPrivateKey(seed);
	}
	
	/**
	 * Synchronize a list of {@code Wallet} against the BlockChain.
	 * 
	 * @param params the NetworkParameters to use
	 * @param wallets the list of Wallet
	 * @param chainFile the chain file to use
	 * @param listener the listener to inform for status changes
	 */
	public static void syncBlockChain(NetworkParameters params, final List<Wallet> wallets, final File chainFile, final DownloadProgressTracker listener) {
		
		try {

			BlockStore chainStore = new SPVBlockStore(params, chainFile);
			
			for (Wallet wallet : wallets) {
				
				if (wallet.getLastBlockSeenHeight() < 1) {

					try {
						
						CheckpointManager.checkpoint(params, openStream(params), chainStore,
								wallet.getKeyChainSeed().getCreationTimeSeconds());
						
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
	public static void syncBlockChain(NetworkParameters params, final Wallet wallet, final File chainFile, final DownloadProgressTracker listener) {

		syncBlockChain(params, Arrays.asList(new Wallet[] { wallet }), chainFile, listener);

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

}
