package com.uniquid.node.impl;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.uniquid.node.exception.NodeException;
import com.uniquid.register.provider.ProviderChannel;

/**
 * Implementation of an Uniquid Watching Node
 * 
 * @author Giuseppe Magnotta
 */
public class UniquidWatchingNodeImpl extends UniquidNodeImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeImpl.class.getName());

	protected UniquidWatchingNodeImpl(UniquidWatchingNodeConfiguration uniquidWatchingNodeConfiguration)
			throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		super(uniquidWatchingNodeConfiguration);

		this.publicKey = uniquidWatchingNodeConfiguration.getXpub();

	}

	@Override
	public void initNode() throws NodeException {
		try {
			if (getUniquidNodeConfiguration().getProviderFile().exists()
					&& !getUniquidNodeConfiguration().getProviderFile().isDirectory()
					&& getUniquidNodeConfiguration().getUserFile().exists()
					&& !getUniquidNodeConfiguration().getUserFile().isDirectory()) {

				// Wallets already present!
				providerWallet = Wallet.loadFromFile(getUniquidNodeConfiguration().getProviderFile());
				userWallet = Wallet.loadFromFile(getUniquidNodeConfiguration().getUserFile());
			} else {
				DeterministicKey key = DeterministicKey.deserializeB58(null, publicKey,
						getUniquidNodeConfiguration().getNetworkParameters());
				LOGGER.info(key.toAddress(getUniquidNodeConfiguration().getNetworkParameters()).toBase58());
				DeterministicHierarchy hierarchy = new DeterministicHierarchy(key);

				DeterministicKey k_orch = hierarchy.get(ImmutableList.of(new ChildNumber(0, false)), true, true);

				DeterministicKey k_machines = DeterministicKey.deserializeB58(null,
						k_orch.dropParent().serializePubB58(getUniquidNodeConfiguration().getNetworkParameters()),
						getUniquidNodeConfiguration().getNetworkParameters());
				DeterministicHierarchy h_machines = new DeterministicHierarchy(k_machines);

				DeterministicKey k_provider = h_machines.get(ImmutableList.of(new ChildNumber(0, false)), true, true);
				providerWallet = Wallet.fromWatchingKeyB58(getUniquidNodeConfiguration().getNetworkParameters(),
						k_provider.serializePubB58(getUniquidNodeConfiguration().getNetworkParameters()),
						getUniquidNodeConfiguration().getCreationTime(), ImmutableList.of(new ChildNumber(0, false)));
				providerWallet.setDescription("provider");
				providerWallet.saveToFile(getUniquidNodeConfiguration().getProviderFile());
				imprintingAddress = providerWallet.currentReceiveAddress();

				DeterministicKey k_user = h_machines.get(ImmutableList.of(new ChildNumber(1, false)), true, true);
				userWallet = Wallet.fromWatchingKeyB58(getUniquidNodeConfiguration().getNetworkParameters(),
						k_user.serializePubB58(getUniquidNodeConfiguration().getNetworkParameters()),
						getUniquidNodeConfiguration().getCreationTime(), ImmutableList.of(new ChildNumber(1, false)));
				userWallet.setDescription("user");
				userWallet.saveToFile(getUniquidNodeConfiguration().getUserFile());

			}
			// Retrieve contracts
			List<ProviderChannel> providerChannels = getUniquidNodeConfiguration().getRegisterFactory()
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
			providerWallet.addCoinsReceivedEventListener(this);
			providerWallet.addCoinsSentEventListener(this);
			userWallet.addCoinsReceivedEventListener(this);
			userWallet.addCoinsSentEventListener(this);
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

			uniquidNodeConfiguration.setXpub(xpub);
			uniquidNodeConfiguration.setCreationTime(creationTime);

			return createUniquidNode(uniquidNodeConfiguration);
			
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
