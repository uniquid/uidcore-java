package com.uniquid.node.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.TransactionConfidence.Listener;
import org.bitcoinj.core.TransactionConfidence.Listener.ChangeReason;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.script.Script;
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
import com.uniquid.node.state.NodeStateContext;
import com.uniquid.node.state.impl.ReadyState;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

public class Utils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class.getName());
	
	private static final String CONTRACT_FUNCTION = "000000400000000000000000000000000000";
	
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
	
	public static void makeUserContract(Wallet wallet, Transaction tx, NetworkParameters networkParameters, NodeStateContext nodeStateContext) {
		
		LOGGER.info("Creating contract...");
		
		// Transaction already confirmed
		if (tx.getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)) {
			
			LOGGER.info("tx.getConfidence is BUILDING...");
			
			doUserContract(wallet, tx, networkParameters, nodeStateContext);
			
			LOGGER.info("Done creating contract");
			
		} else {
			
			LOGGER.info("tx.getConfidence is not BUILDING: " + tx.getConfidence() + ", registering a listener");
			
			final Listener listener = new Listener() {

				@Override
				public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {
					
					LOGGER.info("tx.getConfidence is changed: " + confidence);

					try {
						
						if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING) && reason.equals(ChangeReason.TYPE)) {
					
							LOGGER.info("tx.getConfidence is BUILDING...");
							
							doUserContract(wallet, tx, networkParameters, nodeStateContext);
							
							tx.getConfidence().removeEventListener(this);
							
							LOGGER.info("Contract Done!");
							
						} else if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.DEAD) && reason.equals(ChangeReason.TYPE)) {
							
							LOGGER.error("Something bad happened! TRansaction is DEAD!");
							
							tx.getConfidence().removeEventListener(this);
							
						} else {
							
							LOGGER.warn("Unexpected tx.getConfidence..");
						}
					
					} catch (Exception ex) {
						
						LOGGER.error("Exception while populating Register", ex);
						
					}
					
				}
				
			};
			
			// Transaction not yet confirmed! Register callback!
			tx.getConfidence().addEventListener(listener);
		}
		
	}
	
	/**
	 * Create contract
	 * @param tx
	 * @param networkParameters
	 * @param nodeStateContext
	 */
	private static void doUserContract(Wallet wallet, Transaction tx, NetworkParameters networkParameters, NodeStateContext nodeStateContext) {
		
//		List<Address> addresses = wallet.getIssuedReceiveAddresses();
					
//		List<DeterministicKey> keys = wallet.getActiveKeyChain().getLeafKeys();
//		List<Address> addresses2 = new ArrayList<>();
//		for (ECKey key : keys) {
//			addresses2.add(key.toAddress(networkParameters));
//		}
		
		List<TransactionOutput> to = tx.getOutputs();
		
		if (to.size() != 4) {
			LOGGER.error("Contract not valid! size is not 4");
			return;
		}

		Script script = tx.getInput(0).getScriptSig();
		Address p_address = new Address(networkParameters, org.bitcoinj.core.Utils.sha256hash160(script.getPubKey()));

		List<TransactionOutput> ts = new ArrayList<>(to);

		Address u_address = ts.get(0).getAddressFromP2PKHScript(networkParameters);
		
//		if (u_address == null || !addresses2.contains(u_address)) {
//			// is u_address one of our user addresses?
//			LOGGER.error("Contract not valid! User address is null or we are not the user");
//			return;
//		}
		
//		wallet.isPubKeyHashMine(ts.get(0).getScriptPubKey().getPubKeyHash());
		if (u_address == null || !wallet.isPubKeyHashMine(u_address.getHash160())) {
			// is u_address one of our user addresses?
			LOGGER.error("Contract not valid! User address is null or we are not the user");
			return;
		}

		if (!WalletUtils.isValidOpReturn(tx)) {
			LOGGER.error("Contract not valid! OPRETURN not valid");
			return;
		}

		Address revoke = ts.get(2).getAddressFromP2PKHScript(networkParameters);
		if(revoke == null /*|| !WalletUtils.isUnspent(tx.getHashAsString(), revoke.toBase58())*/){
			LOGGER.error("Contract not valid! Revoke address is null");
			return;
		}
		
		String providerName = WalletUtils.retrieveNameFromProvider(p_address.toBase58());
		if (providerName == null) {
			LOGGER.error("Contract not valid! Provider name is null");
			return;
		}

		UserChannel userChannel = new UserChannel();
		userChannel.setProviderAddress(p_address.toBase58());
		userChannel.setUserAddress(u_address.toBase58());
		userChannel.setProviderName(providerName);
		userChannel.setRevokeAddress(revoke.toBase58());
		userChannel.setRevokeTxId(tx.getHashAsString());
		
		String opreturn = WalletUtils.getOpReturn(tx);
		
		byte[] op_to_byte = Hex.decode(opreturn);
		
		byte[] bitmask = Arrays.copyOfRange(op_to_byte, 1, 19);
		
		// encode to be saved on db
		String bitmaskToString = new String(Hex.encode(bitmask));
		
		userChannel.setBitmask(bitmaskToString);
		
		try {

			UserRegister userRegister = nodeStateContext.getRegisterFactory().createUserRegister();
			
			userRegister.insertChannel(userChannel);
			
		} catch (Exception e) {

			LOGGER.error("Exception while inserting userChannel", e);

		}
		
		// We need to watch the revoked address
		nodeStateContext.getRevokeWallet().addWatchedAddress(revoke);

	}
	
	public static void makeProviderContract(Wallet wallet, Transaction tx, NetworkParameters networkParameters, NodeStateContext nodeStateContext) {
		
		LOGGER.info("Creating contract...");
		
		// Transaction already confirmed
		if (tx.getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)) {
			
			LOGGER.info("tx.getConfidence is BUILDING...");
			
			doProviderContract(wallet, tx, networkParameters, nodeStateContext);
			
			LOGGER.info("Done creating contract");
			
		} else {
			
			LOGGER.info("tx.getConfidence is not BUILDING: " + tx.getConfidence() + ", registering a listener");
			
			final Listener listener = new Listener() {

				@Override
				public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {
					
					LOGGER.info("tx.getConfidence is changed: " + confidence);

					try {
						
						if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING) && reason.equals(ChangeReason.TYPE)) {
					
							LOGGER.info("tx.getConfidence is BUILDING...");
							
							doProviderContract(wallet, tx, networkParameters, nodeStateContext);
							
							tx.getConfidence().removeEventListener(this);
							
							LOGGER.info("Contract Done!");
							
						} else if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.DEAD) && reason.equals(ChangeReason.TYPE)) {
							
							LOGGER.error("Something bad happened! TRansaction is DEAD!");
							
							tx.getConfidence().removeEventListener(this);
							
						} else {
							
							LOGGER.warn("Unexpected tx.getConfidence..");
						}
					
					} catch (Exception ex) {
						
						LOGGER.error("Exception while populating Register", ex);
						
					}
					
				}
				
			};
			
			// Transaction not yet confirmed! Register callback!
			tx.getConfidence().addEventListener(listener);
		}
		
	}
	
	private static void doProviderContract(Wallet wallet, Transaction tx, NetworkParameters networkParameters, NodeStateContext nodeStateContext) {
		
//		List<Address> addresses = wallet.getIssuedReceiveAddresses();
		
//		List<DeterministicKey> keys = wallet.getActiveKeyChain().getLeafKeys();
//		List<Address> addresses2 = new ArrayList<>();
//		for (ECKey key : keys) {
//			addresses2.add(key.toAddress(networkParameters));
//		}
		
		List<TransactionOutput> to = tx.getOutputs();
		
		if (to.size() != 4) {
			LOGGER.error("Contract not valid! size is not 4");
			return;
		}

		Script script = tx.getInput(0).getScriptSig();
		Address p_address = new Address(networkParameters, org.bitcoinj.core.Utils.sha256hash160(script.getPubKey()));

//		if (/*!addresses.contains(p_address) ||*/ !addresses2.contains(p_address)) {
//			LOGGER.error("Contract not valid! We are not the provider");
//			return;
//		}
		
		if (!wallet.isPubKeyHashMine(p_address.getHash160())) {
			LOGGER.error("Contract not valid! We are not the provider");
			return;
		}
		
		List<TransactionOutput> ts = new ArrayList<>(to);

		Address u_address = ts.get(0).getAddressFromP2PKHScript(networkParameters);

		// We are provider!!!
		if (u_address == null) {
			LOGGER.error("Contract not valid! User address is null");
			return;
		}

		if (!WalletUtils.isValidOpReturn(tx)) {
			LOGGER.error("Contract not valid! OPRETURN not valid");
			return;
		}

		Address revoke = ts.get(2).getAddressFromP2PKHScript(networkParameters);
		if(revoke == null /*|| !WalletUtils.isUnspent(tx.getHashAsString(), revoke.toBase58())*/){
			LOGGER.error("Contract not valid! Revoke address is null");
			return;
		}

		ProviderChannel providerChannel = new ProviderChannel();
		providerChannel.setProviderAddress(p_address.toBase58());
		providerChannel.setUserAddress(u_address.toBase58());
		providerChannel.setRevokeAddress(revoke.toBase58());
		providerChannel.setRevokeTxId(tx.getHashAsString());
		
		String opreturn = WalletUtils.getOpReturn(tx);
		
		byte[] op_to_byte = Hex.decode(opreturn);
		
		byte[] bitmask = Arrays.copyOfRange(op_to_byte, 1, 19);
		
		// encode to be saved on db
		String bitmaskToString = new String(Hex.encode(bitmask));
		
		providerChannel.setBitmask(bitmaskToString);
		
		try {

			ProviderRegister providerRegister = nodeStateContext.getRegisterFactory().createProviderRegister();
			
			List<ProviderChannel> channels = providerRegister.getAllChannels();
			
			// If this is the first "normal" contract then remove the imprinting contract
			if (channels.size() == 1 &&
					channels.get(0).getRevokeAddress().equals("IMPRINTING") ) {
				
				providerRegister.deleteChannel(channels.get(0));
				
			}
			
			providerRegister.insertChannel(providerChannel);
			
		} catch (Exception e) {

			LOGGER.error("Exception while inserting providerregister", e);

		}
		
		// We need to watch the revoked address
		nodeStateContext.getRevokeWallet().addWatchedAddress(revoke);

	}
	
	public static void makeImprintContract(Transaction tx, NetworkParameters networkParameters, NodeStateContext nodeStateContext, Address imprintingAddress) throws Exception {
		
		// Transaction already confirmed
		if (tx.getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)) {
			
			doImprint(tx, networkParameters, nodeStateContext, imprintingAddress);
			
			// DONE
			
		} else {
			
			final Listener listener = new Listener() {

				@Override
				public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {

					try {
						
						if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING) && reason.equals(ChangeReason.TYPE)) {
					
							doImprint(tx, networkParameters, nodeStateContext, imprintingAddress);
							
							tx.getConfidence().removeEventListener(this);
							
							LOGGER.info("Imprinting Done!");
							
						} else if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.DEAD) && reason.equals(ChangeReason.TYPE)) {
							
							LOGGER.error("Something bad happened! TRansaction is DEAD!");
							
							tx.getConfidence().removeEventListener(this);
							
						}
					
					} catch (Exception ex) {
						
						LOGGER.error("Exception while populating Register", ex);
						
					}
					
				}
				
			};
			
			// Transaction not yet confirmed! Register callback!
			tx.getConfidence().addEventListener(listener);
			
		}
		
	}
	
	private static void doImprint(Transaction tx, NetworkParameters networkParameters, NodeStateContext nodeStateContext, Address imprintingAddress) throws Exception {
		
		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();
		
		// Check output
		List<TransactionOutput> transactionOutputs = tx.getOutputs();
		for (TransactionOutput to : transactionOutputs) {
			Address address = to.getAddressFromP2PKHScript(networkParameters);
			if (address != null && address.equals(imprintingAddress)) {
				
				// This is our imprinter!!!
				
				ProviderRegister providerRegister = nodeStateContext.getRegisterFactory().createProviderRegister();
				
				ProviderChannel providerChannel = new ProviderChannel();
				providerChannel.setUserAddress(sender);
				providerChannel.setProviderAddress(imprintingAddress.toBase58());
				providerChannel.setBitmask(CONTRACT_FUNCTION);
				providerChannel.setRevokeAddress("IMPRINTING");
				providerChannel.setRevokeTxId(tx.getHashAsString());
				
				providerRegister.insertChannel(providerChannel);
				
				// We can move now to ReadyState
				nodeStateContext.setNodeState(new ReadyState(nodeStateContext, imprintingAddress));
				
				LOGGER.info("Machine IMPRINTED!");
				
				break;

			}
			
		}
	}
	
	public static void revokeContract(Wallet wallet, Transaction tx, NetworkParameters networkParameters, NodeStateContext nodeStateContext) {
		String txid = tx.getHashAsString();
		
		ProviderRegister providerRegister;
		try {
			providerRegister = nodeStateContext.getRegisterFactory().createProviderRegister();
			ProviderChannel channel = providerRegister.getChannelByRevokeTxId(txid);
			
			
			if (channel != null) {
				LOGGER.info("Found a contract to revoke!");
				// contract revoked
				providerRegister.deleteChannel(channel);
				
				LOGGER.info("Contract revoked! " + channel);
			}
			
		} catch (Exception e) {
			
			LOGGER.error("Exception", e);
			
		}
		
	}
	
	public static boolean isValidImprintingTransaction(Transaction tx, NetworkParameters networkParameters, Address imprintingAddress) {
		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();
		
		// Check output
		List<TransactionOutput> transactionOutputs = tx.getOutputs();
		for (TransactionOutput to : transactionOutputs) {
			Address address = to.getAddressFromP2PKHScript(networkParameters);
			if (address != null && address.equals(imprintingAddress)) {
				return true;
			}
		}

		return false;
	}
	
	public static boolean isValidRevokeContract(Transaction tx, NetworkParameters networkParameters, NodeStateContext nodeStateContext) {
		
		String txid = tx.getHashAsString();
		
		ProviderRegister providerRegister;
		try {
			providerRegister = nodeStateContext.getRegisterFactory().createProviderRegister();
			ProviderChannel channel = providerRegister.getChannelByRevokeTxId(txid);
			
			
			if (channel != null) {
				return true;
			}
			
		} catch (Exception e) {
			
			LOGGER.error("Exception", e);
			
		}
		
		return false;
		
	}
	
}
