package com.uniquid.node.impl;

import static org.bitcoinj.core.Utils.HEX;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.Listener;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStoreException;
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
 * Implementation of an Uniquid Node
 * 
 * @author Giuseppe Magnotta
 */
public class UniquidNodeImpl implements UniquidNode, WalletCoinsSentEventListener, WalletCoinsReceivedEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeImpl.class.getName());

	public static ImmutableList<ChildNumber> BIP44_ACCOUNT_PROVIDER = ImmutableList.of(new ChildNumber(44, true),
			new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(0, false));

	public static ImmutableList<ChildNumber> BIP44_ACCOUNT_USER = ImmutableList.of(new ChildNumber(44, true),
			new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(1, false));

	public static ImmutableList<ChildNumber> BIP44_ACCOUNT_ORCHESTRATOR = ImmutableList.of(new ChildNumber(44, true),
			new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(2, false), new ChildNumber(0, false));

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

	private RegisterFactory registerFactory;

	private List<UniquidNodeEventListener> eventListeners;

	private UniquidNodeImpl(Builder builder)
			throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		this.networkParameters = builder._params;
		this.providerFile = builder._providerFile;
		this.providerChainFile = builder._chainFile;
		this.userFile = builder._userFile;
		this.userChainFile = builder._userChainFile;
		this.registerFactory = builder._registerFactory;
		this.machineName = builder._machineName;
		this.eventListeners = new ArrayList<UniquidNodeEventListener>();

	}

	/*
	 * Begin of public info for implementing UniquidNode
	 *
	 */

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
	public void initNode() throws NodeException {
		SecureRandom random = new SecureRandom();
		// byte[] bytes = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS /
		// 8];
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		long creationTime = System.currentTimeMillis() / 1000;

		initNode(bytes, creationTime);
	}

	@Override
	public void initNodeFromHexEntropy(String hexEntropy, long creationTime) throws NodeException {
		byte[] bytes = HEX.decode(hexEntropy);

		initNode(bytes, creationTime);
	}

	@Override
	public void updateNode() throws NodeException {

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

	}

	@Override
	public synchronized String getNodeState() {
		return nodeState.toString();
	}

	@Override
	public synchronized void addUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener) {
		eventListeners.add(uniquidNodeEventListener);
	}

	@Override
	public synchronized void removeUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener) {
		eventListeners.remove(uniquidNodeEventListener);
	}

	/*
	 * End of public info for implementing UniquidNode
	 *
	 */

	/*
	 * Begin of some other util public method
	 */

	public Wallet getProviderWallet() {
		return providerWallet;
	}

	public Wallet getUserWallet() {
		return userWallet;
	}

	public String signTransaction(String s_tx, String path) throws BlockStoreException, InterruptedException,
			ExecutionException, InsufficientMoneyException, Exception {

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
	
	/**
	 * Builder for UniquidNodeImpl
	 *
	 */
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

		public UniquidNodeImpl build()
				throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

			return new UniquidNodeImpl(this);

		}
	}

	/*
	 * End of some other util public method
	 */

	/*
	 * Change internal state
	 */
	private synchronized void setUniquidNodeState(UniquidNodeState nodeState) {
		this.nodeState = nodeState;
	}

	/*
	 * Initiliaze this node from a byte array
	 */
	private void initNode(byte[] bytes, long creationTime) throws NodeException {

		try {

			if (providerFile.exists() && !providerFile.isDirectory() && userFile.exists() && !userFile.isDirectory()) {

				// Wallets already present!
				providerWallet = Wallet.loadFromFile(providerFile);
				userWallet = Wallet.loadFromFile(userFile);

				bytes = providerWallet.getKeyChainSeed().getSeedBytes();
				creationTime = providerWallet.getKeyChainSeed().getCreationTimeSeconds();

			} else {

				// Create a new provider wallet
				// providerWallet = Wallet.fromSeed(networkParameters,
				// new DeterministicSeed(bytes, "", creationTime),
				// UniquidNodeImpl.BIP44_ACCOUNT_PROVIDER,
				// UniquidNodeImpl.BIP44_ACCOUNT_USER);

				providerWallet = Wallet.fromSeed(networkParameters, new DeterministicSeed(bytes, "", creationTime),
						UniquidNodeImpl.BIP44_ACCOUNT_PROVIDER);

				// Create a new user wallet
				userWallet = Wallet.fromSeed(networkParameters, new DeterministicSeed(bytes, "", creationTime),
						UniquidNodeImpl.BIP44_ACCOUNT_USER);

			}

			// Calculate public info
			calculatePublicInfo(bytes, creationTime);

			// Retrieve contracts
			List<ProviderChannel> providerChannels = registerFactory.getProviderRegister().getAllChannels();

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

			// start updating
			updateNode();

		} catch (Exception ex) {

			throw new NodeException("Exception while initializating node", ex);

		}

		// DONE INITIALIZATION
	}

	/*
	 * Calculate some public info
	 */
	private void calculatePublicInfo(byte[] bytes, long creationTime) {

		LOGGER.info("HEX entropy " + HEX.encode(bytes) + "; creation time " + creationTime);

		LOGGER.info("Mnemonics: " + Utils.join(new DeterministicSeed(bytes, "", creationTime).getMnemonicCode()));

		DeterministicKey deterministicKey = NodeUtils.createDeterministicKeyFromByteArray(bytes);

		// LOGGER.info("START_NODE tpriv: " +
		// deterministicKey.serializePrivB58(networkParameters));
		// LOGGER.info("START_NODE tpub: " +
		// deterministicKey.serializePubB58(networkParameters));

		DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(deterministicKey);

		ImmutableList<ChildNumber> IMPRINTING_PATH = ImmutableList.of(new ChildNumber(44, true),
				new ChildNumber(0, true));

		DeterministicKey imprintingKey = deterministicHierarchy.get(IMPRINTING_PATH, true, true);
		LOGGER.info("Imprinting key tpub: " + imprintingKey.serializePubB58(networkParameters));

		publicKey = imprintingKey.serializePubB58(networkParameters);

		ImmutableList<ChildNumber> PROVIDER_IMPRINTING_ADDRESS = ImmutableList.of(new ChildNumber(44, true),
				new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(0, false),
				new ChildNumber(0, false), new ChildNumber(0, false));

		DeterministicKey imprintingProviderKey = deterministicHierarchy.get(PROVIDER_IMPRINTING_ADDRESS, true, true);
		imprintingAddress = imprintingProviderKey.toAddress(networkParameters);

	}

	/*
	 * callback to receive events from bitcoinj when coins are received
	 */
	public synchronized void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		nodeState.onCoinsReceived(wallet, tx);
	}

	/*
	 * callback to receive events from bitcoinj when coins are sent
	 */
	public synchronized void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		nodeState.onCoinsSent(wallet, tx);
	}

	/*
	 * Implementation of callback for blockchain events
	 */
	private class UniquidNodeDownloadProgressTracker extends DownloadProgressTracker {

		@Override
		protected void startDownload(final int blocks) {

			for (UniquidNodeEventListener listener : eventListeners) {
				
				listener.onSyncStarted(blocks);
			
			}

		}

		@Override
		protected void progress(final double pct, final int blocksSoFar, final Date date) {

			for (UniquidNodeEventListener listener : eventListeners) {
				
				listener.onSyncProgress(pct, blocksSoFar, date);
			
			}

		}

		@Override
		public void doneDownload() {

			for (UniquidNodeEventListener listener : eventListeners) {
				
				listener.onSyncEnded();

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
		 * @param tx
		 * @throws Exception
		 */
		public void manageContractCreation(Transaction tx) throws Exception;

		/**
		 * Defines the revocation of a contract
		 * 
		 * @param tx
		 * @throws Exception
		 */
		public void manageContractRevocation(Transaction tx) throws Exception;

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

				final Listener listener = new Listener() {

					@Override
					public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {

						try {

							if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)
									&& reason.equals(ChangeReason.TYPE)) {

								doRealContract(tx);

								tx.getConfidence().removeEventListener(this);

								LOGGER.info("Imprinting Done!");

							} else if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.DEAD)
									&& reason.equals(ChangeReason.TYPE)) {

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
		public void manageContractRevocation(Transaction tx) throws Exception {

			revokeRealContract(tx);

		}

		/**
		 * Delegate to subclass the real contract creation
		 * 
		 * @param tx
		 * @throws Exception
		 */
		public abstract void doRealContract(Transaction tx) throws Exception;

		/**
		 * Delegate to subclass the real contract revocation
		 * 
		 * @param tx
		 * @throws Exception
		 */
		public abstract void revokeRealContract(Transaction tx) throws Exception;

	}

	/**
	 * Class that manage imprinting contracts
	 * 
	 * @author giuseppe
	 *
	 */
	private class ImprintingContract extends AbstractContract {

		private static final String CONTRACT_FUNCTION = "000000400000000000000000000000000000";

		@Override
		public void doRealContract(Transaction tx) throws Exception {

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
					ProviderChannel providerChannel = new ProviderChannel();
					providerChannel.setUserAddress(sender);
					providerChannel.setProviderAddress(imprintingAddress.toBase58());
					providerChannel.setBitmask(CONTRACT_FUNCTION);
					providerChannel.setRevokeAddress("IMPRINTING");
					providerChannel.setRevokeTxId(tx.getHashAsString());

					// persist channel
					providerRegister.insertChannel(providerChannel);

					// We can move now to ReadyState
					setUniquidNodeState(new ReadyState());

					// Send event to listeners
					for (UniquidNodeEventListener listener : eventListeners) {

						listener.onProviderContractCreated(providerChannel);

					}

					LOGGER.info("Machine IMPRINTED!");

					break;

				}

			}

		}

		@Override
		public void revokeRealContract(Transaction tx) throws Exception {
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
		public void doRealContract(Transaction tx) throws Exception {

			// List<Address> addresses = wallet.getIssuedReceiveAddresses();

			// List<DeterministicKey> keys =
			// wallet.getActiveKeyChain().getLeafKeys();
			// List<Address> addresses2 = new ArrayList<>();
			// for (ECKey key : keys) {
			// addresses2.add(key.toAddress(networkParameters));
			// }

			List<TransactionOutput> to = tx.getOutputs();

			if (to.size() != 4) {
				LOGGER.error("Contract not valid! output size is not 4");
				return;
			}

			Script script = tx.getInput(0).getScriptSig();
			Address p_address = new Address(networkParameters,
					org.bitcoinj.core.Utils.sha256hash160(script.getPubKey()));

			// if (/*!addresses.contains(p_address) ||*/
			// !addresses2.contains(p_address)) {
			// LOGGER.error("Contract not valid! We are not the provider");
			// return;
			// }

			if (!providerWallet.isPubKeyHashMine(p_address.getHash160())) {
				LOGGER.error("Contract not valid! We are not the provider");
				return;
			}

			List<TransactionOutput> ts = new ArrayList<>(to);

			Address u_address = ts.get(0).getAddressFromP2PKHScript(networkParameters);

			// We are provider!!!
			if (u_address == null) {
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
			ProviderChannel providerChannel = new ProviderChannel();
			providerChannel.setProviderAddress(p_address.toBase58());
			providerChannel.setUserAddress(u_address.toBase58());
			providerChannel.setRevokeAddress(revoke.toBase58());
			providerChannel.setRevokeTxId(tx.getHashAsString());

			String opreturn = WalletUtils.getOpReturn(tx);

			byte[] op_to_byte = Hex.decode(opreturn);

			byte[] bitmask = Arrays.copyOfRange(op_to_byte, 1, 19);

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

				LOGGER.error("Exception while inserting providerregister", e);

				throw e;

			}

			// Inform listeners
			for (UniquidNodeEventListener listener : eventListeners) {

				listener.onProviderContractCreated(providerChannel);

			}

		}

		@Override
		public void revokeRealContract(Transaction tx) throws Exception {

			// Retrieve sender
			String sender = tx.getInput(0).getFromAddress().toBase58();

			ProviderRegister providerRegister;
			try {
				providerRegister = registerFactory.getProviderRegister();
				ProviderChannel channel = providerRegister.getChannelByRevokeAddress(sender);

				if (channel != null) {
					LOGGER.info("Found a contract to revoke!");
					// contract revoked
					providerRegister.deleteChannel(channel);

					LOGGER.info("Contract revoked! " + channel);

					for (UniquidNodeEventListener listener : eventListeners) {

						// send event to listeners
						listener.onProviderContractRevoked(channel);

					}
				} else {

					LOGGER.warn("No contract found to revoke");
				}

			} catch (Exception e) {

				LOGGER.error("Exception", e);

			}
		}

	}

	private class UserContract extends AbstractContract {

		@Override
		public void doRealContract(Transaction tx) throws Exception {

			// List<Address> addresses = wallet.getIssuedReceiveAddresses();

			// List<DeterministicKey> keys =
			// wallet.getActiveKeyChain().getLeafKeys();
			// List<Address> addresses2 = new ArrayList<>();
			// for (ECKey key : keys) {
			// addresses2.add(key.toAddress(networkParameters));
			// }

			List<TransactionOutput> to = tx.getOutputs();

			if (to.size() != 4) {
				LOGGER.error("Contract not valid! size is not 4");
				return;
			}

			Script script = tx.getInput(0).getScriptSig();
			Address p_address = new Address(networkParameters,
					org.bitcoinj.core.Utils.sha256hash160(script.getPubKey()));

			List<TransactionOutput> ts = new ArrayList<>(to);

			Address u_address = ts.get(0).getAddressFromP2PKHScript(networkParameters);

			// if (u_address == null || !addresses2.contains(u_address)) {
			// // is u_address one of our user addresses?
			// LOGGER.error("Contract not valid! User address is null or we are
			// not
			// the user");
			// return;
			// }

			// wallet.isPubKeyHashMine(ts.get(0).getScriptPubKey().getPubKeyHash());
			if (u_address == null || !userWallet.isPubKeyHashMine(u_address.getHash160())) {
				// is u_address one of our user addresses?
				LOGGER.error("Contract not valid! User address is null or we are not the user");
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

			String providerName = WalletUtils.retrieveNameFromProvider(p_address.toBase58());
			if (providerName == null) {
				LOGGER.error("Contract not valid! Provider name is null");
				return;
			}

			UserChannel userChannel = new UserChannel();
			userChannel.setProviderAddress(p_address.toBase58());
			userChannel.setUserAddress(u_address.toBase58());
			userChannel.setProviderName(providerName);
			userChannel.setRevokeAddress(revoke.toBase58());
			userChannel.setRevokeTxId(tx.getHashAsString());

			String opreturn = WalletUtils.getOpReturn(tx);

			byte[] op_to_byte = Hex.decode(opreturn);

			byte[] bitmask = Arrays.copyOfRange(op_to_byte, 1, 19);

			// encode to be saved on db
			String bitmaskToString = new String(Hex.encode(bitmask));

			userChannel.setBitmask(bitmaskToString);

			try {

				UserRegister userRegister = registerFactory.getUserRegister();

				userRegister.insertChannel(userChannel);

			} catch (Exception e) {

				LOGGER.error("Exception while inserting userChannel", e);

				throw e;

			}

			// Inform listeners
			for (UniquidNodeEventListener listener : eventListeners) {

				listener.onUserContractCreated(userChannel);

			}

		}

		@Override
		public void revokeRealContract(Transaction tx) throws Exception {
			// DO NOTHIG
		}

	}

	/**
	 * 
	 * @author giuseppe
	 *
	 */
	private interface UniquidNodeState {

		public void onCoinsSent(Wallet wallet, Transaction tx);

		public void onCoinsReceived(Wallet wallet, Transaction tx);

	}

	/**
	 * Implementation of State Design pattern
	 * 
	 * @author giuseppe
	 *
	 */
	private class ImprintingState implements UniquidNodeState {

		private boolean alreadyImprinted;

		public ImprintingState() {

			this.alreadyImprinted = false;

		}

		@Override
		public void onCoinsSent(Wallet wallet, Transaction tx) {

			LOGGER.info("We sent coins from a wallet that we don't expect!");

		}

		@Override
		public void onCoinsReceived(Wallet wallet, Transaction tx) {

			if (wallet.equals(providerWallet)) {

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

						LOGGER.info("Invalid imprinting contract");

					}

				} catch (Exception ex) {

					LOGGER.error("Exception while imprinting", ex);

				}

			} else if (wallet.equals(userWallet)) {

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

	}

	/**
	 * Class to represents the ready state
	 * 
	 * @author giuseppe
	 *
	 */
	private class ReadyState implements UniquidNodeState {

		@Override
		public void onCoinsSent(Wallet wallet, Transaction tx) {

			// We sent some coins. Probably we created a contract as Provider
			if (wallet.equals(providerWallet)) {

				LOGGER.info("Sent coins from provider wallet");

				try {

					LOGGER.info("Creating provider contract!");
					ContractStrategy contractStrategy = new ProviderContract();
					contractStrategy.manageContractCreation(tx);

				} catch (Exception ex) {

					LOGGER.error("Exception while creating provider contract", ex);

				}

			} else if (wallet.equals(userWallet)) {

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
		public void onCoinsReceived(Wallet wallet, Transaction tx) {

			// Received a contract!!!
			if (wallet.equals(providerWallet)) {

				LOGGER.info("Received coins on provider wallet");

				// If is imprinting transaction...
				// if (UniquidNodeStateUtils.isValidImprintingTransaction(tx,
				// nodeStateContext)) {
				//
				// // imprint!
				// LOGGER.warn("Attention! Another machine tried to imprint US!
				// Skip request!");
				//
				// } /*
				// * else if
				// * (com.uniquid.node.utils.Utils.isValidRevokeContract(tx,
				// * networkParameters, nodeStateContext)) {
				// *
				// * LOGGER.info("Revoking contract!");
				// * com.uniquid.node.utils.Utils.revokeContract(wallet, tx,
				// * networkParameters, nodeStateContext);
				// *
				// * }
				// */ else {
				//
				// LOGGER.info("Unknown contract");
				//
				// }

			} else if (wallet.equals(userWallet)) {

				LOGGER.info("Received coins on user wallet");

				/*
				 * if (com.uniquid.node.utils.Utils.isValidRevokeContract(tx,
				 * networkParameters, nodeStateContext)) {
				 * 
				 * LOGGER.info("Revoking contract!");
				 * com.uniquid.node.utils.Utils.revokeContract(wallet, tx,
				 * networkParameters, nodeStateContext);
				 * 
				 * } else {
				 */

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

	}

}