package com.uniquid.node.state.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.Listener;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.node.state.NodeState;
import com.uniquid.node.state.NodeStateContext;
import com.uniquid.node.utils.WalletUtils;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

/**
 * This class represents an Uniquid Node imprinted and ready to reeive/sign contracts.
 * 
 * @author giuseppe
 *
 */
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
	public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		// We sent some coins. Probably we create a contract as Provider
		if (wallet.equals(nodeStateContext.getProviderWallet())) {

			try {
				
				if (isValidContract(tx)) {
					
					makeContract(tx);
					
				}
				
			} catch (Exception ex) {
	
				LOGGER.error("Exception while creating provider contract", ex);
	
			}
		} else if (wallet.equals(nodeStateContext.getProviderRevokeWallet())) {
			// A contract was revoked on Provider Side!!!
			
			LOGGER.info("A Provider contract was revoked!!!");
			
		} else if (wallet.equals(nodeStateContext.getUserRevokeWallet())) {
			// A contract was revoked on User Side!!!
			
			LOGGER.info("A User contract was revoked!!!");
			
		} else {
			
			LOGGER.info("We sent coins on a wallet that we don't expect!");
			
		}
		
	}

	@Override
	public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		// Received a contract!!!
		if (wallet.equals(nodeStateContext.getProviderWallet())) {
				
			// If is imprinting transaction...
			if (isValidImprintingTransaction(tx)) {
				
				// imprint!
				LOGGER.warn("Another machine tried to do impriting! Skip requests");

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
			
			LOGGER.warn("Received coins on an unknown wallet!");
			
		}
	}
	
	private boolean isValidImprintingTransaction(Transaction tx) {
		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();
		
		// Check output
		List<TransactionOutput> transactionOutputs = tx.getOutputs();
		for (TransactionOutput to : transactionOutputs) {
			Address address = to.getAddressFromP2PKHScript(networkParameters);
			if (address != null && address.equals(imprintingAddress)) {
				return true;
			}
		}

		return false;
	}
	
	private boolean isValidContract(Transaction tx) {
		return true;
	}
	
	private void makeContract(Transaction tx) {
		
		// Transaction already confirmed
		if (tx.getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)) {
			
			doContract(tx);
			
			// DONE
			
		} else {
			
			final Listener listener = new Listener() {

				@Override
				public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {

					try {
						
						if (confidence.equals(TransactionConfidence.ConfidenceType.BUILDING) && reason.equals(ChangeReason.TYPE)) {
					
							doContract(tx);
							
							tx.getConfidence().removeEventListener(this);
							
							LOGGER.info("Contract Done!");
							
						} else if (confidence.equals(TransactionConfidence.ConfidenceType.DEAD) && reason.equals(ChangeReason.TYPE)) {
							
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
	
	private void doContract(Transaction tx) {
		
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
		
		// We need to watch the revoked address
		nodeStateContext.getProviderRevokeWallet().addWatchedAddress(revoke);

		LOGGER.info("GETCHANNELS txid: " + tx.getHashAsString());
		LOGGER.info("GETCHANNELS provider: " + p_address.toBase58());
		LOGGER.info("GETCHANNELS user: " + u_address);
		LOGGER.info("GETCHANNELS revoca: " + ts.get(2).getAddressFromP2PKHScript(networkParameters));
		LOGGER.info("GETCHANNELS change_provider: " + ts.get(3).getAddressFromP2PKHScript(networkParameters));
		LOGGER.info("GETCHANNELS OPRETURN: " + Hex.toHexString(op_to_byte)  + "\n");
		
	}


}
