package com.uniquid.node.impl.contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.node.impl.UniquidNodeEventService;
import com.uniquid.node.impl.utils.WalletUtils;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

public class UserContract extends AbstractContract {
	
	public UserContract(NetworkParameters networkParameters, Wallet userWallet, Wallet providerWallet, RegisterFactory registerFactory,
			UniquidNodeEventService uniquidNodeEventService, String pubKey) {
		
		super(networkParameters, userWallet, providerWallet, registerFactory, uniquidNodeEventService, pubKey);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(UserContract.class);

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
		
		// Inform listeners
		uniquidNodeEventService.onUserContractCreated(userChannel);

	}

	@Override
	public void revokeRealContract(final Transaction tx) throws Exception {
		// DO NOTHIG
	}

}
