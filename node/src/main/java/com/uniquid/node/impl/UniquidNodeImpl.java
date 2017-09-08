package com.uniquid.node.impl;

import java.io.File;
import java.io.IOException;
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
public class UniquidNodeImpl implements UniquidNode {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeImpl.class.getName());

	public static ImmutableList<ChildNumber> BIP44_ACCOUNT_PROVIDER = ImmutableList.of(new ChildNumber(44, true),
			new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(0, false));

	public static ImmutableList<ChildNumber> BIP44_ACCOUNT_USER = ImmutableList.of(new ChildNumber(44, true),
			new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(1, false));

	public static ImmutableList<ChildNumber> BIP44_ACCOUNT_ORCHESTRATOR = ImmutableList.of(new ChildNumber(44, true),
			new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(2, false), new ChildNumber(0, false));

	/** The current state of this Node */
	private UniquidNodeState nodeState;

	protected Wallet providerWallet;
	protected Wallet userWallet;

	protected Address imprintingAddress;
	protected String publicKey;

	protected final UniquidNodeConfiguration uniquidNodeConfiguration;

	private final UniquidNodeEventService uniquidNodeEventService;

	/**
	 * Creates a new instance
	 * 
	 * @param uniquidNodeConfiguration
	 * @throws NodeException
	 */
	protected UniquidNodeImpl(UniquidNodeConfiguration uniquidNodeConfiguration) throws NodeException {

		this.uniquidNodeConfiguration = uniquidNodeConfiguration;
		this.uniquidNodeEventService = new UniquidNodeEventService();

		setUniquidNodeState(getCreatedState());

	}

	/**
	 * Return the {@link com.uniquid.node.UniquidNodeState} that manages the
	 * {@link com.uniquid.node.UniquidNodeState.CREATED}
	 * 
	 * @return
	 */
	protected UniquidNodeState getCreatedState() {

		return new CreatedState();

	}

	/**
	 * Return the {@link com.uniquid.node.UniquidNodeState} that manages the
	 * {@link com.uniquid.node.UniquidNodeState.READY}
	 * 
	 * @return
	 */
	protected UniquidNodeState getReadyState() {

		return new ReadyState(new UniquidNodeStateContextImpl());

	}

	/**
	 * Return the {@link com.uniquid.node.UniquidNodeState} that manages the
	 * {@link com.uniquid.node.UniquidNodeState.IMPRINTING}
	 * 
	 * @return
	 */
	protected UniquidNodeState getImprintingState() {

		return new ImprintingState(new UniquidNodeStateContextImpl());

	}

	/*
	 * 
	 * Begin of public part for implementing UniquidNode
	 *
	 */
	@Override
	public synchronized String getImprintingAddress() {
		if (imprintingAddress != null) {
			return imprintingAddress.toBase58();
		}
		
		return null;
	}

	@Override
	public synchronized String getPublicKey() {
		return publicKey;
	}

	@Override
	public synchronized String getNodeName() {
		return uniquidNodeConfiguration.getNodeName();
	}

	@Override
	public synchronized long getCreationTime() {
		return uniquidNodeConfiguration.getCreationTime();
	}

	@Override
	public synchronized String getHexSeed() {
		return uniquidNodeConfiguration.getDetSeed().toHexString();
	}

	@Override
	public synchronized String getSpendableBalance() {
		return providerWallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).toFriendlyString();
	}

	@Override
	public void initNode() throws NodeException {

		try {

			LOGGER.info("Initializing node");

			if (uniquidNodeConfiguration.getProviderFile().exists()
					&& !uniquidNodeConfiguration.getProviderFile().isDirectory()
					&& uniquidNodeConfiguration.getUserFile().exists()
					&& !uniquidNodeConfiguration.getUserFile().isDirectory()) {

				LOGGER.info("Found {} {}", uniquidNodeConfiguration.getProviderFile().getAbsolutePath(),
						uniquidNodeConfiguration.getUserFile().getAbsolutePath());

				// Wallets already present!
				providerWallet = Wallet.loadFromFile(uniquidNodeConfiguration.getProviderFile());
				userWallet = Wallet.loadFromFile(uniquidNodeConfiguration.getUserFile());

			} else {

				LOGGER.info("Generating new node from seed");

				// Create a new provider wallet
				providerWallet = Wallet.fromSeed(uniquidNodeConfiguration.getNetworkParameters(),
						uniquidNodeConfiguration.getDetSeed(), UniquidNodeImpl.BIP44_ACCOUNT_PROVIDER);
				providerWallet.setDescription("provider");

				providerWallet.saveToFile(uniquidNodeConfiguration.getProviderFile());

				// Create a new user wallet
				userWallet = Wallet.fromSeed(uniquidNodeConfiguration.getNetworkParameters(),
						uniquidNodeConfiguration.getDetSeed(), UniquidNodeImpl.BIP44_ACCOUNT_USER);
				userWallet.setDescription("user");

				userWallet.saveToFile(uniquidNodeConfiguration.getUserFile());

			}

			// Calculate public info
			calculatePublicInfo(uniquidNodeConfiguration.getDetSeed());

			// Retrieve contracts
			List<ProviderChannel> providerChannels = uniquidNodeConfiguration.getRegisterFactory().getProviderRegister()
					.getAllChannels();

			// If there is at least 1 contract, then we are ready
			if (providerChannels.size() > 0) {

				LOGGER.info("Found {} contracts. Jumping to Ready State", providerChannels.size());

				// Jump to ready state
				setUniquidNodeState(getReadyState());

			} else {

				LOGGER.info("No contracts found. Jumping to Imprinting State");

				// Jump to initializing
				setUniquidNodeState(getImprintingState());

			}

			// Add event listeners
			providerWallet.addCoinsReceivedEventListener(new UniquidWalletCoinsReceivedEventListener());
			providerWallet.addCoinsSentEventListener(new UniquidWalletCoinsSentEventListener());
			userWallet.addCoinsReceivedEventListener(new UniquidWalletCoinsReceivedEventListener());
			userWallet.addCoinsSentEventListener(new UniquidWalletCoinsSentEventListener());

		} catch (Exception ex) {

			throw new NodeException("Exception while initializating node", ex);

		}

		// DONE INITIALIZATION
	}

	@Override
	public synchronized void updateNode() throws NodeException {

		LOGGER.info("Updating node");

		// Start node sync
		uniquidNodeEventService.onSyncNodeStart();

		// Provider wallet BC sync
		NodeUtils.syncBlockChain(uniquidNodeConfiguration.getNetworkParameters(), providerWallet,
				uniquidNodeConfiguration.getProviderChainFile(), new UniquidNodeDownloadProgressTracker(),
				new UniquidPeerConnectionListener());

		// User wallet BC sync
		NodeUtils.syncBlockChain(uniquidNodeConfiguration.getNetworkParameters(), userWallet,
				uniquidNodeConfiguration.getUserChainFile(), new UniquidNodeDownloadProgressTracker(),
				new UniquidPeerConnectionListener());

		try {

			providerWallet.saveToFile(uniquidNodeConfiguration.getProviderFile());
			userWallet.saveToFile(uniquidNodeConfiguration.getUserFile());

		} catch (IOException ex) {

			throw new NodeException("Exception while persisting wallets", ex);

		}

		// Check if user contracts are still valid
		// try {
		//
		// List<UserChannel> userChannels =
		// registerFactory.getUserRegister().getAllUserChannels();
		//
		// for (final UserChannel u : userChannels) {
		//
		// if (!WalletUtils.isUnspent(u.getRevokeTxId(), u.getRevokeAddress()) )
		// {
		//
		// LOGGER.info("Revoking user channel: " + u);
		//
		// registerFactory.getUserRegister().deleteChannel(u);
		//
		// // Inform listeners
		// for (final ListenerRegistration<UniquidNodeEventListener> listener :
		// eventListeners) {
		//
		// listener.executor.execute(new Runnable() {
		// @Override
		// public void run() {
		// listener.listener.onUserContractRevoked(u);
		// }
		// });
		//
		// }
		//
		// }
		//
		// }
		//
		// } catch (Exception ex) {
		//
		// LOGGER.error("Exception while accessing user channel", ex);
		//
		// }

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

		LOGGER.info("Signing TX");
		LOGGER.trace("Signing TX {} at path {}", s_tx, path);

		try {
			Transaction originalTransaction = uniquidNodeConfiguration.getNetworkParameters().getDefaultSerializer()
					.makeTransaction(Hex.decode(s_tx));

			String transactionToString = Hex.toHexString(originalTransaction.bitcoinSerialize());
			LOGGER.trace("Serialized unsigned transaction: " + transactionToString);

			SendRequest send = SendRequest.forTx(originalTransaction);

			if (path.startsWith("0")) {

				// fix our tx
				WalletUtils.newCompleteTransaction(send, providerWallet,
						uniquidNodeConfiguration.getNetworkParameters());

				// delegate to walled the signing
				providerWallet.signTransaction(send);

				return Hex.toHexString(originalTransaction.bitcoinSerialize());

			} else if (path.startsWith("1")) {

				// fix our tx
				WalletUtils.newCompleteTransaction(send, userWallet, uniquidNodeConfiguration.getNetworkParameters());

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

		LOGGER.info("Broadcasting TX");

		try {

			Transaction originalTransaction = uniquidNodeConfiguration.getNetworkParameters().getDefaultSerializer()
					.makeTransaction(Hex.decode(serializedTx));

			SendRequest send = SendRequest.forTx(originalTransaction);

			return NodeUtils.sendTransaction(uniquidNodeConfiguration.getNetworkParameters(), send);

		} catch (Throwable t) {

			throw new NodeException("Exception while broadcasting transaction", t);

		}

	}

	/**
	 * Builder for UniquidNodeImpl
	 */
	public static class UniquidNodeBuilder<B extends UniquidNodeBuilder<B, T>, T extends UniquidNodeConfiguration> {

		protected T uniquidNodeConfiguration;

		public UniquidNodeBuilder() {
			this.uniquidNodeConfiguration = createUniquidNodeConfiguration();
		}
		
		public T getUniquidNodeConfiguration() {
			return uniquidNodeConfiguration;
		}

		public B setNetworkParameters(NetworkParameters params) {
			uniquidNodeConfiguration.setNetworkParameters(params);
			return (B) this;
		}

		public B setProviderFile(File providerFile) {
			uniquidNodeConfiguration.setProviderFile(providerFile);
			return (B) this;
		}

		public B setUserFile(File userFile) {
			uniquidNodeConfiguration.setUserFile(userFile);
			return (B) this;
		}

		public B setProviderChainFile(File chainFile) {
			uniquidNodeConfiguration.setProviderChainFile(chainFile);
			return (B) this;
		}

		public B setUserChainFile(File userChainFile) {
			uniquidNodeConfiguration.setUserChainFile(userChainFile);
			return (B) this;
		}

		public B setRegisterFactory(RegisterFactory registerFactory) {
			uniquidNodeConfiguration.setRegisterFactory(registerFactory);
			return (B) this;
		}

		public B setNodeName(String machineName) {
			uniquidNodeConfiguration.setNodeName(machineName);
			return (B) this;
		}

		/**
		 * Build a new instance
		 * 
		 * @return
		 * @throws Exception
		 */
		public UniquidNodeImpl build() throws Exception {

			SecureRandom random = new SecureRandom();
			byte[] entropy = new byte[32];
			random.nextBytes(entropy);
			long creationTime = System.currentTimeMillis() / 1000;

			DeterministicSeed detSeed = new DeterministicSeed(entropy, "", creationTime);

			uniquidNodeConfiguration.setCreationTime(creationTime);
			uniquidNodeConfiguration.setDetSeed(detSeed);

			return createUniquidNode(uniquidNodeConfiguration);

		}
		
		public UniquidNodeImpl buildFromHexSeed(final String hexSeed, final long creationTime) throws Exception {

			DeterministicSeed detSeed = new DeterministicSeed("", org.bitcoinj.core.Utils.HEX.decode(hexSeed), "", creationTime);

			uniquidNodeConfiguration.setCreationTime(creationTime);
			uniquidNodeConfiguration.setDetSeed(detSeed);

			return createUniquidNode(uniquidNodeConfiguration);

		}

		public UniquidNodeImpl buildFromMnemonic(final String mnemonic, final long creationTime) throws Exception {

			DeterministicSeed detSeed = new DeterministicSeed(mnemonic, null, "", creationTime);

			uniquidNodeConfiguration.setCreationTime(creationTime);
			uniquidNodeConfiguration.setDetSeed(detSeed);

			return createUniquidNode(uniquidNodeConfiguration);

		}

		@SuppressWarnings("unchecked")
		protected T createUniquidNodeConfiguration() {
			return (T) new UniquidNodeConfiguration();
		}

		protected UniquidNodeImpl createUniquidNode(T uniquidNodeConfiguration) throws Exception {
			return new UniquidNodeImpl(uniquidNodeConfiguration);
		}

	}

	/*
	 * Begin of some other useful public method
	 */

	/**
	 * Change internal state
	 */
	public synchronized void setUniquidNodeState(final UniquidNodeState nodeState) {

		LOGGER.info("Changing node state to {}", nodeState.getClass());

		this.nodeState = nodeState;

		// Send event to listeners
		uniquidNodeEventService.onNodeStateChange(nodeState.getNodeState());
	}

	/*
	 * Calculate some public info
	 */
	private void calculatePublicInfo(final DeterministicSeed detSeed) {

		LOGGER.debug("Calculating public info");

		DeterministicKey deterministicKey = NodeUtils.createDeterministicKeyFromByteArray(detSeed.getSeedBytes());

		// LOGGER.info("START_NODE tpriv: " +
		// deterministicKey.serializePrivB58(networkParameters));
		// LOGGER.info("START_NODE tpub: " +
		// deterministicKey.serializePubB58(networkParameters));

		DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(deterministicKey);

		ImmutableList<ChildNumber> IMPRINTING_PATH = ImmutableList.of(new ChildNumber(44, true),
				new ChildNumber(0, true));

		DeterministicKey imprintingKey = deterministicHierarchy.get(IMPRINTING_PATH, true, true);

		publicKey = imprintingKey.serializePubB58(uniquidNodeConfiguration.getNetworkParameters());

		imprintingAddress = NodeUtils.calculateImprintAddress(imprintingKey,
				uniquidNodeConfiguration.getNetworkParameters());

	}

	/*
	 * Implementation of callback for blockchain events
	 */
	protected class UniquidNodeDownloadProgressTracker extends DownloadProgressTracker {

		@Override
		protected void startDownload(final int blocks) {

			UniquidNodeImpl.this.uniquidNodeEventService.onSyncStarted(blocks);

		}

		@Override
		protected void progress(final double pct, final int blocksSoFar, final Date date) {

			UniquidNodeImpl.this.uniquidNodeEventService.onSyncProgress(pct, blocksSoFar, date);

		}

		@Override
		public void doneDownload() {

			UniquidNodeImpl.this.uniquidNodeEventService.onSyncEnded();

		}

	}

	protected class UniquidPeerConnectionListener extends NativePeerEventListener {

		@Override
		public void onPeersDiscovered(Set<PeerAddress> peerAddresses) {
			UniquidNodeImpl.this.uniquidNodeEventService.onPeersDiscovered(peerAddresses);
		}

		@Override
		public void onPeerConnected(Peer peer, int peerCount) {
			UniquidNodeImpl.this.uniquidNodeEventService.onPeerConnected(peer, peerCount);

		}

		@Override
		public void onPeerDisconnected(Peer peer, int peerCount) {
			UniquidNodeImpl.this.uniquidNodeEventService.onPeerDisconnected(peer, peerCount);
		}

	}
	
	protected class UniquidWalletCoinsReceivedEventListener implements WalletCoinsReceivedEventListener {

		@Override
		public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
			
			org.bitcoinj.core.Context currentContext = new org.bitcoinj.core.Context(
					uniquidNodeConfiguration.getNetworkParameters());
			org.bitcoinj.core.Context.propagate(currentContext);

			UniquidNodeImpl.this.nodeState.onCoinsReceived(wallet, tx);
			
		}
		
	}
	
	protected class UniquidWalletCoinsSentEventListener implements WalletCoinsSentEventListener {

		@Override
		public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
			
			org.bitcoinj.core.Context currentContext = new org.bitcoinj.core.Context(
					uniquidNodeConfiguration.getNetworkParameters());
			org.bitcoinj.core.Context.propagate(currentContext);

			UniquidNodeImpl.this.nodeState.onCoinsSent(wallet, tx);
			
		}
		
	}
	
	protected class UniquidNodeStateContextImpl implements UniquidNodeStateContext {

		@Override
		public void setUniquidNodeState(UniquidNodeState nodeState) {
			UniquidNodeImpl.this.setUniquidNodeState(nodeState);
		}
		
		@Override
		public Wallet getUserWallet() {
			return UniquidNodeImpl.this.userWallet;
		}
		
		@Override
		public UniquidNodeEventService getUniquidNodeEventService() {
			return UniquidNodeImpl.this.uniquidNodeEventService;
		}
		
		@Override
		public RegisterFactory getRegisterFactory() {
			return UniquidNodeImpl.this.uniquidNodeConfiguration.getRegisterFactory();
		}
		
		@Override
		public String getPublicKeyValue() {
			return UniquidNodeImpl.this.publicKey;
		}
		
		@Override
		public Wallet getProviderWallet() {
			return UniquidNodeImpl.this.providerWallet;
		}
		
		@Override
		public NetworkParameters getNetworkParameters() {
			return UniquidNodeImpl.this.uniquidNodeConfiguration.getNetworkParameters();
		}
		
		@Override
		public Address getImprintingAddressValue() {
			return UniquidNodeImpl.this.imprintingAddress;
		}
		
	}

}