package com.uniquid.node.impl;

import java.util.List;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.uniquid.node.exception.NodeException;
import com.uniquid.node.impl.utils.NodeUtils;
import com.uniquid.register.provider.ProviderChannel;

/**
 * Implementation of an Uniquid Watching Node
 * 
 * @author Giuseppe Magnotta
 */
public class UniquidWatchingNodeImpl extends UniquidNodeImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeImpl.class.getName());

	protected UniquidWatchingNodeImpl(UniquidWatchingNodeConfiguration uniquidWatchingNodeConfiguration) 
			throws NodeException {

		super(uniquidWatchingNodeConfiguration);

		this.publicKey = uniquidWatchingNodeConfiguration.getXpub();

	}

	@Override
	public void initNode() throws NodeException {
		try {
			if (uniquidNodeConfiguration.getProviderFile().exists()
					&& !uniquidNodeConfiguration.getProviderFile().isDirectory()
					&& uniquidNodeConfiguration.getUserFile().exists()
					&& !uniquidNodeConfiguration.getUserFile().isDirectory()) {

				// Wallets already present!
				providerWallet = Wallet.loadFromFile(uniquidNodeConfiguration.getProviderFile());
				userWallet = Wallet.loadFromFile(uniquidNodeConfiguration.getUserFile());
			} else {
				DeterministicKey key = DeterministicKey.deserializeB58(null, publicKey,
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
			List<ProviderChannel> providerChannels = uniquidNodeConfiguration.getRegisterFactory()
					.getProviderRegister().getAllChannels();

			
			LOGGER.info("providerChannels size: " + providerChannels.size());

			// If there is at least 1 contract, then we are ready
			if (providerChannels.size() > 0) {

				// Jump to ready state
				setUniquidNodeState(getReadyState());

			} else {

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
	}

	@Override
	public String signTransaction(String s_tx, String path) throws NodeException {

		throw new NodeException("This node can't sign a transaction");

	}

	@Override
	public synchronized String getHexSeed() {
		return null;
	}
	
	/**
	 * Builder for WatchingNodeBuilder
	 */
	public static class WatchingNodeBuilder extends UniquidNodeImpl.UniquidNodeBuilder<WatchingNodeBuilder, UniquidWatchingNodeConfiguration> {

		public UniquidWatchingNodeImpl buildFromXpub(final String xpub, final long creationTime) throws Exception {

			_uniquidNodeConfiguration.setXpub(xpub);
			_uniquidNodeConfiguration.setCreationTime(creationTime);

			return createUniquidNode(_uniquidNodeConfiguration);
			
		}
		
		@Override
		public UniquidNodeImpl build() throws Exception {

			throw new Exception("Can't create node with random seed");

		}
		
		@Override
		public UniquidNodeImpl buildFromMnemonic(final String mnemonic, final long creationTime) throws Exception {

			throw new Exception("Can't create node from mnemonic");

		}

		@Override
		protected UniquidWatchingNodeConfiguration createUniquidNodeConfiguration() {
			return new UniquidWatchingNodeConfiguration();
		}
		
		@Override
		protected UniquidWatchingNodeImpl createUniquidNode(UniquidWatchingNodeConfiguration uniquidNodeConfiguration) throws Exception {
			return new UniquidWatchingNodeImpl(uniquidNodeConfiguration);
		}
		
	}

}
