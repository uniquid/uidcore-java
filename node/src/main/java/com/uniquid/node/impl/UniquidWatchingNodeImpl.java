package com.uniquid.node.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.ListenerRegistration;
import org.bitcoinj.utils.Threading;
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
import com.uniquid.node.impl.utils.NodeUtils;
import com.uniquid.node.impl.utils.UniquidNodeStateUtils;
import com.uniquid.node.impl.utils.WalletUtils;
import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

/**
 * Implementation of an Uniquid Watching Node
 * 
 * @author Giuseppe Magnotta
 */
public class UniquidWatchingNodeImpl implements UniquidNode, WalletCoinsSentEventListener, WalletCoinsReceivedEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeImpl.class.getName());

    /** The current state of this Node */
    private UniquidNodeState nodeState;

    private NetworkParameters networkParameters;

    private File providerFile;
    private Wallet providerWallet;
    private File providerChainFile;

    private File userFile;
    private Wallet userWallet;
    private File userChainFile;

    private Address imprintingAddress;
    private String publicKey;
    private String machineName;

    private String xpub;

    private long creationTime;

    private RegisterFactory registerFactory;

    private CopyOnWriteArrayList<ListenerRegistration<UniquidNodeEventListener>> eventListeners;

    private UniquidWatchingNodeImpl (Builder builder){
        this.networkParameters = builder._params;
        this.providerFile = builder._providerFile;
        this.providerChainFile = builder._chainFile;
        this.userFile = builder._userFile;
        this.userChainFile = builder._userChainFile;
        this.registerFactory = builder._registerFactory;
        this.machineName = builder._machineName;
        this.eventListeners = new CopyOnWriteArrayList<>();
        this.xpub = builder._xpub;
        this.creationTime = builder._creationTime;

        setUniquidNodeState(new CreatedState());
    }

    @Override
    public String getImprintingAddress() {
        return nodeState.getImprintingAddress();
    }

    @Override
    public synchronized String getPublicKey() {
        return nodeState.getPublicKey();
    }

    @Override
    public synchronized String getNodeName() {
        return nodeState.getNodeName();
    }

    @Override
    public synchronized long getCreationTime() {
        return nodeState.getCreationTime();
    }

    @Override
    public String getHexSeed() {
        return null;
    }

    @Override
    public synchronized String getSpendableBalance() {
        return nodeState.getSpendableBalance();
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
                        xpub,
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
                setUniquidNodeState(new ReadyState());

            } else {

                // Jump to initializing
                setUniquidNodeState(new ImprintingState());

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
    public synchronized void updateNode() throws NodeException {
        // Start node sync
        for (final ListenerRegistration<UniquidNodeEventListener> listener : eventListeners) {

            listener.executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.listener.onSyncNodeStart();
                }
            });

        }

        // Provider wallet BC sync
        NodeUtils.syncBlockChain(networkParameters, providerWallet, providerChainFile,
                new UniquidNodeDownloadProgressTracker());


        // User wallet BC sync
        NodeUtils.syncBlockChain(networkParameters, userWallet, userChainFile,
                new UniquidNodeDownloadProgressTracker());

        try {

            providerWallet.saveToFile(providerFile);
            userWallet.saveToFile(userFile);

        } catch (IOException ex) {

            throw new NodeException("Exception while updating node", ex);

        }

        // Start node sync
        for (final ListenerRegistration<UniquidNodeEventListener> listener : eventListeners) {

            listener.executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.listener.onSyncNodeEnd();
                }
            });

        }
    }

    @Override
    public com.uniquid.node.UniquidNodeState getNodeState() {
        return nodeState.getNodeState();
    }

    @Override
    public synchronized void addUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener) {
        addUniquidNodeEventListener(Threading.SAME_THREAD, uniquidNodeEventListener);
    }

    private void addUniquidNodeEventListener(Executor executor, UniquidNodeEventListener listener) {
        // This is thread safe, so we don't need to take the lock.
        eventListeners.add(new ListenerRegistration<>(listener, executor));
    }

    @Override
    public void removeUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener) {
        eventListeners.remove(uniquidNodeEventListener);
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


    public static class Builder {

        private NetworkParameters _params;

        private File _providerFile;
        private File _userFile;
        private File _chainFile;
        private File _userChainFile;
        private String _xpub;
        private long _creationTime;

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

        public UniquidWatchingNodeImpl buildFromXpub(final String xpub, final long creationTime)
                throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

            _xpub = xpub;
            _creationTime = creationTime;

            return new UniquidWatchingNodeImpl(this);
        }
    }

    /**
     * Change internal state
     */
    private synchronized void setUniquidNodeState(final UniquidNodeState nodeState) {
        this.nodeState = nodeState;
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

            for (final ListenerRegistration<UniquidNodeEventListener> listener : eventListeners) {

                listener.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        listener.listener.onSyncStarted(blocks);
                    }
                });

            }

        }

        @Override
        protected void progress(final double pct, final int blocksSoFar, final Date date) {

            for (final ListenerRegistration<UniquidNodeEventListener> listener : eventListeners) {

                listener.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        listener.listener.onSyncProgress(pct, blocksSoFar, date);
                    }
                });

            }

        }

        @Override
        public void doneDownload() {

            for (final ListenerRegistration<UniquidNodeEventListener> listener : eventListeners) {

                listener.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        listener.listener.onSyncEnded();
                    }
                });

            }

        }

    }

    /**
     * Implementation of Strategy Design Pattern
     */
    private interface ContractStrategy {

        /**
         * Defines the creation of a contract
         *
         * @param tx transaction to manage
         * @throws Exception
         */
        public void manageContractCreation(final Transaction tx) throws Exception;

        /**
         * Defines the revocation of a contract
         *
         * @param tx transaction to manage
         * @throws Exception
         */
        public void manageContractRevocation(final Transaction tx) throws Exception;

    }

    /**
     * Abstract implementation of State pattern with some boilerplate code for
     * transactions callback
     */
    private abstract class AbstractContract implements ContractStrategy {

        @Override
        public void manageContractCreation(final Transaction tx) throws Exception {

            // Transaction already confirmed
            if (tx.getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)) {

                doRealContract(tx);

                // DONE

            } else {

                final TransactionConfidence.Listener listener = new TransactionConfidence.Listener() {

                    @Override
                    public void onConfidenceChanged(TransactionConfidence confidence, TransactionConfidence.Listener.ChangeReason reason) {

                        try {

                            if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)
                                    && reason.equals(TransactionConfidence.Listener.ChangeReason.TYPE)) {

                                doRealContract(tx);

                                tx.getConfidence().removeEventListener(this);

                                LOGGER.info("Contract Done!");

                            } else if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.DEAD)
                                    && reason.equals(TransactionConfidence.Listener.ChangeReason.TYPE)) {

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

        @Override
        public void manageContractRevocation(final Transaction tx) throws Exception {

            revokeRealContract(tx);

        }

        /**
         * Delegate to subclass the real contract creation
         *
         * @param tx
         * @throws Exception
         */
        public abstract void doRealContract(final Transaction tx) throws Exception;

        /**
         * Delegate to subclass the real contract revocation
         *
         * @param tx
         * @throws Exception
         */
        public abstract void revokeRealContract(final Transaction tx) throws Exception;

    }

    /**
     * Class that manage imprinting contracts
     *
     * @author giuseppe
     *
     */
    private class ImprintingContract extends AbstractContract {

        private static final String CONTRACT_FUNCTION = "00000000400000000000000000000000000000";

        @Override
        public void doRealContract(final Transaction tx) throws Exception {

            // Retrieve sender
            String sender = tx.getInput(0).getFromAddress().toBase58();

            // Check output
            List<TransactionOutput> transactionOutputs = tx.getOutputs();
            for (TransactionOutput to : transactionOutputs) {

                Address address = to.getAddressFromP2PKHScript(networkParameters);
                if (address != null && address.equals(imprintingAddress)) {

                    // This is our imprinter!!!

                    ProviderRegister providerRegister = registerFactory.getProviderRegister();

                    // Create provider channel
                    final ProviderChannel providerChannel = new ProviderChannel();
                    providerChannel.setUserAddress(sender);
                    providerChannel.setProviderAddress(imprintingAddress.toBase58());
                    providerChannel.setBitmask(CONTRACT_FUNCTION);
                    providerChannel.setRevokeAddress("IMPRINTING");
                    providerChannel.setRevokeTxId(tx.getHashAsString());
                    providerChannel.setCreationTime(tx.getUpdateTime().getTime()/1000);

                    // persist channel
                    providerRegister.insertChannel(providerChannel);

                    // We can move now to ReadyState
                    setUniquidNodeState(new ReadyState());

                    // Send event to listeners
                    for (final ListenerRegistration<UniquidNodeEventListener> listener : eventListeners) {

                        listener.executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                listener.listener.onNodeStateChange(com.uniquid.node.UniquidNodeState.READY);
                            }
                        });

                    }

                    for (final ListenerRegistration<UniquidNodeEventListener> listener : eventListeners) {

                        listener.executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                listener.listener.onProviderContractCreated(providerChannel);
                            }
                        });

                    }

                    LOGGER.info("Machine IMPRINTED!");

                    break;

                }

            }

        }

        @Override
        public void revokeRealContract(final Transaction tx) throws Exception {
            // DO NOTHING
        }

    }

    /**
     * Class that manage provider contracts
     *
     * @author giuseppe
     *
     */
    private class ProviderContract extends AbstractContract {

        @Override
        public void doRealContract(final Transaction tx) throws Exception {

            List<TransactionOutput> transactionOutputs = tx.getOutputs();

            if (transactionOutputs.size() != 4) {
                LOGGER.error("Contract not valid! output size is not 4");
                return;
            }

            Script script = tx.getInput(0).getScriptSig();
            Address providerAddress = new Address(networkParameters,
                    org.bitcoinj.core.Utils.sha256hash160(script.getPubKey()));

            if (!providerWallet.isPubKeyHashMine(providerAddress.getHash160())) {
                LOGGER.error("Contract not valid! We are not the provider");
                return;
            }

            List<TransactionOutput> ts = new ArrayList<>(transactionOutputs);

            Address userAddress = ts.get(0).getAddressFromP2PKHScript(networkParameters);

            // We are provider!!!
            if (userAddress == null) {
                LOGGER.error("Contract not valid! User address is null");
                return;
            }

            if (!WalletUtils.isValidOpReturn(tx)) {
                LOGGER.error("Contract not valid! OPRETURN not valid");
                return;
            }

            Address revoke = ts.get(2).getAddressFromP2PKHScript(networkParameters);
            if (revoke == null /*
								 * ||
								 * !WalletUtils.isUnspent(tx.getHashAsString(),
								 * revoke.toBase58())
								 */) {
                LOGGER.error("Contract not valid! Revoke address is null");
                return;
            }

            // Create provider channel
            final ProviderChannel providerChannel = new ProviderChannel();
            providerChannel.setProviderAddress(providerAddress.toBase58());
            providerChannel.setUserAddress(userAddress.toBase58());
            providerChannel.setRevokeAddress(revoke.toBase58());
            providerChannel.setRevokeTxId(tx.getHashAsString());
            providerChannel.setCreationTime(tx.getUpdateTime().getTime()/1000);

            String opreturn = WalletUtils.getOpReturn(tx);

            byte[] op_to_byte = Hex.decode(opreturn);

            byte[] bitmask = Arrays.copyOfRange(op_to_byte, 0, 19);

            // encode to be saved on db
            String bitmaskToString = new String(Hex.encode(bitmask));

            // persist
            providerChannel.setBitmask(bitmaskToString);

            try {

                ProviderRegister providerRegister = registerFactory.getProviderRegister();

                List<ProviderChannel> channels = providerRegister.getAllChannels();

                // If this is the first "normal" contract then remove the
                // imprinting
                // contract
                if (channels.size() == 1 && channels.get(0).getRevokeAddress().equals("IMPRINTING")) {

                    providerRegister.deleteChannel(channels.get(0));

                }

                providerRegister.insertChannel(providerChannel);

            } catch (Exception e) {

                LOGGER.error("Exception while inserting provider register", e);

                throw e;

            }

            // Inform listeners
            for (final ListenerRegistration<UniquidNodeEventListener> listener : eventListeners) {

                listener.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        listener.listener.onProviderContractCreated(providerChannel);
                    }
                });

            }

        }

        @Override
        public void revokeRealContract(final Transaction tx) throws Exception {

            // Retrieve sender
            String sender = tx.getInput(0).getFromAddress().toBase58();

            ProviderRegister providerRegister;
            try {

                providerRegister = registerFactory.getProviderRegister();
                final ProviderChannel channel = providerRegister.getChannelByRevokeAddress(sender);

                if (channel != null) {

                    LOGGER.info("Found a contract to revoke!");
                    // contract revoked
                    providerRegister.deleteChannel(channel);

                    LOGGER.info("Contract revoked! " + channel);

                    for (final ListenerRegistration<UniquidNodeEventListener> listener : eventListeners) {

                        listener.executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                listener.listener.onProviderContractRevoked(channel);
                            }
                        });

                    }

                } else {

                    LOGGER.warn("No contract found to revoke!");
                }

            } catch (Exception e) {

                LOGGER.error("Exception", e);

            }
        }

    }

    private class UserContract extends AbstractContract {

        @Override
        public void doRealContract(final Transaction tx) throws Exception {

            List<TransactionOutput> transactionOutputs = tx.getOutputs();

            if (transactionOutputs.size() != 4) {
                LOGGER.error("Contract not valid! size is not 4");
                return;
            }

            Script script = tx.getInput(0).getScriptSig();
            Address providerAddress = new Address(networkParameters,
                    org.bitcoinj.core.Utils.sha256hash160(script.getPubKey()));

            List<TransactionOutput> ts = new ArrayList<>(transactionOutputs);

            Address userAddress = ts.get(0).getAddressFromP2PKHScript(networkParameters);

            if (userAddress == null || !userWallet.isPubKeyHashMine(userAddress.getHash160())) {
                LOGGER.error("Contract not valid! User address is null or we are not the user");
                return;
            }

            if (!WalletUtils.isValidOpReturn(tx)) {
                LOGGER.error("Contract not valid! OPRETURN not valid");
                return;
            }

            Address revoke = ts.get(2).getAddressFromP2PKHScript(networkParameters);
            if (revoke == null /*|| !WalletUtils.isUnspent(tx.getHashAsString(), revoke.toBase58())*/) {
                LOGGER.error("Contract not valid! Revoke address is null or contract revoked");
                return;
            }

            String providerName = WalletUtils.retrieveNameFromProvider(providerAddress.toBase58());
            if (providerName == null) {
                LOGGER.error("Contract not valid! Provider name is null");
                return;
            }

            // Create channel
            final UserChannel userChannel = new UserChannel();
            userChannel.setProviderAddress(providerAddress.toBase58());
            userChannel.setUserAddress(userAddress.toBase58());
            userChannel.setProviderName(providerName);
            userChannel.setRevokeAddress(revoke.toBase58());
            userChannel.setRevokeTxId(tx.getHashAsString());

            String opreturn = WalletUtils.getOpReturn(tx);

            byte[] op_to_byte = Hex.decode(opreturn);

            byte[] bitmask = Arrays.copyOfRange(op_to_byte, 0, 19);

            // encode to be saved on db
            String bitmaskToString = new String(Hex.encode(bitmask));

            userChannel.setBitmask(bitmaskToString);

            try {

                UserRegister userRegister = registerFactory.getUserRegister();

                userRegister.insertChannel(userChannel);

                LOGGER.info("inserted user register: " + userRegister);

            } catch (Exception e) {

                LOGGER.error("Exception while inserting userChannel", e);

                throw e;

            }

            for (final ListenerRegistration<UniquidNodeEventListener> listener : eventListeners) {

                listener.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        listener.listener.onUserContractCreated(userChannel);
                    }
                });

            }

        }

        @Override
        public void revokeRealContract(final Transaction tx) throws Exception {
            // DO NOTHIG
        }

    }

    /**
     * Implementation of State Design pattern: most public methods will be delegated to current state
     *
     */
    private interface UniquidNodeState {

        void onCoinsSent(final Wallet wallet, final Transaction tx);

        void onCoinsReceived(final Wallet wallet, final Transaction tx);

        com.uniquid.node.UniquidNodeState getNodeState();

        String getImprintingAddress();

        String getPublicKey();

        String getNodeName();

        long getCreationTime();

        String getSpendableBalance();

        Wallet getProviderWallet();

        Wallet getUserWallet();

    }

    /**
     * Fake state to be used when new instance is created
     */
    private class CreatedState implements UniquidNodeState {

        @Override
        public void onCoinsSent(Wallet wallet, Transaction tx) {
            throw new IllegalStateException();

        }

        @Override
        public void onCoinsReceived(Wallet wallet, Transaction tx) {
            throw new IllegalStateException();

        }

        @Override
        public com.uniquid.node.UniquidNodeState getNodeState() {
            return com.uniquid.node.UniquidNodeState.CREATED;
        }

        @Override
        public String getImprintingAddress() {
            throw new IllegalStateException();
        }

        @Override
        public String getPublicKey() {
            throw new IllegalStateException();
        }

        @Override
        public String getNodeName() {
            throw new IllegalStateException();
        }

        @Override
        public long getCreationTime() {
            throw new IllegalStateException();
        }

        @Override
        public String getSpendableBalance() {
            throw new IllegalStateException();
        }

        @Override
        public Wallet getProviderWallet() {
            throw new IllegalStateException();
        }

        @Override
        public Wallet getUserWallet() {
            throw new IllegalStateException();
        }

    }

    /**
     * Implementation of State Design pattern
     */
    private class ImprintingState implements UniquidNodeState {

        private boolean alreadyImprinted;

        public ImprintingState() {

            this.alreadyImprinted = false;

        }

        @Override
        public void onCoinsSent(final Wallet wallet, final Transaction tx) {

            LOGGER.info("We sent coins from a wallet that we don't expect!");

        }

        @Override
        public void onCoinsReceived(final Wallet wallet, final Transaction tx) {

            if (wallet.equals(providerWallet) || "provider".equalsIgnoreCase(wallet.getDescription())) {

                LOGGER.info("Received coins on provider wallet");

                try {

                    // If is imprinting transaction...
                    if (UniquidNodeStateUtils.isValidImprintingTransaction(tx, networkParameters, imprintingAddress)
                            && !alreadyImprinted) {

                        LOGGER.info("Imprinting contract");

                        // imprint!
                        ContractStrategy contractStrategy = new ImprintingContract();
                        contractStrategy.manageContractCreation(tx);

                        alreadyImprinted = true;

                    } else {

                        LOGGER.info("Invalid contract");

                    }

                } catch (Exception ex) {

                    LOGGER.error("Exception while imprinting", ex);

                }

            } else if (wallet.equals(userWallet) || "user".equalsIgnoreCase(wallet.getDescription())) {

                LOGGER.info("Received coins on user wallet");

                try {

                    ContractStrategy contractStrategy = new UserContract();
                    contractStrategy.manageContractCreation(tx);

                } catch (Exception ex) {

                    LOGGER.error("Exception while creating user contract", ex);

                }

            } else {

                LOGGER.warn("We received coins on a wallet that we don't expect!");

            }

        }

        @Override
        public com.uniquid.node.UniquidNodeState getNodeState() {

            return com.uniquid.node.UniquidNodeState.IMPRINTING;

        }

        @Override
        public String getImprintingAddress() {
            return imprintingAddress.toBase58();
        }

        @Override
        public String getPublicKey() {
            return publicKey;
        }

        @Override
        public String getNodeName() {
            return machineName;
        }

        @Override
        public long getCreationTime() {
            return providerWallet.getKeyChainSeed().getCreationTimeSeconds();
        }

        @Override
        public String getSpendableBalance() {
            return providerWallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).toFriendlyString();
        }

        @Override
        public Wallet getProviderWallet() {
            return providerWallet;
        }

        @Override
        public Wallet getUserWallet() {
            return userWallet;
        }

    }

    /**
     * Class to represents the ready state
     *
     */
    private class ReadyState implements UniquidNodeState {

        @Override
        public void onCoinsSent(final Wallet wallet, final Transaction tx) {

            // We sent some coins. Probably we created a contract as Provider
            if (wallet.equals(providerWallet) || "provider".equalsIgnoreCase(wallet.getDescription())) {

                LOGGER.info("Sent coins from provider wallet");

                try {

                    LOGGER.info("Creating provider contract!");
                    ContractStrategy contractStrategy = new ProviderContract();
                    contractStrategy.manageContractCreation(tx);

                } catch (Exception ex) {

                    LOGGER.error("Exception while creating provider contract", ex);

                }

            } else if (wallet.equals(userWallet) || "user".equalsIgnoreCase(wallet.getDescription())) {

                LOGGER.info("Sent coins from user wallet");

                // if (UniquidNodeStateUtils.isValidRevokeContract(tx,
                // nodeStateContext)) {

                try {
                    LOGGER.info("Revoking contract!");
                    ContractStrategy contractStrategy = new ProviderContract();
                    contractStrategy.manageContractRevocation(tx);

                } catch (Exception ex) {

                    LOGGER.error("Exception while revoking provider contract", ex);

                }

            } else {

                LOGGER.info("We sent coins from a wallet that we don't expect!");

            }

        }

        @Override
        public void onCoinsReceived(final Wallet wallet, final Transaction tx) {

            // Received a contract!!!
            if (wallet.equals(providerWallet) || "provider".equalsIgnoreCase(wallet.getDescription())) {

                LOGGER.info("Received coins on provider wallet");

                // If is imprinting transaction...
                if (UniquidNodeStateUtils.isValidImprintingTransaction(tx, networkParameters, imprintingAddress)) {

                    // imprint!
                    LOGGER.warn("Attention! Another machine tried to imprint US! Skip request!");

                } else if (UniquidNodeStateUtils.isValidRevokeContract(tx, registerFactory)) {

                    try {
                        // Revoking a contract will move coins from provider wallet to another provider address
                        LOGGER.info("Revoking contract!");

                        ContractStrategy contractStrategy = new ProviderContract();
                        contractStrategy.manageContractRevocation(tx);

                    } catch (Exception ex) {

                        LOGGER.error("Exception", ex);
                    }

                } else {

                    LOGGER.info("Unknown contract");

                }

            } else if (wallet.equals(userWallet) || "user".equalsIgnoreCase(wallet.getDescription())) {

                LOGGER.info("Received coins on user wallet");

                try {

                    LOGGER.info("Creating user contract!");
                    ContractStrategy contractStrategy = new UserContract();
                    contractStrategy.manageContractCreation(tx);

                } catch (Exception ex) {

                    LOGGER.error("Exception while creating provider contract", ex);

                }

            } else {

                LOGGER.warn("We received coins on a wallet that we don't expect!");

            }

        }

        @Override
        public com.uniquid.node.UniquidNodeState getNodeState() {

            return com.uniquid.node.UniquidNodeState.READY;

        }

        @Override
        public String getImprintingAddress() {
            return imprintingAddress.toBase58();
        }

        @Override
        public String getPublicKey() {
            return publicKey;
        }

        @Override
        public String getNodeName() {
            return machineName;
        }

        @Override
        public long getCreationTime() {
            return providerWallet.getKeyChainSeed().getCreationTimeSeconds();
        }

        @Override
        public String getSpendableBalance() {
            return providerWallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).toFriendlyString();
        }

        @Override
        public Wallet getProviderWallet() {
            return providerWallet;
        }

        @Override
        public Wallet getUserWallet() {
            return userWallet;
        }

    }
    
}
