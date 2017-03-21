package com.uniquid.node.impl.utils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.net.discovery.SeedPeers;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.google.common.collect.ImmutableList;

/**
 */
public class NodeUtils {
	
	private static ImmutableList<ChildNumber> IMPRINTING = ImmutableList.of(
            new ChildNumber(44, true),
            new ChildNumber(0, true)
    );

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeUtils.class.getName());
	
	/**
	 * Generate a Deterministic Seed from a string
	 * 
	 * @param seed
	 * @return new deterministic seed
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 */
	public static DeterministicSeed createDeterministicSeed(String seed) throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		return createDeterministicSeed(seed, System.currentTimeMillis() / 1000);

	}
	
	/**
	 * Generate a Deterministic Seed from a string
	 * 
	 * @param seed
	 * @return new deterministic seed
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 */
	public static DeterministicSeed createDeterministicSeed(String seed, long creationTime) throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		md.update(seed.getBytes("UTF-8"));
		byte[] hash = md.digest();
		
		return createDeterministicSeed(hash, creationTime);
	}
	
	/**
	 * Generate a Deterministic Seed from a byte array
	 * 
	 * @param seed
	 * @return new deterministic seed
	 */
	public static DeterministicSeed createDeterministicSeed(byte[] seed, long creationTime) throws UnreadableWalletException {
		return new DeterministicSeed("", seed, "", creationTime);
	}
	
	/**
	 * Create from brain wallet
	 * @param string
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static DeterministicKey createDeterministicKeyFromBrainWallet(String string)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		md.update(string.getBytes("UTF-8"));
		byte[] hash = md.digest();

		return HDKeyDerivation.createMasterPrivateKey(hash);

	}
	
	public static DeterministicKey createDeterministicKeyFromByteArray(byte[] array) {
		return HDKeyDerivation.createMasterPrivateKey(array);
	}
	
	public static DeterministicKey createImprintingKey(DeterministicKey deterministicKey) {
		
		DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(deterministicKey);
		
		return deterministicHierarchy.get(IMPRINTING, true, true);

	}

	/**
	 * Synchronize a list of wallets against blockchain
	 * @param params
	 * @param wallets
	 * @param chainFile
	 * @param creationTime
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
			
			for (Wallet wallet : wallets) {
			
				chain.addWallet(wallet);
				peerGroup.addWallet(wallet);
			
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
	 * Sycnhronize a single wallet against the blockchain
	 * @param params
	 * @param wallet
	 * @param chainFile
	 * @param creationTime
	 */
	public static void syncBlockChain(NetworkParameters params, final Wallet wallet, final File chainFile, final DownloadProgressTracker listener) {

		syncBlockChain(params, Arrays.asList(new Wallet[] { wallet }), chainFile, listener);

	}
	
	/**
	 * Send the transaction to the peers
	 * @param params
	 * @param wallet
	 * @param chainFile
	 * @param sendRequest
	 * @return
	 * @throws BlockStoreException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static String sendTransaction(NetworkParameters params, final Wallet wallet, final File chainFile,
			final SendRequest sendRequest) throws BlockStoreException, InterruptedException, ExecutionException {

		SPVBlockStore chainStore = new SPVBlockStore(params, chainFile);
		PeerGroup peerGroup = null;

		try {

			BlockChain chain = new BlockChain(params, chainStore);
			peerGroup = new PeerGroup(params, chain);
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
			
			chain.addWallet(wallet);
			peerGroup.addWallet(wallet);

			LOGGER.info("BLOCKCHAIN Preparing to send TX...");

			peerGroup.start();

			Transaction t = peerGroup.broadcastTransaction(sendRequest.tx, 2).future().get(2, TimeUnit.MINUTES);
			
			return t.getHashAsString();

		} catch (Throwable t) {
			
			LOGGER.error("Catched throwable", t);
			
			throw new ExecutionException("Problem sending transaction: " + t.getMessage(), t);
			
		} finally {

			peerGroup.stop();
			chainStore.close();

		}
	}

	public static InputStream openStream(NetworkParameters params) {
        return NodeUtils.class.getResourceAsStream("/" + params.getId() + ".uniquidcheckpoints.txt");
    }
	
	public static BitSet toBitset(String bitmask) {

		byte[] bitset = Hex.decode(bitmask);
		return BitSet.valueOf(Arrays.copyOfRange(bitset, 1, bitset.length));

	}

}
