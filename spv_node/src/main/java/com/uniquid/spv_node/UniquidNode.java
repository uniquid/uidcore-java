package com.uniquid.spv_node;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.Address;
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
import com.uniquid.register.RegisterFactory;

/**
 * This class represents an Uniquid Node: an entity that have wallets
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
	private File providerRevokeFile;
	private File userRevokeFile;
	private Wallet providerWallet;
	private Wallet userWallet;
	private Wallet providerRevokeWallet;
	private Wallet userRevokeWallet;
	
	private Address imprintingAddress;
	private String publicKey;
	private String  machineName;

	private RegisterFactory registerFactory;

	private UniquidNode(Builder builder) throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		this.networkParameters = builder._params;
		this.providerFile = builder._providerFile;
		this.userFile = builder._userFile;
		this.chainFile = builder._chainFile;
		this.providerWallet = builder._providerWallet;
		this.userWallet = builder._userWallet;
		this.registerFactory = builder._registerFactory;
		this.machineName = builder._machineName;
		this.providerRevokeFile = builder._providerRevokeFile;
		this.userRevokeFile = builder._userRevokeFile;
		
	}
	
	@Override
	public File getProviderFile() {
		return providerFile;
	}

	@Override
	public File getUserFile() {
		return userFile;
	}

	@Override
	public File getChainFile() {
		return chainFile;
	}
	
	@Override
	public File getProviderRevokeFile() {
		return providerRevokeFile;
	}
	
	@Override
	public File getUserRevokeFile() {
		return userRevokeFile;
	}

	@Override
	public void setProviderWallet(Wallet providerWallet) {
		this.providerWallet = providerWallet;
	}

	@Override
	public void setUserWallet(Wallet userWallet) {
		this.userWallet = userWallet;
	}

	@Override
	public void setNodeState(NodeState nodeState) {
		this.nodeState = nodeState;
	}
	
	@Override
	public Wallet getProviderWallet() {
		return providerWallet;
	}
	
	@Override
	public Wallet getProviderRevokeWallet() {
		return providerRevokeWallet;
	}
	
	@Override
	public Wallet getUserWallet() {
		return userWallet;
	}
	
	@Override
	public Wallet getUserRevokeWallet() {
		return userRevokeWallet;
	}

	@Override
	public NetworkParameters getNetworkParameters() {
		return networkParameters;
	}
	
	public NodeState getNodeState() {
		return nodeState;
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
		
		if (providerFile.exists() && !providerFile.isDirectory() &&
				userFile.exists() && !userFile.isDirectory()) {

			// Wallets already present!
			providerWallet = Wallet.loadFromFile(providerFile);
			userWallet = Wallet.loadFromFile(userFile);
			providerRevokeWallet = Wallet.loadFromFile(providerRevokeFile);
			userRevokeWallet = Wallet.loadFromFile(userRevokeFile);
			
			byte[] bytes = providerWallet.getKeyChainSeed().getSeedBytes();
			long creationTime = providerWallet.getKeyChainSeed().getCreationTimeSeconds();
			
			calculatePublicInfo(bytes, creationTime);
			
			// Jump to ready state
			setNodeState(new ReadyState(this, imprintingAddress));
			
		} else {
			
			SecureRandom random = new SecureRandom();
			byte bytes[] = new byte[32];
			random.nextBytes(bytes);
			long creationTime = System.currentTimeMillis() / 1000;
			
			calculatePublicInfo(bytes, creationTime);
			
			// Create a new provider wallet
			providerWallet = Wallet.fromSeed(networkParameters,
					NodeUtils.createDeterministicSeed(bytes, creationTime), UniquidNode.BIP44_ACCOUNT_PROVIDER);
			
			// Create a new user wallet
			userWallet = Wallet.fromSeed(networkParameters,
					NodeUtils.createDeterministicSeed(bytes, creationTime), UniquidNode.BIP44_ACCOUNT_USER);
			
			// Create provider revoke watching wallet
			providerRevokeWallet = new Wallet(networkParameters);
			
			// Create user revoke watching wallet
			userRevokeWallet = new Wallet(networkParameters);
			
//			LOGGER.info("PROVIDER WALLET created: " + providerWallet.currentReceiveAddress().toBase58());
//			LOGGER.info("PROVIDER WALLET current change addr: " + providerWallet.currentChangeAddress().toBase58());
//			LOGGER.info("PROVIDER WALLET: " + providerWallet.toString());
//			LOGGER.info("USER WALLET created: " + userWallet.currentReceiveAddress().toBase58());
//			LOGGER.info("USER WALLET curent change addr: " + userWallet.currentChangeAddress().toBase58());
//			LOGGER.info("USER WALLET: " + userWallet.toString());
			
			setNodeState(new CreatedState(this, imprintingAddress));
			
			providerWallet.saveToFile(providerFile);
			userWallet.saveToFile(userFile);
			providerRevokeWallet.saveToFile(providerRevokeFile);
			userWallet.saveToFile(userRevokeFile);
			
		}
		
		// Create node event listner
		NodeEventListener nodeEventListner = new NodeEventListener(this);
		
		// Add event listeners
		providerWallet.addCoinsReceivedEventListener(nodeEventListner);
		providerWallet.addCoinsSentEventListener(nodeEventListner);
		userWallet.addCoinsReceivedEventListener(nodeEventListner);
		userWallet.addCoinsSentEventListener(nodeEventListner);
		providerRevokeWallet.addCoinsSentEventListener(nodeEventListner);
		userRevokeWallet.addCoinsSentEventListener(nodeEventListner);
		
		
		// First BC sync
		NodeUtils.syncBlockChain(networkParameters, Arrays.asList(new Wallet[] { providerWallet, userWallet, providerRevokeWallet, userRevokeWallet }), chainFile);
		
		//DONE INITIALIZATION
	}
	
	private void calculatePublicInfo(byte[] bytes, long creationTime) {
		
		DeterministicKey deterministicKey = NodeUtils.createDeterministicKeyFromByteArray(bytes);
		
		//LOGGER.info("START_NODE tpriv: " + deterministicKey.serializePrivB58(networkParameters));
		LOGGER.info("START_NODE tpub: " + deterministicKey.serializePubB58(networkParameters));
		
		DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(deterministicKey);
		
		ImmutableList<ChildNumber> IMPRINTING_PATH = ImmutableList.of(
	    		new ChildNumber(44, true),
	    		new ChildNumber(0, true)
	    	);
		
		DeterministicKey imprintingKey = deterministicHierarchy.get(IMPRINTING_PATH, true, true);
		//LOGGER.info("Imprinting key tpriv: " + imprintingKey.serializePrivB58(networkParameters));
		//LOGGER.info("Imprinting key tpub: " + imprintingKey.serializePubB58(networkParameters));
		
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
	
	public void startNode() throws Exception {
		
		// This will start BlockChain synchronization
		
		final Runnable walletSyncher = new Runnable() {
			
			public void run() {

				// Synchronize wallets against blockchain
				NodeUtils.syncBlockChain(networkParameters, Arrays.asList(new Wallet[] { providerWallet, userWallet, providerRevokeWallet, userRevokeWallet }), chainFile);
				
				try {
					providerWallet.saveToFile(providerFile);
					userWallet.saveToFile(userFile);
					providerRevokeWallet.saveToFile(providerRevokeFile);
					userRevokeWallet.saveToFile(userRevokeFile);
				} catch (Exception ex) {
					LOGGER.error("Exception while saving wallets");
				}
			}
		};

		final ScheduledFuture<?> updaterThread = scheduledExecutorService.scheduleWithFixedDelay(walletSyncher, 0, 5,
				TimeUnit.MINUTES);
		
	}
	
	public void stopNode() throws Exception{
		
		//nodeState.stopNode();

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
		private File _providerRevokeFile;
		private File _userRevokeFile;

		private Wallet _providerWallet;
		private Wallet _userWallet;

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
		
		public Builder set_providerRevokeFile(File _providerRevokeFile) {
			this._providerRevokeFile = _providerRevokeFile;
			return this;
		}
		
		public Builder set_userRevokeFile(File _userRevokeFile) {
			this._userRevokeFile = _userRevokeFile;
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

	@Override
	public RegisterFactory getRegisterFactory() {
		return registerFactory;
	}

}