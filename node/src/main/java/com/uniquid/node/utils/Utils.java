package com.uniquid.node.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class Utils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class.getName());
	
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

	public static Wallet createOrLoadMasterWallet2(String mnemonic, long creationTime, File walletFile,
			NetworkParameters params) throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		Wallet masterWallet;

		if (walletFile.exists() && !walletFile.isDirectory()) {

			masterWallet = Wallet.loadFromFile(walletFile);

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
			LOGGER.info("mnemonicCode: " + org.bitcoinj.core.Utils.join(dSeed.getMnemonicCode()));

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
			masterWallet = Wallet.fromKeys(params, Arrays.asList(key));
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
        System.out.println("mnemonicCode: " + org.bitcoinj.core.Utils.join(seed.getMnemonicCode()));
		
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

			DeterministicSeed dSeed = NodeUtils.createDeterministicSeed("Hulkbuster-Plated Compact Armor", creationTime);

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
	
	public void mnemonicToBase58(String mnemonic, NetworkParameters params) {
		
        //
        byte[] byteSeed = MnemonicCode.toSeed(Splitter.on(" ").splitToList(mnemonic), "");
        
        DeterministicHierarchy detH = createDeterministicHierarchy(byteSeed);

        // M/0H (watching key)
        List<ChildNumber> list = ImmutableList.of(new ChildNumber(0, true));
        DeterministicKey ekprv_0 = detH.get(list, true, true);
        
        ekprv_0.toAddress(params); //mgkyT4e2BU5EVgndzVYZ51rTrzMVFM5ZPx
	}
	
}
