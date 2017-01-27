package com.uniquid.spv_node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

public class ReadyState implements NodeState {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReadyState.class.getName());
    
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	
    private NodeStateContext nodeStateContext;
	private NetworkParameters networkParameters;
	private Address imprintingAddress;
	
	public ReadyState(NodeStateContext nodeStateContext, Address imprintingAddress) {
		this.nodeStateContext = nodeStateContext;
		this.networkParameters = nodeStateContext.getNetworkParameters();
		this.imprintingAddress = imprintingAddress;
	}

	@Override
	public void onWalletChanged(Wallet wallet) {
		
		Set<Transaction> transactions = wallet.getTransactions(false);

		if (wallet.equals(nodeStateContext.getProviderWallet())) {
			
			for (Transaction t : transactions) {
			
				try {
					// Populate provider register
					LOGGER.info("GETCHANNELS t.size: " + transactions.size());
//					List<Address> addresses = masterWallet.getIssuedReceiveAddresses();
					
					List<DeterministicKey> keys = wallet.getActiveKeyChain().getLeafKeys();
					List<String> addresses = new ArrayList<>();
					for (ECKey key : keys) {
						addresses.add(key.toAddress(networkParameters).toBase58());
					}
					
					List<TransactionOutput> to = t.getOutputs();

					if (to.size() != 4)
						continue;

					Script script = t.getInput(0).getScriptSig();
					Address p_address = new Address(networkParameters, Utils.sha256hash160(script.getPubKey()));

					List<TransactionOutput> ts = new ArrayList<>(to);

					Address u_address = ts.get(0).getAddressFromP2PKHScript(networkParameters);

					// We are provider!!!
					if (u_address == null /*|| !addresses.contains(u_address.toBase58())*/) {
						continue;
					}

					if (!WalletUtils.isValidOpReturn(t)) {
						continue;
					}

					Address revoca = ts.get(2).getAddressFromP2PKHScript(networkParameters);
					if(revoca == null || !WalletUtils.isUnspent(t.getHashAsString(), revoca.toBase58())){
						continue;
					}

					ProviderChannel providerChannel = new ProviderChannel();
					providerChannel.setProviderAddress(p_address.toBase58());
					providerChannel.setUserAddress(u_address.toBase58());
					providerChannel.setRevokeAddress(revoca.toBase58());
					
					String opreturn = WalletUtils.getOpReturn(t);
					
					byte[] op_to_byte = Hex.decode(opreturn);
					
					byte[] bitmask = Arrays.copyOfRange(op_to_byte, 1, 19);
					
					// encode to be saved on db
					String bitmaskToString = new String(Hex.encode(bitmask));
					
					providerChannel.setBitmask(bitmaskToString);
					
					try {

						ProviderRegister providerRegister = nodeStateContext.getRegisterFactory().createProviderRegister();
						
						providerRegister.insertChannel(providerChannel);
						
					} catch (Exception e) {

						LOGGER.error("Exception while inserting providerregister", e);

					}

					LOGGER.info("GETCHANNELS txid: " + t.getHashAsString());
					LOGGER.info("GETCHANNELS provider: " + p_address.toBase58());
					LOGGER.info("GETCHANNELS user: " + u_address);
					LOGGER.info("GETCHANNELS revoca: " + ts.get(2).getAddressFromP2PKHScript(networkParameters));
					LOGGER.info("GETCHANNELS change_provider: " + ts.get(3).getAddressFromP2PKHScript(networkParameters));
					LOGGER.info("GETCHANNELS OPRETURN: " + Hex.toHexString(op_to_byte)  + "\n");
		
				} catch (Exception ex) {
		
					LOGGER.error("Exception while populating Register", ex);
		
				}
			
			}
		
		} else {
			
			// Populate user register
			LOGGER.info("GETCHANNELS t.size: " + transactions.size());
			List<Address> issuedAddresses = wallet.getIssuedReceiveAddresses();
			for (Transaction t : transactions) {
				
				List<TransactionOutput> to = t.getOutputs();

				if (to.size() != 4)
					continue;

				Script script = t.getInput(0).getScriptSig();
				Address p_address = new Address(networkParameters, Utils.sha256hash160(script.getPubKey()));

				List<TransactionOutput> ts = new ArrayList<>(to);

				Address u_address = ts.get(0).getAddressFromP2PKHScript(networkParameters);

				if (u_address == null /*|| !addresses.contains(u_address)*/) {
					continue;
				}

				if (!WalletUtils.isValidOpReturn(t)) {
					continue;
				}

				Address revoca = ts.get(2).getAddressFromP2PKHScript(networkParameters);
				if(revoca == null || !WalletUtils.isUnspent(t.getHashAsString(), revoca.toBase58())){
					continue;
				}
				
				String providerName = WalletUtils.retrieveNameFromProvider(p_address.toBase58());
				if (providerName == null) {
					continue;
				}

				UserChannel userChannel = new UserChannel();
				userChannel.setProviderAddress(p_address.toBase58());
				userChannel.setUserAddress(u_address.toBase58());
				userChannel.setProviderName(providerName);
				
				String opreturn = WalletUtils.getOpReturn(t);
				
				byte[] op_to_byte = Hex.decode(opreturn);
				
				byte[] bitmask = Arrays.copyOfRange(op_to_byte, 1, 19);
				
				// encode to be saved on db
				String bitmaskToString = new String(Hex.encode(bitmask));
				
				userChannel.setBitmask(bitmaskToString);
				
				try {

					UserRegister userRegister = nodeStateContext.getRegisterFactory().createUserRegister();
					
					userRegister.insertChannel(userChannel);
					
				} catch (Exception e) {

					LOGGER.error("Exception while inserting userChannel", e);

				}

				LOGGER.info("GETCHANNELS txid: " + t.getHashAsString());
				LOGGER.info("GETCHANNELS provider: " + p_address.toBase58());
				LOGGER.info("GETCHANNELS user: " + u_address);
				LOGGER.info("GETCHANNELS revoca: " + ts.get(2).getAddressFromP2PKHScript(networkParameters));
				LOGGER.info("GETCHANNELS change_provider: " + ts.get(3).getAddressFromP2PKHScript(networkParameters));
				LOGGER.info("GETCHANNELS OPRETURN: " + Hex.toHexString(op_to_byte)  + "\n");

			}

		}

	}

	@Override
	public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		// Created a contract!!!
		try {
			// Populate provider register
//			List<Address> addresses = masterWallet.getIssuedReceiveAddresses();
			
//			List<DeterministicKey> keys = wallet.getActiveKeyChain().getLeafKeys();
//			List<String> addresses = new ArrayList<>();
//			for (ECKey key : keys) {
//				addresses.add(key.toAddress(networkParameters).toBase58());
//			}
			
			List<TransactionOutput> to = tx.getOutputs();

			if (to.size() != 4)
				return;

			Script script = tx.getInput(0).getScriptSig();
			Address p_address = new Address(networkParameters, Utils.sha256hash160(script.getPubKey()));

			List<TransactionOutput> ts = new ArrayList<>(to);

			Address u_address = ts.get(0).getAddressFromP2PKHScript(networkParameters);

			// We are provider!!!
			if (u_address == null /*|| !addresses.contains(u_address.toBase58())*/) {
				return;
			}

			if (!WalletUtils.isValidOpReturn(tx)) {
				return;
			}

			Address revoke = ts.get(2).getAddressFromP2PKHScript(networkParameters);
			if(revoke == null || !WalletUtils.isUnspent(tx.getHashAsString(), revoke.toBase58())){
				return;
			}

			ProviderChannel providerChannel = new ProviderChannel();
			providerChannel.setProviderAddress(p_address.toBase58());
			providerChannel.setUserAddress(u_address.toBase58());
			providerChannel.setRevokeAddress(revoke.toBase58());
			
			String opreturn = WalletUtils.getOpReturn(tx);
			
			byte[] op_to_byte = Hex.decode(opreturn);
			
			byte[] bitmask = Arrays.copyOfRange(op_to_byte, 1, 19);
			
			// encode to be saved on db
			String bitmaskToString = new String(Hex.encode(bitmask));
			
			providerChannel.setBitmask(bitmaskToString);
			
			try {

				ProviderRegister providerRegister = nodeStateContext.getRegisterFactory().createProviderRegister();
				
				providerRegister.insertChannel(providerChannel);
				
			} catch (Exception e) {

				LOGGER.error("Exception while inserting providerregister", e);

			}
			
			// We need to watch the revoke address
			//wallet.addWatchedAddress(revoke);

			LOGGER.info("GETCHANNELS txid: " + tx.getHashAsString());
			LOGGER.info("GETCHANNELS provider: " + p_address.toBase58());
			LOGGER.info("GETCHANNELS user: " + u_address);
			LOGGER.info("GETCHANNELS revoca: " + ts.get(2).getAddressFromP2PKHScript(networkParameters));
			LOGGER.info("GETCHANNELS change_provider: " + ts.get(3).getAddressFromP2PKHScript(networkParameters));
			LOGGER.info("GETCHANNELS OPRETURN: " + Hex.toHexString(op_to_byte)  + "\n");

		} catch (Exception ex) {

			LOGGER.error("Exception while populating Register", ex);

		}
		
	}

	@Override
	public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		// Received a contract!!!
		if (wallet.equals(nodeStateContext.getProviderWallet())) {
			
			try {
				// skip unconfirmed transactions
				if (!tx.isPending()) {
					
					// Retrieve sender
					String sender = tx.getInput(0).getFromAddress().toBase58();
					
					// Check output
					List<TransactionOutput> transactionOutputs = tx.getOutputs();
					for (TransactionOutput to : transactionOutputs) {
						Address address = to.getAddressFromP2PKHScript(networkParameters);
						if (address != null && address.equals(imprintingAddress)) {
							
							LOGGER.warn(sender + " wants to do an Imprinting contract with Us, but we are already imprinted!!!");
							
							break;
	
						} 
					}
					
				}
	
			} catch (Exception ex) {
	
				LOGGER.error("Exception while populating Register", ex);
	
			}
		
		} else if (wallet.equals(nodeStateContext.getUserWallet())) {

			// Populate user register
			List<Address> issuedAddresses = wallet.getIssuedReceiveAddresses();
				
			List<TransactionOutput> to = tx.getOutputs();

			if (to.size() != 4)
				return;

			Script script = tx.getInput(0).getScriptSig();
			Address p_address = new Address(networkParameters, Utils.sha256hash160(script.getPubKey()));

			List<TransactionOutput> ts = new ArrayList<>(to);

			Address u_address = ts.get(0).getAddressFromP2PKHScript(networkParameters);

			if (u_address == null /*|| !addresses.contains(u_address)*/) {
				return;
			}

			if (!WalletUtils.isValidOpReturn(tx)) {
				return;
			}

			Address revoca = ts.get(2).getAddressFromP2PKHScript(networkParameters);
			if(revoca == null || !WalletUtils.isUnspent(tx.getHashAsString(), revoca.toBase58())){
				return;
			}
			
			String providerName = WalletUtils.retrieveNameFromProvider(p_address.toBase58());
			if (providerName == null) {
				return;
			}

			UserChannel userChannel = new UserChannel();
			userChannel.setProviderAddress(p_address.toBase58());
			userChannel.setUserAddress(u_address.toBase58());
			userChannel.setProviderName(providerName);
			
			String opreturn = WalletUtils.getOpReturn(tx);
			
			byte[] op_to_byte = Hex.decode(opreturn);
			
			byte[] bitmask = Arrays.copyOfRange(op_to_byte, 1, 19);
			
			// encode to be saved on db
			String bitmaskToString = new String(Hex.encode(bitmask));
			
			userChannel.setBitmask(bitmaskToString);
			
			try {

				UserRegister userRegister = nodeStateContext.getRegisterFactory().createUserRegister();
				
				userRegister.insertChannel(userChannel);
				
			} catch (Exception e) {

				LOGGER.error("Exception while inserting userChannel", e);

			}

			LOGGER.info("GETCHANNELS txid: " + tx.getHashAsString());
			LOGGER.info("GETCHANNELS provider: " + p_address.toBase58());
			LOGGER.info("GETCHANNELS user: " + u_address);
			LOGGER.info("GETCHANNELS revoca: " + ts.get(2).getAddressFromP2PKHScript(networkParameters));
			LOGGER.info("GETCHANNELS change_provider: " + ts.get(3).getAddressFromP2PKHScript(networkParameters));
			LOGGER.info("GETCHANNELS OPRETURN: " + Hex.toHexString(op_to_byte)  + "\n");

		} else {
			
			LOGGER.warn("Unknown wallet!");
			
		}
	}


}
