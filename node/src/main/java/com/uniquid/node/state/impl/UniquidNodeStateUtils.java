package com.uniquid.node.state.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.Listener;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.node.state.UniquidNodeStateContext;
import com.uniquid.node.utils.WalletUtils;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

public class UniquidNodeStateUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeStateUtils.class.getName());
	
	public static void makeUserContract(final Wallet wallet, final Transaction tx,
			final UniquidNodeStateContext nodeStateContext) {

		LOGGER.info("Creating contract...");

		// Transaction already confirmed
		if (tx.getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)) {

			LOGGER.info("tx.getConfidence is BUILDING...");

			doUserContract(wallet, tx, nodeStateContext);

			LOGGER.info("Done creating contract");

		} else {

			LOGGER.info("tx.getConfidence is not BUILDING: " + tx.getConfidence() + ", registering a listener");

			final Listener listener = new Listener() {

				@Override
				public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {

					LOGGER.info("tx.getConfidence is changed: " + confidence);

					try {

						if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)
								&& reason.equals(ChangeReason.TYPE)) {

							LOGGER.info("tx.getConfidence is BUILDING...");

							doUserContract(wallet, tx, nodeStateContext);

							tx.getConfidence().removeEventListener(this);

							LOGGER.info("Contract Done!");

						} else if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.DEAD)
								&& reason.equals(ChangeReason.TYPE)) {

							LOGGER.error("Something bad happened! TRansaction is DEAD!");

							tx.getConfidence().removeEventListener(this);

						} else {

							LOGGER.warn("Unexpected tx.getConfidence..");
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

	/**
	 * Create contract
	 * 
	 * @param tx
	 * @param networkParameters
	 * @param nodeStateContext
	 */
	private static void doUserContract(Wallet wallet, Transaction tx, UniquidNodeStateContext nodeStateContext) {

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
		Address p_address = new Address(nodeStateContext.getNetworkParameters(),
				org.bitcoinj.core.Utils.sha256hash160(script.getPubKey()));

		List<TransactionOutput> ts = new ArrayList<>(to);

		Address u_address = ts.get(0).getAddressFromP2PKHScript(nodeStateContext.getNetworkParameters());

		// if (u_address == null || !addresses2.contains(u_address)) {
		// // is u_address one of our user addresses?
		// LOGGER.error("Contract not valid! User address is null or we are not
		// the user");
		// return;
		// }

		// wallet.isPubKeyHashMine(ts.get(0).getScriptPubKey().getPubKeyHash());
		if (u_address == null || !wallet.isPubKeyHashMine(u_address.getHash160())) {
			// is u_address one of our user addresses?
			LOGGER.error("Contract not valid! User address is null or we are not the user");
			return;
		}

		if (!WalletUtils.isValidOpReturn(tx)) {
			LOGGER.error("Contract not valid! OPRETURN not valid");
			return;
		}

		Address revoke = ts.get(2).getAddressFromP2PKHScript(nodeStateContext.getNetworkParameters());
		if (revoke == null /*
							 * || !WalletUtils.isUnspent(tx.getHashAsString(),
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

			UserRegister userRegister = nodeStateContext.getRegisterFactory().getUserRegister();

			userRegister.insertChannel(userChannel);

		} catch (Exception e) {

			LOGGER.error("Exception while inserting userChannel", e);

		}

		// Inform listeners
		List<UniquidNodeEventListener> listeners = nodeStateContext.getUniquidNodeEventListeners();

		for (UniquidNodeEventListener listener : listeners) {

			listener.onUserContractCreated(userChannel);

		}

		// We need to watch the revoked address
		// nodeStateContext.getRevokeWallet().addWatchedAddress(revoke);

	}

	public static void makeProviderContract(final Wallet wallet, final Transaction tx,
			final UniquidNodeStateContext nodeStateContext) {

		LOGGER.info("Creating contract...");

		// Transaction already confirmed
		if (tx.getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)) {

			LOGGER.info("tx.getConfidence is BUILDING...");

			doProviderContract(wallet, tx, nodeStateContext);

			LOGGER.info("Done creating contract");

		} else {

			LOGGER.info("tx.getConfidence is not BUILDING: " + tx.getConfidence() + ", registering a listener");

			final Listener listener = new Listener() {

				@Override
				public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {

					LOGGER.info("tx.getConfidence is changed: " + confidence);

					try {

						if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)
								&& reason.equals(ChangeReason.TYPE)) {

							LOGGER.info("tx.getConfidence is BUILDING...");

							doProviderContract(wallet, tx, nodeStateContext);

							tx.getConfidence().removeEventListener(this);

							LOGGER.info("Contract Done!");

						} else if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.DEAD)
								&& reason.equals(ChangeReason.TYPE)) {

							LOGGER.error("Something bad happened! TRansaction is DEAD!");

							tx.getConfidence().removeEventListener(this);

						} else {

							LOGGER.warn("Unexpected tx.getConfidence..");
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

	private static void doProviderContract(Wallet wallet, Transaction tx, UniquidNodeStateContext nodeStateContext) {

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
		Address p_address = new Address(nodeStateContext.getNetworkParameters(),
				org.bitcoinj.core.Utils.sha256hash160(script.getPubKey()));

		// if (/*!addresses.contains(p_address) ||*/
		// !addresses2.contains(p_address)) {
		// LOGGER.error("Contract not valid! We are not the provider");
		// return;
		// }

		if (!wallet.isPubKeyHashMine(p_address.getHash160())) {
			LOGGER.error("Contract not valid! We are not the provider");
			return;
		}

		List<TransactionOutput> ts = new ArrayList<>(to);

		Address u_address = ts.get(0).getAddressFromP2PKHScript(nodeStateContext.getNetworkParameters());

		// We are provider!!!
		if (u_address == null) {
			LOGGER.error("Contract not valid! User address is null");
			return;
		}

		if (!WalletUtils.isValidOpReturn(tx)) {
			LOGGER.error("Contract not valid! OPRETURN not valid");
			return;
		}

		Address revoke = ts.get(2).getAddressFromP2PKHScript(nodeStateContext.getNetworkParameters());
		if (revoke == null /*
							 * || !WalletUtils.isUnspent(tx.getHashAsString(),
							 * revoke.toBase58())
							 */) {
			LOGGER.error("Contract not valid! Revoke address is null");
			return;
		}

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

		providerChannel.setBitmask(bitmaskToString);

		try {

			ProviderRegister providerRegister = nodeStateContext.getRegisterFactory().getProviderRegister();

			List<ProviderChannel> channels = providerRegister.getAllChannels();

			// If this is the first "normal" contract then remove the imprinting
			// contract
			if (channels.size() == 1 && channels.get(0).getRevokeAddress().equals("IMPRINTING")) {

				providerRegister.deleteChannel(channels.get(0));

			}

			providerRegister.insertChannel(providerChannel);

		} catch (Exception e) {

			LOGGER.error("Exception while inserting providerregister", e);

		}

		// Inform listeners
		for (UniquidNodeEventListener listener : nodeStateContext.getUniquidNodeEventListeners()) {

			listener.onProviderContractCreated(providerChannel);

		}

		// We need to watch the revoked address
		// nodeStateContext.getRevokeWallet().addWatchedAddress(revoke);

	}

	public static void revokeContract(Wallet wallet, Transaction tx, UniquidNodeStateContext nodeStateContext) {

		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();

		ProviderRegister providerRegister;
		try {
			providerRegister = nodeStateContext.getRegisterFactory().getProviderRegister();
			ProviderChannel channel = providerRegister.getChannelByRevokeAddress(sender);

			if (channel != null) {
				LOGGER.info("Found a contract to revoke!");
				// contract revoked
				providerRegister.deleteChannel(channel);

				LOGGER.info("Contract revoked! " + channel);

				for (UniquidNodeEventListener listener : nodeStateContext.getUniquidNodeEventListeners()) {

					// send event to listeners
					listener.onProviderContractRevoked(channel);

				}
			}

		} catch (Exception e) {

			LOGGER.error("Exception", e);

		}

	}

	public static boolean isValidImprintingTransaction(Transaction tx, UniquidNodeStateContext nodeStateContext) {
		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();

		// Check output
		List<TransactionOutput> transactionOutputs = tx.getOutputs();
		for (TransactionOutput to : transactionOutputs) {
			Address address = to.getAddressFromP2PKHScript(nodeStateContext.getNetworkParameters());
			if (address != null && address.equals(Address.fromBase58(nodeStateContext.getNetworkParameters(),
					nodeStateContext.getImprintingAddress()))) {
				return true;
			}
		}

		return false;
	}

	public static boolean isValidRevokeContract(Transaction tx, UniquidNodeStateContext nodeStateContext) {

		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();

		ProviderRegister providerRegister;
		try {
			providerRegister = nodeStateContext.getRegisterFactory().getProviderRegister();
			ProviderChannel channel = providerRegister.getChannelByRevokeAddress(sender);

			if (channel != null) {
				return true;
			}

		} catch (Exception e) {

			LOGGER.error("Exception", e);

		}

		return false;

	}

}
