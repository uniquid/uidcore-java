package com.uniquid.node.impl.contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.node.impl.UniquidNodeStateContext;
import com.uniquid.node.impl.utils.WalletUtils;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;
import com.uniquid.registry.RegistryDAO;
import com.uniquid.registry.exception.RegistryException;
import com.uniquid.registry.impl.RegistryDAOImpl;

@SuppressWarnings("rawtypes")
public class UserContract extends AbstractContract {
	
	@SuppressWarnings("unchecked")
	public UserContract(UniquidNodeStateContext uniquidNodeStateContext) {
		super(uniquidNodeStateContext);
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserContract.class);

	@Override
	public void doRealContract(final Transaction tx) throws Exception {
		
		LOGGER.info("Making user contract from TX {}", tx.getHashAsString());

		List<TransactionOutput> transactionOutputs = tx.getOutputs();

		if (transactionOutputs.size() != 4) {
			LOGGER.error("Contract not valid! size is not 4");
			return;
		}

		Script script = tx.getInput(0).getScriptSig();
		Address providerAddress = new Address(uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters(),
				org.bitcoinj.core.Utils.sha256hash160(script.getPubKey()));

		List<TransactionOutput> ts = new ArrayList<>(transactionOutputs);

		Address userAddress = ts.get(0).getAddressFromP2PKHScript(uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters());

		if (userAddress == null || !uniquidNodeStateContext.getUserWallet().isPubKeyHashMine(userAddress.getHash160())) {
			LOGGER.error("Contract not valid! User address is null or we are not the user");
			return;
		}

		if (!WalletUtils.isValidOpReturn(tx)) {
			LOGGER.error("Contract not valid! OPRETURN not valid");
			return;
		}

		Address revoke = ts.get(2).getAddressFromP2PKHScript(uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters());
		if (revoke == null /*|| !WalletUtils.isUnspent(tx.getHashAsString(), revoke.toBase58())*/) {
			LOGGER.error("Contract not valid! Revoke address is null or contract revoked");
			return;
		}

		String providerName = retrieveNameFromProvider(providerAddress, uniquidNodeStateContext);
		if (providerName == null) {
			LOGGER.error("Contract not valid! Provider name is null");
			return;
		}
		
		ECKey key = uniquidNodeStateContext.getUserWallet().findKeyFromPubHash(userAddress.getHash160());
		String path = null;
		if (key != null) {
			path = ((DeterministicKey) key).getPathAsString();
		}

		LOGGER.info("Contract is valid. Inserting in register");

		// Create channel
		final UserChannel userChannel = new UserChannel();
		userChannel.setProviderAddress(providerAddress.toBase58());
		userChannel.setUserAddress(userAddress.toBase58());
		userChannel.setProviderName(providerName);
		userChannel.setRevokeAddress(revoke.toBase58());
		userChannel.setRevokeTxId(tx.getHashAsString());
		userChannel.setSince(0);
		userChannel.setUntil(Long.MAX_VALUE);
		userChannel.setPath(path);

		String opreturn = WalletUtils.getOpReturn(tx);

		byte[] op_to_byte = Hex.decode(opreturn);

		byte[] bitmask = Arrays.copyOfRange(op_to_byte, 0, 19);

		// encode to be saved on db
		String bitmaskToString = new String(Hex.encode(bitmask));

		userChannel.setBitmask(bitmaskToString);

		UserRegister userRegister = uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getUserRegister();
		
		uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().startTransaction();
		
		try {

			userRegister.insertChannel(userChannel);
			
			LOGGER.trace("Inserted user register: " + userRegister);
			
			uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().commitTransaction();
		
		} catch (RegisterException ex) {
			
			LOGGER.error("Error while inserting channel", ex);
			
			uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().rollbackTransaction();
			
			// ReThrow
			throw ex;
			
		}

		// Inform listeners
		uniquidNodeStateContext.getUniquidNodeEventService().onUserContractCreated(userChannel);

	}

	@Override
	public void revokeRealContract(final Transaction tx) throws Exception {

		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();

		UserRegister userRegister;
		try {

			userRegister = uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getUserRegister();
			final UserChannel channel = userRegister.getUserChannelByRevokeAddress(sender);

			if (channel != null) {

				LOGGER.info("Found an user contract to revoke!");
				// contract revoked
				userRegister.deleteChannel(channel);

				LOGGER.info("Contract revoked! " + channel);

				// Inform listeners
				uniquidNodeStateContext.getUniquidNodeEventService().onUserContractRevoked(channel);

			} else {

				LOGGER.warn("No contract found to revoke!");
			}

		} catch (Exception e) {

			LOGGER.error("Exception", e);

		}

	}
	
	protected String retrieveNameFromProvider(Address providerAddress, UniquidNodeStateContext uniquidNodeStateContext) throws RegistryException {
		
		RegistryDAO registryDAO = new RegistryDAOImpl(uniquidNodeStateContext.getUniquidNodeConfiguration().getRegistryUrl());
		
		return registryDAO.retrieveProviderName(providerAddress.toBase58());
		
	}

}
