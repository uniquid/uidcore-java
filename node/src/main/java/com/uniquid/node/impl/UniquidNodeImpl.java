package com.uniquid.node.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.jni.NativePeerEventListener;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.google.common.collect.ImmutableList;
import com.uniquid.node.UniquidNode;
import com.uniquid.node.exception.NodeException;
import com.uniquid.node.impl.state.CreatedState;
import com.uniquid.node.impl.state.ImprintingState;
import com.uniquid.node.impl.state.ReadyState;
import com.uniquid.node.impl.state.UniquidNodeState;
import com.uniquid.node.impl.utils.NodeUtils;
import com.uniquid.node.impl.utils.WalletUtils;
import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;

/**
 * Implementation of an Uniquid Node with BitcoinJ library
 */
public class UniquidNodeImpl implements UniquidNode, WalletCoinsSentEventListener, WalletCoinsReceivedEventListener, UniquidNodeStateContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeImpl.class.getName());

	public static ImmutableList<ChildNumber> BIP44_ACCOUNT_PROVIDER = ImmutableList.of(new ChildNumber(44, true),
			new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(0, false));

	public static ImmutableList<ChildNumber> BIP44_ACCOUNT_USER = ImmutableList.of(new ChildNumber(44, true),
			new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(1, false));

	public static ImmutableList<ChildNumber> BIP44_ACCOUNT_ORCHESTRATOR = ImmutableList.of(new ChildNumber(44, true),
			new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(2, false), new ChildNumber(0, false));

	/** The current state of this Node */
	private UniquidNodeState nodeState;
	private File providerChainFile;
	private File userChainFile;
	private String nodeName;

	protected NetworkParameters networkParameters;
	protected File providerFile;
	protected Wallet providerWallet;
	protected File userFile;
	protected Wallet userWallet;

	protected Address imprintingAddress;
	protected String publicKey;

	protected long creationTime;

	protected RegisterFactory registerFactory;

	private DeterministicSeed detSeed;

	private UniquidNodeEventService uniquidNodeEventService;

	/**
	 * Creates a new instance from {@code Builder}
	 * @param builder
	 * @throws UnreadableWalletException
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	protected UniquidNodeImpl(Builder builder)
			throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		this.networkParameters = builder.getNetworkParameters();
		this.providerFile = builder.getProviderFile();
		this.providerChainFile = builder.getChainFile();
		this.userFile = builder.getUserFile();
		this.userChainFile = builder.getUserChainFile();
		this.registerFactory = builder.getRegisterFactory();
		this.nodeName = builder.getNodeName();
		this.creationTime = builder.getCreationTime();
		this.uniquidNodeEventService = new UniquidNodeEventService();
		this.detSeed = builder.getDeterministicSeed();
		
		setUniquidNodeState(getCreatedState());

	}

	/**
	 * Return the {@link com.uniquid.node.UniquidNodeState} that manages the {@link com.uniquid.node.UniquidNodeState.CREATED}
	 * @return
	 */
	protected UniquidNodeState getCreatedState() {

		return new CreatedState();

	}

	/**
	 * Return the {@link com.uniquid.node.UniquidNodeState} that manages the {@link com.uniquid.node.UniquidNodeState.READY}
	 * @return
	 */
	protected UniquidNodeState getReadyState() {
		
		return new ReadyState(this);
	
	}

	/**
	 * Return the {@link com.uniquid.node.UniquidNodeState} that manages the {@link com.uniquid.node.UniquidNodeState.IMPRINTING}
	 * @return
	 */
	protected UniquidNodeState getImprintingState() {
		
		return new ImprintingState(this);
	
	}

	/*
	 * 
	 * Begin of public part for implementing UniquidNode
	 *
	 */
	@Override
	public synchronized String getImprintingAddress() {
		return nodeState.getImprintingAddress();
	}
	
	@Override
	public synchronized Address getImprintingAddressValue() {
		return imprintingAddress;
	}

	@Override
	public synchronized String getPublicKey() {
		return nodeState.getPublicKey();
	}
	
	@Override
	public synchronized String getPublicKeyValue() {
		return publicKey;
	}

	@Override
	public synchronized String getNodeName() {
		return nodeName;
	}

	@Override
	public synchronized long getCreationTime() {
		return creationTime;
	}
	
	@Override
	public synchronized String getHexSeed() {
		return detSeed.toHexString();
	}

	@Override
	public synchronized String getSpendableBalance() {
		return providerWallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).toFriendlyString();
	}

	@Override
	public void initNode() throws NodeException {

		try {
			
			if (providerFile.exists() && !providerFile.isDirectory() && userFile.exists() && !userFile.isDirectory()) {

				// Wallets already present!
				providerWallet = Wallet.loadFromFile(providerFile);
				userWallet = Wallet.loadFromFile(userFile);

			} else {

				// Create a new provider wallet
				providerWallet = Wallet.fromSeed(networkParameters, detSeed, UniquidNodeImpl.BIP44_ACCOUNT_PROVIDER);
				providerWallet.setDescription("provider");

				providerWallet.saveToFile(providerFile);

				// Create a new user wallet
				userWallet = Wallet.fromSeed(networkParameters, detSeed, UniquidNodeImpl.BIP44_ACCOUNT_USER);
				userWallet.setDescription("user");

				userWallet.saveToFile(userFile);

			}

			// Calculate public info
			calculatePublicInfo(detSeed);

			// Retrieve contracts
			List<ProviderChannel> providerChannels = registerFactory.getProviderRegister().getAllChannels();

			// If there is at least 1 contract, then we are ready
			if (providerChannels.size() > 0) {

				// Jump to ready state
				setUniquidNodeState(getReadyState());

			} else {

				// Jump to initializing
				setUniquidNodeState(getImprintingState());

			}

			// Add event listeners
			providerWallet.addCoinsReceivedEventListener(this);
			providerWallet.addCoinsSentEventListener(this);
			userWallet.addCoinsReceivedEventListener(this);
			userWallet.addCoinsSentEventListener(this);

		} catch (Exception ex) {

			throw new NodeException("Exception while initializating node", ex);

		}

		// DONE INITIALIZATION
	}
	
	@Override
	public synchronized void updateNode() throws NodeException {

		// Start node sync
		uniquidNodeEventService.onSyncNodeStart();

		// Provider wallet BC sync
		NodeUtils.syncBlockChain(networkParameters, providerWallet, providerChainFile,
				new UniquidNodeDownloadProgressTracker(), new UniquidPeerConnectionListener());

		// User wallet BC sync
		NodeUtils.syncBlockChain(networkParameters, userWallet, userChainFile,
				new UniquidNodeDownloadProgressTracker(), new UniquidPeerConnectionListener());

		try {

			providerWallet.saveToFile(providerFile);
			userWallet.saveToFile(userFile);

		} catch (IOException ex) {

			throw new NodeException("Exception while updating node", ex);

		}
		
		// Check if user contracts are still valid
//		try {
//		
//			List<UserChannel> userChannels = registerFactory.getUserRegister().getAllUserChannels();
//			
//			for (final UserChannel u : userChannels) {
//
//				if (!WalletUtils.isUnspent(u.getRevokeTxId(), u.getRevokeAddress()) ) {
//					
//					LOGGER.info("Revoking user channel: " + u);
//					
//					registerFactory.getUserRegister().deleteChannel(u);
//					
//					// Inform listeners
//					for (final ListenerRegistration<UniquidNodeEventListener> listener : eventListeners) {
//
//						listener.executor.execute(new Runnable() {
//			                @Override
//			                public void run() {
//			                		listener.listener.onUserContractRevoked(u);
//			                }
//			            });
//
//					}
//
//				}
//				
//			}
//		
//		} catch (Exception ex) {
//			
//			LOGGER.error("Exception while accessing user channel", ex);
//			
//		}
		
		// Start node sync
		uniquidNodeEventService.onSyncNodeEnd();
		
	}

	@Override
	public synchronized com.uniquid.node.UniquidNodeState getNodeState() {
		return nodeState.getNodeState();
	}

	@Override
	public synchronized void addUniquidNodeEventListener(final UniquidNodeEventListener uniquidNodeEventListener) {
		uniquidNodeEventService.addUniquidNodeEventListener(uniquidNodeEventListener);
	}

	@Override
	public synchronized void removeUniquidNodeEventListener(final UniquidNodeEventListener uniquidNodeEventListener) {
		uniquidNodeEventService.removeUniquidNodeEventListener(uniquidNodeEventListener);
	}

	/*
	 * End of public part for implementing UniquidNode
	 *
	 */

	/*
	 * Begin of some other useful public method
	 */
	public Wallet getProviderWallet() {
		return providerWallet;
	}

	public Wallet getUserWallet() {
		return userWallet;
	}

	@Override
	public synchronized String signTransaction(final String s_tx, final String path) throws NodeException {

		try {
			Transaction originalTransaction = networkParameters.getDefaultSerializer().makeTransaction(Hex.decode(s_tx));
	
			String transactionToString = Hex.toHexString(originalTransaction.bitcoinSerialize());
			LOGGER.info("Serialized unsigned transaction: " + transactionToString);
	
			SendRequest send = SendRequest.forTx(originalTransaction);
	
			if (path.startsWith("0")) {
				
				// fix our tx
				WalletUtils.newCompleteTransaction(send, providerWallet, networkParameters);
	
				// delegate to walled the signing
				providerWallet.signTransaction(send);
	
				return Hex.toHexString(originalTransaction.bitcoinSerialize());
	
			} else if (path.startsWith("1")) {
				
				// fix our tx
				WalletUtils.newCompleteTransaction(send, userWallet, networkParameters);
	
				// delegate to walled the signing
				userWallet.signTransaction(send);
	
				return Hex.toHexString(originalTransaction.bitcoinSerialize());
	
			} else {

				throw new NodeException("Unknown path");
				
			}
	
		} catch (Exception ex) {
			
			throw new NodeException("Exception while signing", ex);
		}
	}
	
	@Override
	public String broadCastTransaction(String serializedTx) throws NodeException {
		
		try {

			Transaction originalTransaction = networkParameters.getDefaultSerializer().makeTransaction(Hex.decode(serializedTx));
			
			SendRequest send = SendRequest.forTx(originalTransaction);
			
			return NodeUtils.sendTransaction(networkParameters, send);
		
		} catch (Throwable t) {
			
			throw new NodeException("Exception while broadcasting transaction", t);
			
		}

	}

	/**
	 * Builder for UniquidNodeImpl
	 *
	 */
	public static class Builder<T extends Builder> {

		private NetworkParameters _params;

		private File _providerFile;
		private File _userFile;
		private File _chainFile;
		private File _userChainFile;
		protected long _creationTime;

		private RegisterFactory _registerFactory;

		private String _machineName;
		
		protected DeterministicSeed _detSeed;
		
		public Builder setNetworkParameters(NetworkParameters _params) {
			this._params = _params;
			return (T) this;
		}
		
		public NetworkParameters getNetworkParameters() {
			return _params;
		}

		public Builder setProviderFile(File _providerFile) {
			this._providerFile = _providerFile;
			return (T) this;
		}
		
		public File getProviderFile() {
			return _providerFile;
		}

		public Builder setUserFile(File _userFile) {
			this._userFile = _userFile;
			return (T) this;
		}
		
		public File getUserFile() {
			return _userFile;
		}

		public Builder setChainFile(File _chainFile) {
			this._chainFile = _chainFile;
			return (T) this;
		}
		
		public File getChainFile() {
			return _chainFile;
		}

		public Builder setUserChainFile(File _userChainFile) {
			this._userChainFile = _userChainFile;
			return (T) this;
		}
		
		public File getUserChainFile() {
			return _userChainFile;
		}

		public Builder setRegisterFactory(RegisterFactory _registerFactory) {
			this._registerFactory = _registerFactory;
			return (T) this;
		}
		
		public RegisterFactory getRegisterFactory() {
			return _registerFactory;
		}

		public Builder setNodeName(String _machineName) {
			this._machineName = _machineName;
			return (T) this;
		}
		
		public String getNodeName() {
			return _machineName;
		}

		public long getCreationTime() {
			return _creationTime;
		}
		
		public DeterministicSeed getDeterministicSeed() {
			return _detSeed;
		}

		public UniquidNodeImpl build()
				throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

			SecureRandom random = new SecureRandom();
			byte[] _entropy = new byte[32];
			random.nextBytes(_entropy);
			_creationTime = System.currentTimeMillis() / 1000;
			
			_detSeed = new DeterministicSeed(_entropy, "", _creationTime);

			return new UniquidNodeImpl(this);

		}

		public UniquidNodeImpl buildFromHexSeed(final String hexSeed, final long creationTime)
				throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

			_creationTime = creationTime;

			_detSeed = new DeterministicSeed("", org.bitcoinj.core.Utils.HEX.decode(hexSeed), "", creationTime);

			return new UniquidNodeImpl(this);

		}

		public UniquidNodeImpl buildFromMnemonic(final String mnemonic, final long creationTime)
				throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

			_creationTime = creationTime;
			
			_detSeed = new DeterministicSeed(mnemonic, null, "", creationTime);

			return new UniquidNodeImpl(this);

		}
	}

	/*
	 * Begin of some other useful public method
	 */

	/**
	 * Change internal state
	 */
	public synchronized void setUniquidNodeState(final UniquidNodeState nodeState) {
		
		this.nodeState = nodeState;
		
		// Send event to listeners
		uniquidNodeEventService.onNodeStateChange(nodeState.getNodeState());
	}

	/*
	 * Calculate some public info
	 */
	private void calculatePublicInfo(final DeterministicSeed detSeed) {

		DeterministicKey deterministicKey = NodeUtils.createDeterministicKeyFromByteArray(detSeed.getSeedBytes());

		// LOGGER.info("START_NODE tpriv: " +
		// deterministicKey.serializePrivB58(networkParameters));
		// LOGGER.info("START_NODE tpub: " +
		// deterministicKey.serializePubB58(networkParameters));

		DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(deterministicKey);

		ImmutableList<ChildNumber> IMPRINTING_PATH = ImmutableList.of(new ChildNumber(44, true),
				new ChildNumber(0, true));

		DeterministicKey imprintingKey = deterministicHierarchy.get(IMPRINTING_PATH, true, true);

		publicKey = imprintingKey.serializePubB58(networkParameters);

		imprintingAddress = NodeUtils.calculateImprintAddress(imprintingKey, networkParameters);

	}

	/*
	 * callback to receive events from bitcoinj when coins are received
	 */
	public synchronized void onCoinsReceived(final Wallet wallet, final Transaction tx, final Coin prevBalance,
			final Coin newBalance) {

		org.bitcoinj.core.Context currentContext = new org.bitcoinj.core.Context(networkParameters);
		org.bitcoinj.core.Context.propagate(currentContext);

		nodeState.onCoinsReceived(wallet, tx);
	}

	/*
	 * callback to receive events from bitcoinj when coins are sent
	 */
	public synchronized void onCoinsSent(final Wallet wallet, final Transaction tx, final Coin prevBalance,
			final Coin newBalance) {

		org.bitcoinj.core.Context currentContext = new org.bitcoinj.core.Context(networkParameters);
		org.bitcoinj.core.Context.propagate(currentContext);

		nodeState.onCoinsSent(wallet, tx);
	}

	/*
	 * Implementation of callback for blockchain events
	 */
	private class UniquidNodeDownloadProgressTracker extends DownloadProgressTracker {

		@Override
		protected void startDownload(final int blocks) {

			uniquidNodeEventService.onSyncStarted(blocks);

		}

		@Override
		protected void progress(final double pct, final int blocksSoFar, final Date date) {

			uniquidNodeEventService.onSyncProgress(pct, blocksSoFar, date);

		}

		@Override
		public void doneDownload() {

			uniquidNodeEventService.onSyncEnded();

		}

	}
	
	private class UniquidPeerConnectionListener extends NativePeerEventListener {
		
		@Override
		public void onPeersDiscovered(Set<PeerAddress> peerAddresses) {
			uniquidNodeEventService.onPeersDiscovered(peerAddresses);
		}

		@Override
		public void onPeerConnected(Peer peer, int peerCount) {
			uniquidNodeEventService.onPeerConnected(peer, peerCount);
			
		}

		@Override
		public void onPeerDisconnected(Peer peer, int peerCount) {
			uniquidNodeEventService.onPeerDisconnected(peer, peerCount);			
		}
		
	}

	@Override
	public NetworkParameters getNetworkParameters() {
		return networkParameters;
	}

	@Override
	public RegisterFactory getRegisterFactory() {
		return registerFactory;
	}

	@Override
	public UniquidNodeEventService getUniquidNodeEventService() {
		return uniquidNodeEventService;
	}

}