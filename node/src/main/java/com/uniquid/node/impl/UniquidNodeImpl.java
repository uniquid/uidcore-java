package com.uniquid.node.impl;

import static org.bitcoinj.core.Utils.HEX;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.google.common.collect.ImmutableList;
import com.uniquid.node.UniquidNode;
import com.uniquid.node.event.NodeEventListener;
import com.uniquid.node.event.UniquidNodeDownloadProgressTracker;
import com.uniquid.node.exception.NodeException;
import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.node.state.NodeState;
import com.uniquid.node.state.NodeState.State;
import com.uniquid.node.state.NodeStateContext;
import com.uniquid.node.state.impl.ImprintingState;
import com.uniquid.node.state.impl.ReadyState;
import com.uniquid.node.utils.NodeUtils;
import com.uniquid.node.utils.WalletUtils;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;

/**
 * This class represents an Uniquid Node: an entity that have wallets and a state
 * 
 * @author giuseppe
 *
 */
public class UniquidNodeImpl implements NodeStateContext, UniquidNode {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeImpl.class.getName());
	
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
	private Wallet providerWallet;
	private File providerChainFile;
	private File userFile;
	private Wallet userWallet;
	private File userChainFile;
	
	private Address imprintingAddress;
	private String publicKey;
	private String  machineName;

	private RegisterFactory registerFactory;
	
	private List<UniquidNodeEventListener> eventListeners;

	private UniquidNodeImpl(Builder builder) throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		this.networkParameters = builder._params;
		this.providerFile = builder._providerFile;
		this.providerChainFile = builder._chainFile;
		this.userFile = builder._userFile;
		this.userChainFile = builder._userChainFile;
		this.registerFactory = builder._registerFactory;
		this.machineName = builder._machineName;
		this.eventListeners = new ArrayList<UniquidNodeEventListener>();
		
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
	
	/* (non-Javadoc)
	 * @see com.uniquid.node.UniquidNode#getImprintingAddress()
	 */
	@Override
	public Address getImprintingAddress() {
		return imprintingAddress;
	}
	
	@Override
	public synchronized List<UniquidNodeEventListener> getUniquidNodeEventListeners() {
		return eventListeners;
	}

	/* (non-Javadoc)
	 * @see com.uniquid.node.UniquidNode#getPublicKey()
	 */
	@Override
	public String getPublicKey() {
		return publicKey;
	}
	
	/* (non-Javadoc)
	 * @see com.uniquid.node.UniquidNode#getMachineName()
	 */
	@Override
	public String  getMachineName() {
		return machineName;
	}
	
	@Override
	public void initNodeFromHexEntropy(String hexEntropy, long creationTime) throws NodeException {
		byte[] bytes = HEX.decode(hexEntropy);
		
		initNode(bytes, creationTime);
	}
	
	@Override
	public void initNode() throws NodeException {
		SecureRandom random = new SecureRandom();
		//byte[] bytes = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 8];
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		long creationTime = System.currentTimeMillis() / 1000;
		
		initNode(bytes, creationTime);
	}

	/* (non-Javadoc)
	 * @see com.uniquid.node.UniquidNode#initNode()
	 */
	private void initNode(byte[] bytes, long creationTime) throws NodeException {
		
		try {
		
			if (providerFile.exists() && !providerFile.isDirectory() &&
					userFile.exists() && !userFile.isDirectory()) {
	
				// Wallets already present!
				providerWallet = Wallet.loadFromFile(providerFile);
				userWallet = Wallet.loadFromFile(userFile);
				
				bytes = providerWallet.getKeyChainSeed().getSeedBytes();
				creationTime = providerWallet.getKeyChainSeed().getCreationTimeSeconds();
				
			} else {
				
				// Create a new provider wallet
	//			providerWallet = Wallet.fromSeed(networkParameters,
	//					new DeterministicSeed(bytes, "", creationTime), UniquidNodeImpl.BIP44_ACCOUNT_PROVIDER, UniquidNodeImpl.BIP44_ACCOUNT_USER);
				
				providerWallet = Wallet.fromSeed(networkParameters,
						new DeterministicSeed(bytes, "", creationTime), UniquidNodeImpl.BIP44_ACCOUNT_PROVIDER);
				
				// Create a new user wallet
				userWallet = Wallet.fromSeed(networkParameters,
						new DeterministicSeed(bytes, "", creationTime), UniquidNodeImpl.BIP44_ACCOUNT_USER);
				
			}
			
			// Calculate public info
			calculatePublicInfo(bytes, creationTime);
			
			// Retrieve contracts
			List<ProviderChannel> providerChannels = registerFactory.getProviderRegister().getAllChannels();
			
			// If there is at least 1 contract, then we are ready
			if (providerChannels.size() > 0) {
	
				// Jump to ready state
				setNodeState(new ReadyState());
	
			} else {
	
				// Jump to initializing
				setNodeState(new ImprintingState());
	
			}
			
			// Create node event listner
			NodeEventListener nodeEventListner = new NodeEventListener(this);
			
			// Add event listeners
			providerWallet.addCoinsReceivedEventListener(nodeEventListner);
			providerWallet.addCoinsSentEventListener(nodeEventListner);
			userWallet.addCoinsReceivedEventListener(nodeEventListner);
			userWallet.addCoinsSentEventListener(nodeEventListner);
			
			// start updating
			updateNode();
		
		} catch (Exception ex) {
			
			throw new NodeException("Exception while initializating node", ex);
			
		}
	
		//DONE INITIALIZATION
	}
	
	private void calculatePublicInfo(byte[] bytes, long creationTime) {
		
		LOGGER.info("HEX entropy " + HEX.encode(bytes) + "; creation time " + creationTime);
		
		LOGGER.info("Mnemonics: " + Utils.join(new DeterministicSeed(bytes, "", creationTime).getMnemonicCode()));
		
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
	
	/* (non-Javadoc)
	 * @see com.uniquid.node.UniquidNode#addUniquidNodeEventListener(com.uniquid.node.listeners.UniquidNodeEventListener)
	 */
	@Override
	public synchronized void addUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener) {

		eventListeners.add(uniquidNodeEventListener);

	}
	
	/* (non-Javadoc)
	 * @see com.uniquid.node.UniquidNode#removeUniquidNodeEventListener(com.uniquid.node.listeners.UniquidNodeEventListener)
	 */
	@Override
	public synchronized void removeUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener) {

		eventListeners.remove(uniquidNodeEventListener);

	}
	
	public synchronized void onCoinsReceived(Wallet wallet, Transaction tx) {
		
		nodeState.onCoinsReceived(this, wallet, tx);
	
	}
	
	public synchronized void onCoinsSent(Wallet wallet, Transaction tx) {
	
		nodeState.onCoinsSent(this, wallet, tx);
	
	}
	
	/* (non-Javadoc)
	 * @see com.uniquid.node.UniquidNode#getNodeState()
	 */
	@Override
	public synchronized State getNodeState() {
		
		return nodeState.getState();
		
	}
	
	/* (non-Javadoc)
	 * @see com.uniquid.node.UniquidNode#updateNode()
	 */
	@Override
	public void updateNode() throws NodeException {
		
		// Provider wallet BC sync
		NodeUtils.syncBlockChain(networkParameters, providerWallet, providerChainFile, new UniquidNodeDownloadProgressTracker(this));
		
		// User wallet BC sync
		NodeUtils.syncBlockChain(networkParameters, userWallet, userChainFile, new UniquidNodeDownloadProgressTracker(this));
		
		try {
			
			providerWallet.saveToFile(providerFile);
			userWallet.saveToFile(userFile);
		
		} catch (IOException ex) {
			
			throw new NodeException("Exception while updating node", ex);
			
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
			
			retValue = NodeUtils.sendTransaction(networkParameters, providerWallet, providerChainFile, send);
		
		} else if (path.startsWith("1")) {
			
			// delegate to walled the signing
			userWallet.signTransaction(send);
			
			String sr = Hex.toHexString(originalTransaction.bitcoinSerialize());
		    
			LOGGER.info("Serialized SIGNED transaction: " + sr);
			
			retValue = NodeUtils.sendTransaction(networkParameters, userWallet, providerChainFile, send);
			
		}
		
		return retValue;
	}

	public static class Builder {

		private NetworkParameters _params;

		private File _providerFile;
		private File _userFile;
		private File _chainFile;
		private File _userChainFile;

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
		
		public Builder set_userChainFile(File _userChainFile) {
			this._userChainFile = _userChainFile;
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

		public UniquidNodeImpl build() throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

			return new UniquidNodeImpl(this);

		}
	}

}