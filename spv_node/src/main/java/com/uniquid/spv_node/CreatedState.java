package com.uniquid.spv_node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

public class CreatedState implements NodeState {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CreatedState.class.getName());
	
	private NodeStateContext nodeStateContext;
	private NetworkParameters networkParameters;
	private Address imprintingAddress;
	private boolean imprinted;
	
	public CreatedState(NodeStateContext nodeStateContext, Address imprintingAddress) {
		this.nodeStateContext = nodeStateContext;
		this.networkParameters = nodeStateContext.getNetworkParameters();
		this.imprintingAddress = imprintingAddress;
		this.imprinted = false;
	}
	
	@Override
	public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		// MANAGE ONLY CONTRACT REVOCATION
	}

	@Override
	public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		// Received a contract!!!

		if (wallet.equals(nodeStateContext.getProviderWallet())) {
			
			try {
				// skip unconfirmed transactions
				if (!tx.isPending() && !imprinted) {
					
					// Retrieve sender
					String sender = tx.getInput(0).getFromAddress().toBase58();
					
					// Check output
					List<TransactionOutput> transactionOutputs = tx.getOutputs();
					for (TransactionOutput to : transactionOutputs) {
						Address address = to.getAddressFromP2PKHScript(networkParameters);
						if (address != null && address.equals(imprintingAddress)) {
							
							// This is our imprinter!!!
							
							ProviderRegister providerRegister = nodeStateContext.getRegisterFactory().createProviderRegister();
							
							ProviderChannel providerChannel = new ProviderChannel();
							providerChannel.setUserAddress(sender);
							providerChannel.setProviderAddress(imprintingAddress.toBase58());
							providerChannel.setBitmask("ffffffff0000000000000000000000000000");
							providerChannel.setRevokeAddress(sender);
							
							providerRegister.insertChannel(providerChannel);
							
							// We are imprinted!!!
							imprinted = true;
							
							// Jump to ready state
							nodeStateContext.setNodeState(new ReadyState(nodeStateContext, imprintingAddress));
							
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
