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
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;

/**
 * Class that manage provider contracts
 * 
 * @author giuseppe
 *
 */
public class ProviderContract extends AbstractContract {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProviderContract.class);

	public ProviderContract(NetworkParameters networkParameters, Wallet userWallet, Wallet providerWallet, RegisterFactory registerFactory,
			UniquidNodeEventService uniquidNodeEventService, String pubKey) {
		
		super(networkParameters, userWallet, providerWallet, registerFactory, uniquidNodeEventService, pubKey);
	}

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
		uniquidNodeEventService.onProviderContractCreated(providerChannel);

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
				
				// Inform listeners
				uniquidNodeEventService.onProviderContractRevoked(channel);

			} else {

				LOGGER.warn("No contract found to revoke!");
			}

		} catch (Exception e) {

			LOGGER.error("Exception", e);

		}
	}

}
