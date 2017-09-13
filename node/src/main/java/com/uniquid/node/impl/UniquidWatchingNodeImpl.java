package com.uniquid.node.impl;

import java.io.File;
import java.io.IOException;
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
import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;

/**
 * Implementation of an Uniquid Node with BitcoinJ library
 */
public class UniquidWatchingNodeImpl<T extends UniquidNodeConfiguration> implements UniquidNode {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidWatchingNodeImpl.class.getName());

	/** The current state of this Node */
	private UniquidNodeState nodeState;

	protected Wallet providerWallet;
	protected Wallet userWallet;

	protected Address imprintingAddress;

	protected final T uniquidNodeConfiguration;

	protected final UniquidNodeEventService uniquidNodeEventService;

	/**
	 * Creates a new instance
	 * 
	 * @param uniquidNodeConfiguration
	 * @throws NodeException
	 */
	protected UniquidWatchingNodeImpl(T uniquidNodeConfiguration) throws NodeException {

		this.uniquidNodeConfiguration = uniquidNodeConfiguration;
		this.uniquidNodeEventService = new UniquidNodeEventService();

		setUniquidNodeState(getCreatedState());

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
		return uniquidNodeConfiguration.getPublicKey();
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
	@Deprecated
	public synchronized String getHexSeed() {
		return null;
	}

	@Override
	public synchronized String getSpendableBalance() {
		return providerWallet.getBalance(Wallet.BalanceType.AVAILABLE).toFriendlyString();
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

				LOGGER.info("Generating new node from xpub");

				DeterministicKey key = DeterministicKey.deserializeB58(null, uniquidNodeConfiguration.getPublicKey(),
						uniquidNodeConfiguration.getNetworkParameters());
				LOGGER.info(key.toAddress(uniquidNodeConfiguration.getNetworkParameters()).toBase58());
				DeterministicHierarchy hierarchy = new DeterministicHierarchy(key);

				DeterministicKey k_orch = hierarchy.get(ImmutableList.of(new ChildNumber(0, false)), true, true);

				DeterministicKey k_machines = DeterministicKey.deserializeB58(null,
						k_orch.dropParent().serializePubB58(uniquidNodeConfiguration.getNetworkParameters()),
						uniquidNodeConfiguration.getNetworkParameters());
				DeterministicHierarchy h_machines = new DeterministicHierarchy(k_machines);

				DeterministicKey k_provider = h_machines.get(ImmutableList.of(new ChildNumber(0, false)), true, true);
				providerWallet = Wallet.fromWatchingKeyB58(uniquidNodeConfiguration.getNetworkParameters(),
						k_provider.serializePubB58(uniquidNodeConfiguration.getNetworkParameters()),
						uniquidNodeConfiguration.getCreationTime(), ImmutableList.of(new ChildNumber(0, false)));
				providerWallet.setDescription("provider");
				providerWallet.saveToFile(uniquidNodeConfiguration.getProviderFile());
				imprintingAddress = providerWallet.currentReceiveAddress();

				DeterministicKey k_user = h_machines.get(ImmutableList.of(new ChildNumber(1, false)), true, true);
				userWallet = Wallet.fromWatchingKeyB58(uniquidNodeConfiguration.getNetworkParameters(),
						k_user.serializePubB58(uniquidNodeConfiguration.getNetworkParameters()),
						uniquidNodeConfiguration.getCreationTime(), ImmutableList.of(new ChildNumber(1, false)));
				userWallet.setDescription("user");
				userWallet.saveToFile(uniquidNodeConfiguration.getUserFile());

				imprintingAddress = NodeUtils.calculateImprintAddress(key,
						uniquidNodeConfiguration.getNetworkParameters());

			}

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
	
	@Override
	public synchronized String signTransaction(final String s_tx, final List<String> paths) throws NodeException {

		throw new NodeException("This node can't sign a transaction");

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

	/*
	 * End of public part for implementing UniquidNode
	 *
	 */
	
	/**
	 * Returns the Provider Wallet
	 * @return
	 */
	public Wallet getProviderWallet() {
		return providerWallet;
	}

	/**
	 * Return the User Wallet
	 * @return
	 */
	public Wallet getUserWallet() {
		return userWallet;
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

		return new ReadyState<T>(new UniquidNodeStateContextImpl());

	}

	/**
	 * Return the {@link com.uniquid.node.UniquidNodeState} that manages the
	 * {@link com.uniquid.node.UniquidNodeState.IMPRINTING}
	 * 
	 * @return
	 */
	protected UniquidNodeState getImprintingState() {

		return new ImprintingState<T>(new UniquidNodeStateContextImpl(), getReadyState());

	}
	
	/**
	 * Change internal node state
	 */
	protected synchronized void setUniquidNodeState(final UniquidNodeState nodeState) {

		this.nodeState = nodeState;

		LOGGER.info("Node state changed to {}", nodeState.getNodeState());

		// Send event to listeners
		uniquidNodeEventService.onNodeStateChange(nodeState.getNodeState());
	}
	
	/**
	 * Builder for UniquidNodeImpl
	 */
	public static class WatchingNodeBuilder<B extends WatchingNodeBuilder<B, T, C>, T extends UniquidNodeConfiguration, C extends UniquidNodeConfigurationImpl> {
		
		protected C _uniquidNodeConfiguration;

		public WatchingNodeBuilder() {
			this._uniquidNodeConfiguration = createUniquidNodeConfiguration();
		}
		
		@SuppressWarnings("unchecked")
		public T getUniquidNodeConfiguration() {
			return (T) _uniquidNodeConfiguration;
		}

		@SuppressWarnings("unchecked")
		public B setNetworkParameters(NetworkParameters params) {
			_uniquidNodeConfiguration.setNetworkParameters(params);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B setProviderFile(File providerFile) {
			_uniquidNodeConfiguration.setProviderFile(providerFile);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B setUserFile(File userFile) {
			_uniquidNodeConfiguration.setUserFile(userFile);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B setProviderChainFile(File chainFile) {
			_uniquidNodeConfiguration.setProviderChainFile(chainFile);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B setUserChainFile(File userChainFile) {
			_uniquidNodeConfiguration.setUserChainFile(userChainFile);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B setRegisterFactory(RegisterFactory registerFactory) {
			_uniquidNodeConfiguration.setRegisterFactory(registerFactory);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B setNodeName(String machineName) {
			_uniquidNodeConfiguration.setNodeName(machineName);
			return (B) this;
		}
		
		@SuppressWarnings("unchecked")
		public B setRegistryUrl(String registryUrl) {
			_uniquidNodeConfiguration.setRegistryUrl(registryUrl);
			return (B) this;
		}

		/**
		 * Build a new instance
		 * 
		 * @return
		 * @throws Exception
		 */
		@SuppressWarnings("unchecked")
		public UniquidWatchingNodeImpl<T> buildFromXpub(final String xpub, final long creationTime) throws NodeException {

			_uniquidNodeConfiguration.setPublicKey(xpub);
			_uniquidNodeConfiguration.setCreationTime(creationTime);

			return createUniquidNode((T) _uniquidNodeConfiguration);

		}
		
		@SuppressWarnings("unchecked")
		protected C createUniquidNodeConfiguration() {
			return (C) new UniquidNodeConfigurationImpl();
		}

		protected UniquidWatchingNodeImpl<T> createUniquidNode(T uniquidNodeConfiguration) throws NodeException {
			return new UniquidWatchingNodeImpl<T>(uniquidNodeConfiguration);
		}

	}

	/**
	 * Default implementation of BitcoinJ callback to receive blockchain events
	 */
	protected class UniquidNodeDownloadProgressTracker extends DownloadProgressTracker {
		
		public UniquidNodeDownloadProgressTracker() {
			// protected class have default constructor protected.
		}

		@Override
		protected void startDownload(final int blocks) {

			UniquidWatchingNodeImpl.this.uniquidNodeEventService.onSyncStarted(blocks);

		}

		@Override
		protected void progress(final double pct, final int blocksSoFar, final Date date) {

			UniquidWatchingNodeImpl.this.uniquidNodeEventService.onSyncProgress(pct, blocksSoFar, date);

		}

		@Override
		public void doneDownload() {

			UniquidWatchingNodeImpl.this.uniquidNodeEventService.onSyncEnded();

		}

	}

	/**
	 * Default implementation of BitcoinJ callback to receive events regarding peer connection
	 */
	protected class UniquidPeerConnectionListener extends NativePeerEventListener {
		
		public UniquidPeerConnectionListener() {
			// protected class have default constructor protected.
		}

		@Override
		public void onPeersDiscovered(Set<PeerAddress> peerAddresses) {
			UniquidWatchingNodeImpl.this.uniquidNodeEventService.onPeersDiscovered(peerAddresses);
		}

		@Override
		public void onPeerConnected(Peer peer, int peerCount) {
			UniquidWatchingNodeImpl.this.uniquidNodeEventService.onPeerConnected(peer, peerCount);

		}

		@Override
		public void onPeerDisconnected(Peer peer, int peerCount) {
			UniquidWatchingNodeImpl.this.uniquidNodeEventService.onPeerDisconnected(peer, peerCount);
		}

	}
	
	/**
	 * Default implementation of BitcoinJ callback to receive event of coins received
	 */
	protected class UniquidWalletCoinsReceivedEventListener implements WalletCoinsReceivedEventListener {
		
		public UniquidWalletCoinsReceivedEventListener() {
			// protected class have default constructor protected.
		}

		@Override
		public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
			
			org.bitcoinj.core.Context currentContext = new org.bitcoinj.core.Context(
					uniquidNodeConfiguration.getNetworkParameters());
			org.bitcoinj.core.Context.propagate(currentContext);

			UniquidWatchingNodeImpl.this.nodeState.onCoinsReceived(wallet, tx);
			
		}
		
	}
	
	/**
	 * Default implementation of BitcoinJ callback to receive event of coins sent
	 */
	protected class UniquidWalletCoinsSentEventListener implements WalletCoinsSentEventListener {
		
		public UniquidWalletCoinsSentEventListener() {
			// protected class have default constructor protected.
		}

		@Override
		public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
			
			org.bitcoinj.core.Context currentContext = new org.bitcoinj.core.Context(
					uniquidNodeConfiguration.getNetworkParameters());
			org.bitcoinj.core.Context.propagate(currentContext);

			UniquidWatchingNodeImpl.this.nodeState.onCoinsSent(wallet, tx);
			
		}
		
	}
	
	/**
	 * Default implementation of UniquidNodeStateContext.
	 * This is an inner class: subclasses of UniquidNodeImpl can access it and avoid to copy/pass each time parameters.
	 * 
	 * Remember that to access UniquidNodeImpl field you must use "UniquidNodeImpl.this.fied" as access
	 */
	protected class UniquidNodeStateContextImpl implements UniquidNodeStateContext<T> {
		
		public UniquidNodeStateContextImpl() {
			// protected class have default constructor protected.
		}

		@Override
		public void setUniquidNodeState(UniquidNodeState nodeState) {
			UniquidWatchingNodeImpl.this.setUniquidNodeState(nodeState);
		}
		
		@Override
		public Wallet getUserWallet() {
			return UniquidWatchingNodeImpl.this.userWallet;
		}
		
		@Override
		public UniquidNodeEventService getUniquidNodeEventService() {
			return UniquidWatchingNodeImpl.this.uniquidNodeEventService;
		}
		
		@Override
		public String getPublicKey() {
			return UniquidWatchingNodeImpl.this.getPublicKey();
		}
		
		@Override
		public Wallet getProviderWallet() {
			return UniquidWatchingNodeImpl.this.providerWallet;
		}
		
		@Override
		public Address getImprintingAddress() {
			return UniquidWatchingNodeImpl.this.imprintingAddress;
		}

		@Override
		public T getUniquidNodeConfiguration() {
			return UniquidWatchingNodeImpl.this.uniquidNodeConfiguration;
		}
		
	}

}