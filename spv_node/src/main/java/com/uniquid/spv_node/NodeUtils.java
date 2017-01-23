package com.uniquid.spv_node;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
import org.bitcoinj.net.discovery.SeedPeers;
import org.bitcoinj.params.TestNet2Params;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
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
	
	private static final int APPLIANCE4 = 1441170024;

	public enum FILE_TYPE {
		WALLET_FILE, CHAIN_FILE
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
	 * Generate a new Deterministic Hierarchy starting from seed
	 * 
	 * @param seed
	 * @return deterministic hierarchy
	 */
	public static DeterministicHierarchy createDeterministicHierarchy(byte[] seed) {
		DeterministicKey hdPriv = HDKeyDerivation.createMasterPrivateKey(seed);
		return new DeterministicHierarchy(hdPriv);
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

	public static Wallet createOrLoadMasterWallet2(String mnemonic, long creationTime, File walletFile,
			NetworkParameters params) throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		UniquidWallet masterWallet;

		if (walletFile.exists() && !walletFile.isDirectory()) {

			masterWallet = (UniquidWallet) UniquidWallet.loadFromFile(walletFile);

			LOGGER.info("Master Wallet loaded: " + masterWallet.currentReceiveAddress().toBase58());

		} else {
			
			//DeterministicSeed dSeed = new DeterministicSeed(mnemonic, null, "", creationTime);
//			DeterministicSeed dSeed = createDeterministicSeed("Hulkbuster-Plated Compact Armor");
//			masterWallet = Wallet.fromSeed(params, dSeed);
			
			//DeterministicKey key = DeterministicKey.deserializeB58("tprv8hS9xiSZmRGRPLTjvm46CViQXZPCSy51oqxzurvxH8FaMDwQzhhARX5NTHWWbYHVBRyPav1MpqNEfYZqgKwnfbqbRBj2gV67RNtPsgtDtWM", params);

//			byte[] privKey = Hex.decode("c3683143f3ba4cc2566270f80fa94d565b63c0f0dd143b7dfaac7c6db613fc41");
//			byte[] chainCode = Hex.decode("120bac590d40a7e5eb97286bcb44c3306a0ef79899b047d3a22687170473cd69");
//			
//			
//			BigInteger priv = new BigInteger(1, privKey);
//			//DeterministicKey key =  new DeterministicKey(ImmutableList.of(new ChildNumber(44, true), new ChildNumber(0, true),  new ChildNumber(0, false),  new ChildNumber(0, false)), chainCode, priv, null);
//			
//			DeterministicKey key =  new DeterministicKey(ImmutableList.of(ChildNumber.ZERO_HARDENED), chainCode, priv, null);
//			
////			DeterministicKey key = HDKeyDerivation.createMasterPrivKeyFromBytes(privKey, chainCode);
//			
////			masterWallet = Wallet.fromWatchingKey(params, key);
////			masterWallet = Wallet.fromSeed(params, createDeterministicSeed(key.serializePrivate(params)));
//			
//			masterWallet =  new Uidwallet(params, new KeyChainGroup(params, key));
			
			// NEW TEST CODE
			DeterministicSeed dSeed = NodeUtils.createDeterministicSeed("Hulkbuster-Plated Compact Armor", creationTime);
	        
			LOGGER.info("seed: " + dSeed.toString());
			dSeed.setCreationTimeSeconds(creationTime);
			LOGGER.info("creation time: " + dSeed.getCreationTimeSeconds());
			LOGGER.info("mnemonicCode: " + Utils.join(dSeed.getMnemonicCode()));

			byte[] seed = dSeed.getSeedBytes();

			DeterministicKey hdPriv = HDKeyDerivation.createMasterPrivateKey(seed);
			hdPriv.setCreationTimeSeconds(creationTime);
			LOGGER.info("START_NODE tpriv: " + hdPriv.serializePrivB58(params));

			// Find child M/44'/0'
			List<ChildNumber> imprintingChild = ImmutableList.of(new ChildNumber(44, true), new ChildNumber(0, true));

			DeterministicHierarchy detH = new DeterministicHierarchy(hdPriv);
			DeterministicKey imprinting = detH.get(imprintingChild, true, true);
			LOGGER.info("Imprinting key tpub: " + imprinting.serializePubB58(params));
			LOGGER.info("Imprinting key tpriv: " + imprinting.serializePrivB58(params));
			imprinting = imprinting.dropParent();
			
			ECKey key = ECKey.fromPrivate(imprinting.getPrivKeyBytes());
			masterWallet = UniquidWallet.fromKeys(params, Arrays.asList(key));
			masterWallet.getKeyChainSeed();
			LOGGER.info("isDeterministicUpgradeRequired: " + masterWallet.isDeterministicUpgradeRequired());
//			masterWallet.getImportedKeys().size();
//			masterWallet.getImportedKeys().get(0);
//			masterWallet.upgradeToDeterministic(null);
			// END

		}

		LOGGER.info("Master WALLET created: " + masterWallet.currentReceiveAddress().toBase58());
		LOGGER.info("Master WALLET curent change addr: " + masterWallet.currentChangeAddress().toBase58());
		LOGGER.info("Master WALLET: " + masterWallet.toString());
		
        DeterministicSeed seed = masterWallet.getKeyChainSeed();
        System.out.println("seed: " + seed.toString());

        System.out.println("creation time: " + seed.getCreationTimeSeconds());
        System.out.println("mnemonicCode: " + Utils.join(seed.getMnemonicCode()));
		
		return masterWallet;
	}
	
	public static Wallet createOrLoadWallet(String mnemonic, long creationTime, File walletFile,
			NetworkParameters params) throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		Wallet wallet;

		if (walletFile.exists() && !walletFile.isDirectory()) {

			wallet = Wallet.loadFromFile(walletFile);

			LOGGER.info("Wallet loaded: " + wallet.currentReceiveAddress().toBase58());

		} else {
			
//			DeterministicSeed dSeed = new DeterministicSeed(mnemonic, null, "", creationTime);
//			wallet = Wallet.fromSeed(params, dSeed);

			DeterministicSeed dSeed = createDeterministicSeed("Hulkbuster-Plated Compact Armor", creationTime);

			byte[] seed = dSeed.getSeedBytes();

			DeterministicKey hdPriv = HDKeyDerivation.createMasterPrivateKey(seed);
			LOGGER.info("START_NODE tpriv: " + hdPriv.serializePrivB58(params));

			// Find child M/44'/0'
			List<ChildNumber> imprintingChild = ImmutableList.of(new ChildNumber(44, true), new ChildNumber(0, true));

			DeterministicHierarchy detH = new DeterministicHierarchy(hdPriv);
			DeterministicKey imprinting = detH.get(imprintingChild, true, true);
			LOGGER.info("Imprinting key tpub: " + imprinting.serializePubB58(params));
			LOGGER.info("Imprinting key tpriv: " + imprinting.serializePrivB58(params));

			DeterministicKey contract_orch = detH.deriveChild(imprintingChild, true, true, new ChildNumber(0, false));
			DeterministicKey machines_key = DeterministicKey.deserializeB58(null,
					contract_orch.dropParent().serializePrivB58(params), params);
			DeterministicHierarchy machines_hierarchy = new DeterministicHierarchy(machines_key);

			DeterministicKey provider_key = machines_hierarchy.get(ImmutableList.of(new ChildNumber(0, false)), true,
					true);
			
			wallet = Wallet.fromWatchingKeyB58(params, provider_key.serializePubB58(params), creationTime,
					ImmutableList.of(new ChildNumber(0, false)));

			LOGGER.info("Provider key tpub: " + provider_key.serializePubB58(params));
			
			
//			DeterministicKeyChain chain = new DeterministicKeyChain(provider_key);
//			wallet.addAndActivateHDChain(chain);
			
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
			
		}

//		LOGGER.info("WALLET created: " + wallet.currentReceiveAddress().toBase58());
//		LOGGER.info("WALLET curent change addr: " + wallet.currentChangeAddress().toBase58());
//		LOGGER.info("WALLET: " + wallet.toString());

		return wallet;
	}
	
	public static void syncBC(NetworkParameters params, final Wallet wallet, final File chainFile,
			final File walletFile, long creationTime) {

		try {

			BlockStore chainStore = new SPVBlockStore(params, chainFile);
			
			// User Checkpoint only during wallet creation
			if (wallet.getLastBlockSeenHeight() == 0) {
				try {
					
					CheckpointManager.checkpoint(params, openStream(params), chainStore,
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
//			peerGroup.addPeerDiscovery(new DnsDiscovery(params));
			
			int[] appliance4Addr = new int[] { APPLIANCE4 };
			peerGroup.addPeerDiscovery(new SeedPeers(appliance4Addr, params));
			
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

			LOGGER.info("BLOCKCHAIN Preparing to send TX...");

			peerGroup.start();

			Transaction t = peerGroup.broadcastTransaction(sendRequest.tx).future().get(1, TimeUnit.MINUTES);
			
			return t.getHashAsString();

		} catch (Throwable t) {
			
			LOGGER.error("Catched throwable", t);
			
			throw new ExecutionException("Problem sending transaction: " + t.getMessage(), t);
			
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
	
	public static InputStream openStream(NetworkParameters params) {
        return NodeUtils.class.getResourceAsStream("/" + params.getId() + ".checkpoints.txt");
    }
	
	public static Wallet createOrLoadMasterWallet(String seed, long creationTime, File walletFile,
			NetworkParameters params) throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {
		
		Wallet wallet;

		if (walletFile.exists() && !walletFile.isDirectory()) {

			wallet = Wallet.loadFromFile(walletFile);

			LOGGER.info("Master Wallet loaded: " + wallet.currentReceiveAddress().toBase58());

		} else {
			
			ImmutableList<ChildNumber> BIP44_ACCOUNT =
					ImmutableList.of(new ChildNumber(44, true), new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(0, false));
		
//			byte[] privKey = Hex.decode("c3683143f3ba4cc2566270f80fa94d565b63c0f0dd143b7dfaac7c6db613fc41");
//			byte[] chainCode = Hex.decode("120bac590d40a7e5eb97286bcb44c3306a0ef79899b047d3a22687170473cd69");
			
//			masterWallet = Wallet.fromPrivateKeyAndChainCodeBytes(params, privKey, chainCode, creationTime, ImmutableList.of(new ChildNumber(44, true), new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(0, false)));
			
//			masterWallet = Wallet.fromBase58EncodedKey(params, "tprv8ZgxMBicQKsPdPW6CeDyE5561CPim5MTHScZWqrzziHb1MSebm9UDcbhihjaUFyMBByVb6XT5cdnfHHKaiZCJm2LwVhbrTHJQiBWMSEu4qG", creationTime, ImmutableList.of(new ChildNumber(44, true), new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(0, false)));
			
//			masterWallet = Wallet.fromSeed(params, createDeterministicSeed("Hulkbuster-Plated Compact Armor", creationTime), 
//					ImmutableList.of(new ChildNumber(44, true)));
			
			wallet = Wallet.fromSeed(params, createDeterministicSeed(seed, creationTime), BIP44_ACCOUNT);
//			masterWallet.addWatchedAddress(Address.fromBase58(params, "mgVinrbVtdYjBxRNbmwincYdQ27CwprfSf"));
//			masterWallet.addWatchedAddress(Address.fromBase58(params, "moqaUPafQpAtoV5REq93BXbL24b9seG2jJ"));
			
//			masterWallet.addAndActivateHDChain(new DeterministicKeyChain(createDeterministicSeed("Hulkbuster-Plated Compact Armor", creationTime), 
//					));
		}
		
//		LOGGER.info("WALLET created: " + masterWallet.currentReceiveAddress().toBase58());
//		LOGGER.info("WALLET curent change addr: " + masterWallet.currentChangeAddress().toBase58());
//		LOGGER.info("WALLET: " + masterWallet.toString());
		
		return wallet;
		
	}
	
	public static Wallet createOrLoadUserWallet(String seed, long creationTime, File walletFile,
			NetworkParameters params) throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {
		
		Wallet wallet;

		if (walletFile.exists() && !walletFile.isDirectory()) {

			wallet = Wallet.loadFromFile(walletFile);

			LOGGER.info("Wallet loaded: " + wallet.currentReceiveAddress().toBase58());

		} else {
			
			ImmutableList<ChildNumber> BIP44_ACCOUNT =
					ImmutableList.of(new ChildNumber(44, true), new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(1, false));
		
			wallet = Wallet.fromSeed(params, createDeterministicSeed(seed, creationTime), BIP44_ACCOUNT);
		}
		
//		LOGGER.info("WALLET created: " + masterWallet.currentReceiveAddress().toBase58());
//		LOGGER.info("WALLET curent change addr: " + masterWallet.currentChangeAddress().toBase58());
//		LOGGER.info("WALLET: " + masterWallet.toString());
		
		return wallet;
		
	}
	
	public static void main(String[] args) throws Exception {
		NetworkParameters params = new  TestNet3Params().get();
		
		long creation = 1477958400;
		
		Wallet wallet = createOrLoadMasterWallet("", creation, null, params);
	}

}
