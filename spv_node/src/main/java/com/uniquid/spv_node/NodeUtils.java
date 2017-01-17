package com.uniquid.spv_node;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.script.ScriptBuilder;
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
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Created by beatriz on 11/30/2016 for Uniquid Inc.
 */
public class NodeUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeUtils.class.getName());

	public enum FILE_TYPE {
		WALLET_FILE, CHAIN_FILE
	}

	/**
	 * Generate a Deterministic Seed from a byte array
	 * 
	 * @param seed
	 * @return new deterministic seed
	 */
	public static DeterministicSeed createDeterministicSeed(byte[] seed) throws UnreadableWalletException {
		long creationTime = Calendar.getInstance().getTimeInMillis() / 1000;
		return new DeterministicSeed("", seed, "", creationTime);
	}

	/**
	 * Generate a Deterministic Seed from a string
	 * 
	 * @param seed
	 * @return new deterministic seed
	 */
	public static DeterministicSeed createDeterministicSeed(String seed) throws UnreadableWalletException {
		byte[] seedB = seed.getBytes(StandardCharsets.US_ASCII);
		return createDeterministicSeed(seedB);
	}

	/**
	 * Generate a new Deterministic Hierarchy starting from seed
	 * 
	 * @param seed
	 * @return deterministic hierarchy
	 */
	public static DeterministicHierarchy createDeterministicHierarchy(byte[] seed) {
		DeterministicKey hdPriv = HDKeyDerivation.createMasterPrivateKey(seed);
		return new DeterministicHierarchy(hdPriv);
	}

	// /**
	// * Sync the wallet with the blockchain updating chain file and wallet file
	// *
	// * @param wallet - the wallet to sync
	// * @param chainFile - local file of blockchain
	// * @param walletFile - local file of wallet
	// * @throws UnreadableWalletException
	// * @throws BlockStoreException
	// * @throws InterruptedException
	// * @throws Exception
	// * */
	// public static Wallet syncBlockchain(String seed, NetworkParameters
	// params, final File walletFile, final File chainFile) throws
	// UnreadableWalletException, BlockStoreException, InterruptedException {
	//
	// Wallet wallet = createOrLoadWallet(seed, walletFile, params);
	//
	// SPVBlockStore chainStore = new SPVBlockStore(params, chainFile);
	//
	// BlockChain chain = new BlockChain(params, chainStore);
	//
	// final PeerGroup peerGroup = new PeerGroup(params, chain);
	// peerGroup.setUserAgent("Test", "1.0");
	// peerGroup.addPeerDiscovery(new DnsDiscovery(params));
	// chain.addWallet(wallet);
	// peerGroup.addWallet(wallet);
	//
	// DownloadProgressTracker listener = new DownloadProgressTracker(){
	//
	// @Override
	// protected void progress(double pct, int blocksSoFar, Date date) {
	// LOG.info(String.format(Locale.US, "Chain download %d%% done with %d
	// blocks to go, block date %s", (int) pct, blocksSoFar,
	// Utils.dateTimeFormat(date)));
	// }
	//
	// @Override
	// protected void startDownload(final int blocks) {
	// LOG.info("Downloading block chain of size " + blocks + ". " +
	// (blocks > 1000 ? "This may take a while." : ""));
	// }
	//
	// @Override
	// public void doneDownload() {
	// LOG.info("BLOCKCHAIN Blockchain downloaded");
	// }
	// };
	//
	// LOG.info("BLOCKCHAIN Preparing to download blockchain...");
	// peerGroup.start();
	// peerGroup.startBlockChainDownload(listener);
	// listener.await();
	// peerGroup.stop();
	// chainStore.close();
	//
	// return wallet;
	//
	// //
	//// DeterministicSeed dSeed = new DeterministicSeed("cannon side purity
	// jaguar ability people faint vivid high immense nurse model", null, "",
	// 1481551444);
	////
	//// File walletFile = new
	// File(appconfigProperties.getProperty("walletFile"));
	////
	//// WalletAppKit appKit = new WalletAppKit(networkParameters, walletFile,
	// "wallet");
	////
	//// appKit.restoreWalletFromSeed(dSeed);
	////
	//// appKit.startAsync();
	//// appKit.awaitRunning();
	// //
	//
	// }

	public static Wallet createOrLoadWallet(String mnemonic, long creationTime, File walletFile,
			NetworkParameters params) throws UnreadableWalletException {

		Wallet wallet;

		if (walletFile.exists() && !walletFile.isDirectory()) {

			wallet = Wallet.loadFromFile(walletFile);

			LOGGER.info("Wallet loaded: " + wallet.currentReceiveAddress().toBase58());

		} else {

//			DeterministicSeed dSeed = new DeterministicSeed(mnemonic, null, "", creationTime);
////			 wallet = Wallet.fromSeed(params, dSeed);
////
//			byte[] seed = dSeed.getSeedBytes();
////
//			DeterministicKey hdPriv = HDKeyDerivation.createMasterPrivateKey(seed);
//			LOGGER.info("START_NODE tpriv: " + hdPriv.serializePrivB58(params));
////
////			// Find child M/44'/0'
//			List<ChildNumber> imprintingChild = ImmutableList.of(new ChildNumber(44, true), new ChildNumber(0, true));
////
//			DeterministicHierarchy detH = new DeterministicHierarchy(hdPriv);
//			DeterministicKey imprinting = detH.get(imprintingChild, true, true);
//			LOGGER.info("Imprinting key tpub: " + imprinting.serializePubB58(params));
//
//			DeterministicKey contract_orch = detH.deriveChild(imprintingChild, true, true, new ChildNumber(0, false));
//			DeterministicKey machines_key = DeterministicKey.deserializeB58(null,
//					contract_orch.dropParent().serializePrivB58(params), params);
//			DeterministicHierarchy machines_hierarchy = new DeterministicHierarchy(machines_key);
//
//			DeterministicKey provider_key = machines_hierarchy.get(ImmutableList.of(new ChildNumber(0, false)), true,
//					true);
//			wallet = Wallet.fromWatchingKeyB58(params, provider_key.serializePubB58(params), creationTime,
//					ImmutableList.of(new ChildNumber(0, false)));
//
//			LOGGER.info("Provider key tpub: " + provider_key.serializePubB58(params));

			
//			 wallet = Wallet.fromWatchingKeyB58(params,
//			 "tpubDETW28WecmdcvfKznvFzMgMZ5zLRseWL3SJCyXv72DYpGzxvKUC4N4u5PfnPNpsDYGc7AV1izfuf8Ur7PLFPtHa5azVUdV1jshAmBRvHcPZ",
//			 1464739200L,
//			 ImmutableList.of(new ChildNumber(1, false)));
			
//			 wallet = Wallet.fromWatchingKeyB58(params,
//			 "tpubDCNRCdKwJRELd4hAxKEXXbfvdBJFe8vwHcdVaaiESK2TxdMsD7AqSz2guSUGRAff473godtERjPs9K69ksZBcUkEcQrF1od9u7DkoXTK4FF",
//			 1464739200L,
//			 ImmutableList.of(new ChildNumber(0, false)));
			
//			Test con Marco
//			final DeterministicKey detKey = DeterministicKey.deserializeB58(null, "tprv8hS9xiSZmRGRPLTjvm46CViQXZPCSy51oqxzurvxH8FaMDwQzhhARX5NTHWWbYHVBRyPav1MpqNEfYZqgKwnfbqbRBj2gV67RNtPsgtDtWM", params);
//			detKey.setCreationTimeSeconds(1464739200);
//			
//			ECKey eckey = detKey.decompress();
//			
//			List<ECKey> list = new ArrayList<>();
//			list.add(eckey);
//			
//			wallet = Wallet.fromKeys(params, list);
			
			DeterministicSeed detSeed = createDeterministicSeed("Hulkbuster-Plated Compact Armor");
			wallet = Wallet.fromSeed(params, detSeed);
			
		}

		LOGGER.info("WALLET created: " + wallet.currentReceiveAddress().toBase58());
		LOGGER.info("WALLET curent change addr: " + wallet.currentChangeAddress().toBase58());
		LOGGER.info("WALLET: " + wallet.toString());

		return wallet;
	}
	
	public static void syncBC(NetworkParameters params, final Wallet wallet, final File chainFile,
			final File walletFile, long creationTime) {

		try {

			BlockStore chainStore = new SPVBlockStore(params, chainFile);
			
			// User Checkpoint only during wallet creation
			if (wallet.getLastBlockSeenHeight() == 0) {
				try {
					
					CheckpointManager.checkpoint(params, CheckpointManager.openStream(params), chainStore,
							creationTime);
					
					StoredBlock head = chainStore.getChainHead();
					LOGGER.info("Skipped to checkpoint " + head.getHeight() + " at "
	                         + Utils.dateTimeFormat(head.getHeader().getTimeSeconds() * 1000));
				
				} catch (Throwable t) {
	
					LOGGER.error("Exception catched ", t.getMessage());
					LOGGER.error("Cannot use checkpoint");
	
				}
			}
			
			BlockChain chain = new BlockChain(params, wallet, chainStore);
			
			final PeerGroup peerGroup = new PeerGroup(params, chain);
			peerGroup.setUserAgent("UNIQUID", "0.1");
			peerGroup.addPeerDiscovery(new DnsDiscovery(params));
			
			chain.addWallet(wallet);
			peerGroup.addWallet(wallet);

			DownloadProgressTracker listener = new DownloadProgressTracker();

			LOGGER.info("BLOCKCHAIN Preparing to download blockchain...");

			peerGroup.start();
			peerGroup.startBlockChainDownload(listener);
			listener.await();
			peerGroup.stop();
			chainStore.close();

			LOGGER.info("BLOCKCHAIN downloaded.");

		} catch (Exception ex) {

			LOGGER.error("Exception catched ", ex.getMessage());

		}

	}

	public static String sendTransaction(NetworkParameters params, final Wallet wallet, final File chainFile,
			final SendRequest sendRequest) throws BlockStoreException, InterruptedException, ExecutionException {

		SPVBlockStore chainStore = new SPVBlockStore(params, chainFile);
		PeerGroup peerGroup = null;

		try {

			BlockChain chain = new BlockChain(params, chainStore);
			peerGroup = new PeerGroup(params, chain);
			peerGroup.setUserAgent("UNQD", "0.1");
			peerGroup.addPeerDiscovery(new DnsDiscovery(params));
			chain.addWallet(wallet);
			peerGroup.addWallet(wallet);

			LOGGER.info("BLOCKCHAIN Preparing to download blockchain...");

			peerGroup.start();

			peerGroup.broadcastTransaction(sendRequest.tx).future().get();

			LOGGER.info("Send complete, waiting for confirmation");
			sendRequest.tx.getConfidence().getDepthFuture(1).get();

			return sendRequest.tx.getHashAsString();

			// int heightNow = kit.chain().getBestChainHeight();
			// System.out.println("Height after confirmation is " + heightNow);
			// System.out.println("Result: took " + (heightNow - heightAtStart)
			// + " blocks to confirm at this fee level");

			// Futures.addCallback(lf, new FutureCallback<Transaction>() {
			//
			// @Override
			// public void onFailure(Throwable arg0) {
			// // printLog("tx failure");
			// }
			//
			// @Override
			// public void onSuccess(Transaction tx) {
			// // printLog("broadcasted" + tx.toString());
			// String txId = tx.getHashAsString();
			// }
			// });

		} finally {

			peerGroup.stop();
			chainStore.close();

		}
	}

	/**
	 * Create a contract that "connect" two identity
	 */
	public static void createContract(NetworkParameters params, Wallet wallet, PeerGroup peers, String address1,
			String address2) {
		try {

			Address address = Address.fromBase58(params, address1); // Machine
			String s = "0.00135";
			double c = Double.parseDouble(s);
			Coin coins = Coin.valueOf((long) (c * 100000000L));
			Transaction tx = new Transaction(params);
			tx.addOutput(coins, address);
			tx.addOutput(coins, ScriptBuilder.createOpReturnScript(sha256sha256(address2)));

			final SendRequest request = SendRequest.forTx(tx);
			wallet.completeTx(request);

			ListenableFuture<Transaction> futures = peers.broadcastTransaction(request.tx).future();
			Futures.addCallback(futures, new FutureCallback<Transaction>() {

				@Override
				public void onFailure(Throwable arg0) {

					LOGGER.error("Broadcast failed ", arg0);

				}

				@Override
				public void onSuccess(Transaction arg0) {

					LOGGER.info("Broadcasted: " + request.tx.toString());

				}
			});
		} catch (Exception ex) {

			LOGGER.error("Exception ", ex);

		}

	}

	/**
	 * @param str
	 *            the string to encode
	 * @return sha256sha256 of given string
	 */
	private static byte[] sha256sha256(String str) {
		byte[] address = str.getBytes();

		LOGGER.info("address.length = " + address.length);

		byte[] bufaddr = new byte[64];
		Arrays.fill(bufaddr, (byte) 0);
		System.arraycopy(address, 0, bufaddr, 0, address.length);
		byte[] asha256 = Sha256Hash.hashTwice(bufaddr);

		LOGGER.info("sha256256 = " + Hex.toHexString(asha256));

		return asha256;
	}

	public static DeterministicKey DeterministicKeyFromBrainWallet(String string)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		md.update(string.getBytes("UTF-8"));
		byte[] hash = md.digest();

		// Hex.toHexString(hash);

		return HDKeyDerivation.createMasterPrivateKey(hash);

	}

}
