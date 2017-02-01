package com.uniquid.node;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.google.common.collect.ImmutableList;
import com.uniquid.node.event.NodeEventListener;
import com.uniquid.node.state.NodeState;
import com.uniquid.node.state.NodeStateContext;
import com.uniquid.node.state.impl.InitializingState;
import com.uniquid.node.state.impl.ReadyState;
import com.uniquid.node.utils.NodeUtils;
import com.uniquid.node.utils.WalletUtils;
import com.uniquid.register.RegisterFactory;

/**
 * This class represents an Uniquid Node: an entity that have wallets and a state
 * 
 * @author giuseppe
 *
 */
public class UniquidNode implements NodeStateContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNode.class.getName());
	
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    
    public static ImmutableList<ChildNumber> BIP44_ACCOUNT_PROVIDER = ImmutableList.of(
    		new ChildNumber(44, true),
    		new ChildNumber(0, true),
    		new ChildNumber(0, false),
    		new ChildNumber(0, false)
    	);
    
    public static ImmutableList<ChildNumber> BIP44_ACCOUNT_USER = ImmutableList.of(
    		new ChildNumber(44, true),
    		new ChildNumber(0, true),
    		new ChildNumber(0, false),
    		new ChildNumber(1, false)
    	);
    
    public static ImmutableList<ChildNumber> BIP44_ACCOUNT_ORCHESTRATOR = ImmutableList.of(
    		new ChildNumber(44, true),
    		new ChildNumber(0, true),
    		new ChildNumber(0, false),
    		new ChildNumber(2, false),
    		new ChildNumber(0, false)
    	);
    
    
    /** The current state of this Node */
    private NodeState nodeState;

	private NetworkParameters networkParameters;
	private File providerFile;
	private File userFile;
	private File chainFile;
	private File revokeFile;
	private Wallet providerWallet;
	private Wallet userWallet;
	private Wallet revokeWallet;
	
	private Address imprintingAddress;
	private String publicKey;
	private String  machineName;

	private RegisterFactory registerFactory;

	private UniquidNode(Builder builder) throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		this.networkParameters = builder._params;
		this.providerFile = builder._providerFile;
		this.userFile = builder._userFile;
		this.chainFile = builder._chainFile;
		this.revokeFile = builder._revokeFile;
		this.providerWallet = builder._providerWallet;
		this.userWallet = builder._userWallet;
		this.revokeWallet = builder._revokeWallet;
		this.registerFactory = builder._registerFactory;
		this.machineName = builder._machineName;
		
	}
	
	@Override
	public synchronized void setNodeState(NodeState nodeState) {
		this.nodeState = nodeState;
	}
	
	@Override
	public Wallet getProviderWallet() {
		return providerWallet;
	}
	
	@Override
	public Wallet getRevokeWallet() {
		return revokeWallet;
	}
	
	@Override
	public Wallet getUserWallet() {
		return userWallet;
	}
	
	@Override
	public NetworkParameters getNetworkParameters() {
		return networkParameters;
	}
	
	@Override
	public RegisterFactory getRegisterFactory() {
		return registerFactory;
	}
	
	public Address getImprintingAddress() {
		return imprintingAddress;
	}

	public String getPublicKey() {
		return publicKey;
	}
	
	public String  getMachineName() {
		return machineName;
	}

	public void initNode() throws Exception {
		
		byte[] bytes;
		long creationTime;
		
		if (providerFile.exists() && !providerFile.isDirectory() &&
				userFile.exists() && !userFile.isDirectory()) {

			// Wallets already present!
			providerWallet = Wallet.loadFromFile(providerFile);
			userWallet = Wallet.loadFromFile(userFile);
			revokeWallet = Wallet.loadFromFile(revokeFile);
			
			bytes = providerWallet.getKeyChainSeed().getSeedBytes();
			creationTime = providerWallet.getKeyChainSeed().getCreationTimeSeconds();
			
		} else {
			
			SecureRandom random = new SecureRandom();
			bytes = new byte[32];
			random.nextBytes(bytes);
			creationTime = System.currentTimeMillis() / 1000;
//			bytes = Hex.decode("6b9a445aec588ae54798379e68987c97edc1502d6f6c328bd8021346e4c4727c");
//			creationTime = 1485939601;
			
			// Create a new provider wallet
			providerWallet = Wallet.fromSeed(networkParameters,
					NodeUtils.createDeterministicSeed(bytes, creationTime), UniquidNode.BIP44_ACCOUNT_PROVIDER);
			
			// Create a new user wallet
			userWallet = Wallet.fromSeed(networkParameters,
					NodeUtils.createDeterministicSeed(bytes, creationTime), UniquidNode.BIP44_ACCOUNT_USER);
			
			// Create provider revoke watching wallet
			revokeWallet = new Wallet(networkParameters);
			
			providerWallet.saveToFile(providerFile);
			userWallet.saveToFile(userFile);
			revokeWallet.saveToFile(revokeFile);
			
			//providerRevokeWallet.addWatchedAddress(Address.fromBase58(networkParameters, "mxUQnt3f3H28qvovdMwyKW1Gz7WjzaJLCc"));
			
		}
		
		// Calculate public info
		calculatePublicInfo(bytes, creationTime);
		
		// Retrieve contracts
		Set<Transaction> transactions = providerWallet.getTransactions(false);
		
		// If there is at least 1 contract, then we are ready
		if (transactions.size() > 0) {

			// Jump to ready state
			setNodeState(new ReadyState(this, imprintingAddress));

		} else {

			// Jump to initializing
			setNodeState(new InitializingState(this, imprintingAddress));

		}
		
		// Create node event listner
		NodeEventListener nodeEventListner = new NodeEventListener(this);
		
		// Add event listeners
		providerWallet.addCoinsReceivedEventListener(nodeEventListner);
		providerWallet.addCoinsSentEventListener(nodeEventListner);
		userWallet.addCoinsReceivedEventListener(nodeEventListner);
		userWallet.addCoinsSentEventListener(nodeEventListner);
		//providerRevokeWallet.addChangeEventListener(nodeEventListner);
		revokeWallet.addCoinsSentEventListener(nodeEventListner);
		
		// First BC sync
		NodeUtils.syncBlockChain(networkParameters, Arrays.asList(new Wallet[] { providerWallet, userWallet }), chainFile);
		
		// Update revoke
		NodeUtils.syncBlockChain(networkParameters, revokeWallet, chainFile);
		
		try {
			providerWallet.saveToFile(providerFile);
			userWallet.saveToFile(userFile);
			revokeWallet.saveToFile(revokeFile);
		} catch (Exception ex) {
			LOGGER.error("Exception while saving wallets");
		}
		//DONE INITIALIZATION
	}
	
	private void calculatePublicInfo(byte[] bytes, long creationTime) {
		
		LOGGER.info("HEX seed " + Hex.toHexString(bytes) + "; creation time " + creationTime);
		
		DeterministicKey deterministicKey = NodeUtils.createDeterministicKeyFromByteArray(bytes);
		
		//LOGGER.info("START_NODE tpriv: " + deterministicKey.serializePrivB58(networkParameters));
		//LOGGER.info("START_NODE tpub: " + deterministicKey.serializePubB58(networkParameters));
		
		DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(deterministicKey);
		
		ImmutableList<ChildNumber> IMPRINTING_PATH = ImmutableList.of(
	    		new ChildNumber(44, true),
	    		new ChildNumber(0, true)
	    	);
		
		DeterministicKey imprintingKey = deterministicHierarchy.get(IMPRINTING_PATH, true, true);
		LOGGER.info("Imprinting key tpub: " + imprintingKey.serializePubB58(networkParameters));
		
		publicKey = imprintingKey.serializePubB58(networkParameters);
		
		ImmutableList<ChildNumber> PROVIDER_IMPRINTING_ADDRESS = ImmutableList.of(
	    		new ChildNumber(44, true),
	    		new ChildNumber(0, true),
	    		new ChildNumber(0, false),
	    		new ChildNumber(0, false),
	    		new ChildNumber(0, false),
	    		new ChildNumber(0, false)
	    	);
		
		DeterministicKey imprintingProviderKey = deterministicHierarchy.get(PROVIDER_IMPRINTING_ADDRESS, true, true);
		imprintingAddress = imprintingProviderKey.toAddress(networkParameters);
		
	}
	
	public synchronized void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		nodeState.onCoinsReceived(wallet, tx, prevBalance, newBalance);
	
	}
	
	public synchronized void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
	
		nodeState.onCoinsSent(wallet, tx, prevBalance, newBalance);
	
	}
	
	public synchronized String getNodeState() {
		return nodeState.toString();
	}
	
	public void startNode() throws Exception {
		
		// This will start BlockChain synchronization
		
		final Runnable walletSyncher = new Runnable() {
			
			public void run() {

				// Synchronize wallets against blockchain
				NodeUtils.syncBlockChain(networkParameters, Arrays.asList(new Wallet[] { providerWallet, userWallet }), chainFile);
				
				// Update revoke
				NodeUtils.syncBlockChain(networkParameters, revokeWallet, chainFile);
				
				try {
					providerWallet.saveToFile(providerFile);
					userWallet.saveToFile(userFile);
					revokeWallet.saveToFile(revokeFile);
				} catch (Exception ex) {
					LOGGER.error("Exception while saving wallets");
				}
			}
		};

		final ScheduledFuture<?> updaterThread = scheduledExecutorService.scheduleWithFixedDelay(walletSyncher, 0, 1,
				TimeUnit.MINUTES);
		
	}
	
	public void stopNode() throws Exception{
		
		scheduledExecutorService.shutdown();
		try {

			scheduledExecutorService.awaitTermination(20, TimeUnit.SECONDS);

		} catch (InterruptedException e) {

			LOGGER.error("Exception while awaiting for termination", e);

		}
		
	}

	public String signTransaction(String s_tx, String path)
			throws BlockStoreException, InterruptedException, ExecutionException, InsufficientMoneyException, Exception {
	
		Transaction originalTransaction = networkParameters.getDefaultSerializer().makeTransaction(Hex.decode(s_tx));

		String transactionToString = Hex.toHexString(originalTransaction.bitcoinSerialize());
		LOGGER.info("Serialized unsigned transaction: " + transactionToString);
		
		SendRequest send = SendRequest.forTx(originalTransaction);
		
		// fix our tx
		WalletUtils.newCompleteTransaction(send, providerWallet, networkParameters);

		String retValue = "";
		if (path.startsWith("0")) {
			
			// delegate to walled the signing
			providerWallet.signTransaction(send);
			
			String sr = Hex.toHexString(originalTransaction.bitcoinSerialize());
		    
			LOGGER.info("Serialized SIGNED transaction: " + sr);
			
			retValue = NodeUtils.sendTransaction(networkParameters, providerWallet, chainFile, send);
		
		} else if (path.startsWith("1")) {
			
			// delegate to walled the signing
			userWallet.signTransaction(send);
			
			String sr = Hex.toHexString(originalTransaction.bitcoinSerialize());
		    
			LOGGER.info("Serialized SIGNED transaction: " + sr);
			
			retValue = NodeUtils.sendTransaction(networkParameters, userWallet, chainFile, send);
			
		}
		
		return retValue;
	}

	public static class Builder {

		private NetworkParameters _params;

		private File _providerFile;
		private File _userFile;
		private File _chainFile;
		private File _revokeFile;

		private Wallet _providerWallet;
		private Wallet _userWallet;
		private Wallet _revokeWallet;

		private RegisterFactory _registerFactory;
		
		private String _machineName;

		public Builder set_params(NetworkParameters _params) {
			this._params = _params;
			return this;
		}

		public Builder set_providerFile(File _providerFile) {
			this._providerFile = _providerFile;
			return this;
		}
		
		public Builder set_userFile(File _userFile) {
			this._userFile = _userFile;
			return this;
		}

		public Builder set_chainFile(File _chainFile) {
			this._chainFile = _chainFile;
			return this;
		}
		
		public Builder set_revokeFile(File _revokeFile) {
			this._revokeFile = _revokeFile;
			return this;
		}
		
		public Builder set_providerWallet(Wallet _providerWallet) {
			this._providerWallet = _providerWallet;
			return this;
		}
		
		public Builder set_userWallet(Wallet _userWallet) {
			this._userWallet = _userWallet;
			return this;
		}
		
		public Builder set_revokeWallet(Wallet _revokeWallet) {
			this._revokeWallet = _revokeWallet;
			return this;
		}

		public Builder set_registerFactory(RegisterFactory _registerFactory) {
			this._registerFactory = _registerFactory;
			return this;
		}
		
		public Builder set_machine_name(String _machineName) {
			this._machineName = _machineName;
			return this;
		}

		public UniquidNode build() throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

			return new UniquidNode(this);

		}
	}
	
}