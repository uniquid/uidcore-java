package com.uniquid.node.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
import org.bitcoinj.core.TransactionConfidence.Listener;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.ListenerRegistration;
import org.bitcoinj.utils.Threading;
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

	protected NetworkParameters networkParameters;
	private File providerFile;
	private Wallet providerWallet;
	private File providerChainFile;
	private File userFile;
	private Wallet userWallet;
	private File userChainFile;

	private Address imprintingAddress;
	private String publicKey;
	private String machineName;

	private byte[] seed;
	private long creationTime;

	protected RegisterFactory registerFactory;

	private CopyOnWriteArrayList<ListenerRegistration<UniquidNodeEventListener>> eventListeners;
	
	private DeterministicSeed detSeed;

	protected UniquidNodeImpl(Builder builder)
			throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		this.networkParameters = builder.getParams();
		this.providerFile = builder.getProviderFile();
		this.providerChainFile = builder.getChainFile();
		this.userFile = builder.getUserFile();
		this.userChainFile = builder.getUserChainFile();
		this.registerFactory = builder.getRegisterFactory();
		this.machineName = builder.getMachineName();
		this.eventListeners = new CopyOnWriteArrayList<ListenerRegistration<UniquidNodeEventListener>>();
		this.seed = builder.getSeed();
		this.creationTime = builder.getCreationTime();
		
		setUniquidNodeState(getCreatedState());

	}

	protected UniquidNodeState getCreatedState() {
		return new CreatedState();
	}

	protected UniquidNodeState getReadyState() {
		return new ReadyState();
	}

	protected UniquidNodeState getImprintingState() {
		return new ImprintingState();
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
	public synchronized String getPublicKey() {
		return nodeState.getPublicKey();
	}

	@Override
	public synchronized String getNodeName() {
		return machineName;
	}

	@Override
	public synchronized long getCreationTime() {
		return creationTime;
	}
	
	@Override
	public synchronized String getHexSeed() {
		return nodeState.getHexSeed();
	}

	@Override
	public synchronized String getSpendableBalance() {
		return nodeState.getSpendableBalance();
	}

	@Override
	public void initNode() throws NodeException {

		try {

			if (providerFile.exists() && !providerFile.isDirectory() && userFile.exists() && !userFile.isDirectory()) {

				// Wallets already present!
				providerWallet = Wallet.loadFromFile(providerFile);
				userWallet = Wallet.loadFromFile(userFile);

				detSeed = providerWallet.getKeyChainSeed();

			} else {

				// Create a new provider wallet
				// providerWallet = Wallet.fromSeed(networkParameters,
				// new DeterministicSeed(bytes, "", creationTime),
				// UniquidNodeImpl.BIP44_ACCOUNT_PROVIDER,
				// UniquidNodeImpl.BIP44_ACCOUNT_USER);

				detSeed = NodeUtils.createDeterministicSeed(seed, creationTime);

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
	public synchronized com.uniquid.node.UniquidNodeState getNodeState() {
		return nodeState.getNodeState();
	}

	@Override
	public synchronized void addUniquidNodeEventListener(final UniquidNodeEventListener uniquidNodeEventListener) {
		addUniquidNodeEventListener(Threading.SAME_THREAD, uniquidNodeEventListener);
	}

	private void addUniquidNodeEventListener(Executor executor, UniquidNodeEventListener listener) {
        // This is thread safe, so we don't need to take the lock.
		eventListeners.add(new ListenerRegistration<UniquidNodeEventListener>(listener, executor));
    }

	@Override
	public synchronized void removeUniquidNodeEventListener(final UniquidNodeEventListener uniquidNodeEventListener) {
		// do nothing for now
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
	
			String retValue = "";
			if (path.startsWith("0")) {
				
				// fix our tx
				WalletUtils.newCompleteTransaction(send, providerWallet, networkParameters);
	
				// delegate to walled the signing
				providerWallet.signTransaction(send);
	
				String sr = Hex.toHexString(originalTransaction.bitcoinSerialize());
	
				LOGGER.info("Serialized SIGNED transaction: " + sr);
	
				retValue = NodeUtils.sendTransaction(networkParameters, send);
	
			} else if (path.startsWith("1")) {
				
				// fix our tx
				WalletUtils.newCompleteTransaction(send, userWallet, networkParameters);
	
				// delegate to walled the signing
				userWallet.signTransaction(send);
	
				String sr = Hex.toHexString(originalTransaction.bitcoinSerialize());
	
				LOGGER.info("Serialized SIGNED transaction: " + sr);
	
				retValue = NodeUtils.sendTransaction(networkParameters, send);
	
			}
	
			return retValue;
		
		} catch (Exception ex) {
			
			throw new NodeException("Exception while signing", ex);
		}
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
		protected byte[] _seed;
		protected long _creationTime;

		private RegisterFactory _registerFactory;

		private String _machineName;

		public Builder set_params(NetworkParameters _params) {
			this._params = _params;
			return this;
		}
		
		public NetworkParameters getParams() {
			return _params;
		}

		public Builder set_providerFile(File _providerFile) {
			this._providerFile = _providerFile;
			return this;
		}
		
		public File getProviderFile() {
			return _providerFile;
		}

		public Builder set_userFile(File _userFile) {
			this._userFile = _userFile;
			return this;
		}
		
		public File getUserFile() {
			return _userFile;
		}

		public Builder set_chainFile(File _chainFile) {
			this._chainFile = _chainFile;
			return this;
		}
		
		public File getChainFile() {
			return _chainFile;
		}

		public Builder set_userChainFile(File _userChainFile) {
			this._userChainFile = _userChainFile;
			return this;
		}
		
		public File getUserChainFile() {
			return _userChainFile;
		}

		public Builder set_registerFactory(RegisterFactory _registerFactory) {
			this._registerFactory = _registerFactory;
			return this;
		}
		
		public RegisterFactory getRegisterFactory() {
			return _registerFactory;
		}

		public Builder set_machine_name(String _machineName) {
			this._machineName = _machineName;
			return this;
		}
		
		public String getMachineName() {
			return _machineName;
		}

		public byte[] getSeed() {
			return _seed;
		}

		public long getCreationTime() {
			return _creationTime;
		}

		public UniquidNodeImpl build()
				throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

			SecureRandom random = new SecureRandom();
			_seed = new byte[32];
			random.nextBytes(_seed);
			_creationTime = System.currentTimeMillis() / 1000;

			return new UniquidNodeImpl(this);

		}

		public UniquidNodeImpl buildFromHexSeed(final String hexSeed, final long creationTime)
				throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

			_seed = org.bitcoinj.core.Utils.HEX.decode(hexSeed);

			_creationTime = creationTime;

			return new UniquidNodeImpl(this);

		}
	}

	/*
	 * Begin of some other useful public method
	 */

	/**
	 * Change internal state
	 */
	private synchronized void setUniquidNodeState(final UniquidNodeState nodeState) {
		this.nodeState = nodeState;
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

		ImmutableList<ChildNumber> PROVIDER_IMPRINTING_ADDRESS = ImmutableList.of(new ChildNumber(44, true),
				new ChildNumber(0, true), new ChildNumber(0, false), new ChildNumber(0, false),
				new ChildNumber(0, false), new ChildNumber(0, false));

		DeterministicKey imprintingProviderKey = deterministicHierarchy.get(PROVIDER_IMPRINTING_ADDRESS, true, true);
		imprintingAddress = imprintingProviderKey.toAddress(networkParameters);

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
		 * @param tx
		 * @throws Exception
		 */
		public void manageContractCreation(final Transaction tx) throws Exception;

		/**
		 * Defines the revocation of a contract
		 * 
		 * @param tx
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

				final Listener listener = new Listener() {

					@Override
					public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {

						try {

							if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)
									&& reason.equals(ChangeReason.TYPE)) {

								doRealContract(tx);

								tx.getConfidence().removeEventListener(this);

								LOGGER.info("Contract Done!");

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
					setUniquidNodeState(getReadyState());

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
	protected interface UniquidNodeState {

		public void onCoinsSent(final Wallet wallet, final Transaction tx);

		public void onCoinsReceived(final Wallet wallet, final Transaction tx);

		public com.uniquid.node.UniquidNodeState getNodeState();
		
		public String getImprintingAddress();
		
		public String getPublicKey();
		
		public String getHexSeed();
		
		public String getSpendableBalance();
		
		public Wallet getProviderWallet();
		
		public Wallet getUserWallet();

	}
	
	/**
	 * Fake state to be used when new instance is created
	 */
	protected class CreatedState implements UniquidNodeState {

		public CreatedState() {
		}

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
		public String getHexSeed() {
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
	protected class ImprintingState implements UniquidNodeState {

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
		public String getHexSeed() {
			return detSeed.toHexString();
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
	protected class ReadyState implements UniquidNodeState {

		public ReadyState() {
		}

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
		public String getHexSeed() {
			return detSeed.toHexString();
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