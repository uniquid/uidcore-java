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

	protected UniquidWatchingNodeImpl(Builder builder)
			throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {
		
		super(builder);
		
		this.creationTime = builder._creationTime;
		this.publicKey = builder._xpub;
		
	}

    @Override
    public void initNode() throws NodeException {
        try{
            if (providerFile.exists() && !providerFile.isDirectory() && userFile.exists() && !userFile.isDirectory()) {

                // Wallets already present!
                providerWallet = Wallet.loadFromFile(providerFile);
                userWallet = Wallet.loadFromFile(userFile);
            } else {
                DeterministicKey key = DeterministicKey.deserializeB58(
                        null,
                        publicKey,
                        networkParameters
                );
                LOGGER.info(key.toAddress(networkParameters).toBase58());
                DeterministicHierarchy hierarchy = new DeterministicHierarchy(key);

                DeterministicKey k_orch = hierarchy.get(
                        ImmutableList.of(new ChildNumber(0, false)),
                        true,
                        true
                );

                DeterministicKey k_machines = DeterministicKey.deserializeB58(
                        null,
                        k_orch.dropParent().serializePubB58(networkParameters),
                        networkParameters
                );
                DeterministicHierarchy h_machines = new DeterministicHierarchy(k_machines);

                DeterministicKey k_provider = h_machines.get(
                        ImmutableList.of(new ChildNumber(0, false)),
                        true,
                        true
                );
                providerWallet = Wallet.fromWatchingKeyB58(
                        networkParameters,
                        k_provider.serializePubB58(networkParameters),
                        creationTime,
                        ImmutableList.of(new ChildNumber(0, false))
                );
                providerWallet.setDescription("provider");
                providerWallet.saveToFile(providerFile);
                imprintingAddress = providerWallet.currentReceiveAddress();

                DeterministicKey k_user = h_machines.get(
                        ImmutableList.of(new ChildNumber(1, false)),
                        true,
                        true
                );
                userWallet = Wallet.fromWatchingKeyB58(
                        networkParameters,
                        k_user.serializePubB58(networkParameters),
                        creationTime,
                        ImmutableList.of(new ChildNumber(1, false))
                );
                userWallet.setDescription("user");
                userWallet.saveToFile(userFile);

            }
            // Retrieve contracts
            List<ProviderChannel> providerChannels = registerFactory.getProviderRegister().getAllChannels();


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

    public static class Builder extends UniquidNodeImpl.Builder<Builder> {

        private String _xpub;
        private long _creationTime;

        public UniquidWatchingNodeImpl buildFromXpub(final String xpub, final long creationTime)
                throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

            _xpub = xpub;
            _creationTime = creationTime;

            return new UniquidWatchingNodeImpl(this);
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
    
}
