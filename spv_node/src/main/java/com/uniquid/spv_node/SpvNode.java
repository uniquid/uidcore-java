package com.uniquid.spv_node;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletFiles;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;

public class SpvNode {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpvNode.class.getName());
	private static String URL_REGISTRY = "http://104.130.230.85:8080/registry";
    private static String URL_UTXO = "http://appliance3.uniquid.co:8080/insight-api/addr/%1&s/utxo";

	private String mnemonic;
	private long creationTime;

	private NetworkParameters params;

	private File walletFile;
	private File userFile;
	private File chainFile;
	private File userChainFile;

	private Wallet masterWallet;
	private Wallet userWallet;

	private RegisterFactory registerFactory;

	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	private SpvNode(Builder builder) throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

		this.mnemonic = builder._mnemonic;
		this.creationTime = builder._creationTime;
		this.params = builder._params;
		this.walletFile = builder._walletFile;
		this.userFile = builder._userFile;
		this.chainFile = builder._chainFile;
		this.userChainFile = builder._userChainFile;
		this.masterWallet = builder._masterWallet;
		this.userWallet = builder._userWallet;
		this.registerFactory = builder._registerFactory;

		// Delegate the creating of the wallet
		masterWallet = NodeUtils.createOrLoadMasterWallet(mnemonic, creationTime, walletFile, params);
		
		userWallet = NodeUtils.createOrLoadUserWallet(mnemonic, creationTime, userFile, params);

		masterWallet.autosaveToFile(walletFile, 1000L, TimeUnit.MILLISECONDS, new WalletFiles.Listener() {

			@Override
			public void onBeforeAutoSave(File arg0) {
				// LOGGER.info("SYNC", "before");
			}

			@Override
			public void onAfterAutoSave(File arg0) {
				// LOGGER.info("SYNC", "saved");
			}
		});
		
		userWallet.autosaveToFile(userFile, 1000L, TimeUnit.MILLISECONDS, new WalletFiles.Listener() {

			@Override
			public void onBeforeAutoSave(File arg0) {
				// LOGGER.info("SYNC", "before");
			}

			@Override
			public void onAfterAutoSave(File arg0) {
				// LOGGER.info("SYNC", "saved");
			}
		});

//		masterWallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
//
//			@Override
//			public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
//
//				try {
//					LOGGER.info("Filling register");
//					// ArrayList<Transaction> transactions = new
//					// ArrayList<>();
//					// transactions.addAll(wallet.getTransactions(false));
//					ArrayList<Address> addresses = (ArrayList<Address>) masterWallet.getIssuedReceiveAddresses();
//					ProviderRegister register = registerFactory.createProviderRegister();
//
//					ProviderChannel channel = new ProviderChannel();
//					String sender = tx.getInput(0).getFromAddress().toBase58();
//					// channel.setProviderName(sender); // TODO HACK
//					channel.setProviderAddress(sender);
//					List<TransactionOutput> transactionOutputs = tx.getOutputs();
//					for (TransactionOutput to : transactionOutputs) {
//						Address a = to.getAddressFromP2PKHScript(params);
//						if (a != null && addresses.contains(a)) {
//							channel.setUserAddress(a.toBase58());
//						}
//					}
//
//					// Insert channel if it was not already present
//					// if (register.getChannel(channel.getProviderAddress()) ==
//					// null) {
//					// register.insertChannel(channel);
//					// }
//
//				} catch (Exception ex) {
//					LOGGER.error("Exception while populating Register", ex);
//				}
//
//				LOGGER.info("RECEIVED coins: " + masterWallet);
//			}
//		});
		
	}

	public Wallet getWallet() {

		return masterWallet;

	}
	
	public Wallet getUserWallet() {

		return userWallet;

	}

	public NetworkParameters getNetworkParameters() {

		return params;
	}

	public void startNode() {

		final Runnable walletSyncher = new Runnable() {

			public void run() {

				NodeUtils.syncBC(params, masterWallet, chainFile, walletFile, creationTime);

				NodeUtils.syncBC(params, userWallet, userChainFile, userFile, creationTime);

				// Populate provider register
				Set<Transaction> transactions = masterWallet.getTransactions(false);
				LOGGER.info("GETCHANNELS t.size: " + transactions.size());
//				List<Address> addresses = masterWallet.getIssuedReceiveAddresses();
				
				List<DeterministicKey> keys = masterWallet.getActiveKeyChain().getLeafKeys();
				List<String> addresses = new ArrayList<>();
				for (ECKey key : keys) {
					addresses.add(key.toAddress(params).toBase58());
				}
				
				for (Transaction t : transactions) {
					
					List<TransactionOutput> to = t.getOutputs();

					if (to.size() != 4)
						continue;

					Script script = t.getInput(0).getScriptSig();
					Address p_address = new Address(params, Utils.sha256hash160(script.getPubKey()));

					List<TransactionOutput> ts = new ArrayList<>(to);

					Address u_address = ts.get(0).getAddressFromP2PKHScript(params);

					// We are provider!!!
//					if (u_address == null || !addresses.contains(u_address.toBase58())) {
//						continue;
//					}

					if (!isValidOpReturn(t)) {
						continue;
					}

					Address revoca = ts.get(2).getAddressFromP2PKHScript(params);
					if(revoca == null || !isUnspent(t.getHashAsString(), revoca.toBase58())){
						continue;
					}

					ProviderChannel providerChannel = new ProviderChannel();
					providerChannel.setProviderAddress(p_address.toBase58());
					providerChannel.setUserAddress(u_address.toBase58());
					
					String opreturn = getOpReturn(t);
					
					byte[] op_to_byte = Hex.decode(opreturn);
					
					byte[] bitmask = Arrays.copyOfRange(op_to_byte, 1, 19);
					
					// encode to be saved on db
					String bitmaskToString = new String(Hex.encode(bitmask));
					
					providerChannel.setBitmask(bitmaskToString);
					
					try {

						ProviderRegister providerRegister = registerFactory.createProviderRegister();
						
						providerRegister.insertChannel(providerChannel);
						
					} catch (Exception e) {

						LOGGER.error("Exception while inserting providerregister", e);

					}

					LOGGER.info("GETCHANNELS txid: " + t.getHashAsString());
					LOGGER.info("GETCHANNELS provider: " + p_address.toBase58());
					LOGGER.info("GETCHANNELS user: " + u_address);
					LOGGER.info("GETCHANNELS revoca: " + ts.get(2).getAddressFromP2PKHScript(params));
					LOGGER.info("GETCHANNELS change_provider: " + ts.get(3).getAddressFromP2PKHScript(params));
					LOGGER.info("GETCHANNELS OPRETURN: " + Hex.toHexString(op_to_byte)  + "\n");

				}
				
				// Populate user register
				transactions = userWallet.getTransactions(false);
				LOGGER.info("GETCHANNELS t.size: " + transactions.size());
				List<Address> issuedAddresses = userWallet.getIssuedReceiveAddresses();
				for (Transaction t : transactions) {
					
					List<TransactionOutput> to = t.getOutputs();

					if (to.size() != 4)
						continue;

					Script script = t.getInput(0).getScriptSig();
					Address p_address = new Address(params, Utils.sha256hash160(script.getPubKey()));

					List<TransactionOutput> ts = new ArrayList<>(to);

					Address u_address = ts.get(0).getAddressFromP2PKHScript(params);

					if (u_address == null || !addresses.contains(u_address)) {
						continue;
					}

					if (!isValidOpReturn(t)) {
						continue;
					}

					Address revoca = ts.get(2).getAddressFromP2PKHScript(params);
					if(revoca == null || !isUnspent(t.getHashAsString(), revoca.toBase58())){
						continue;
					}

					ProviderChannel providerChannel = new ProviderChannel();
					providerChannel.setProviderAddress(p_address.toBase58());
					providerChannel.setUserAddress(u_address.toBase58());
					
					String opreturn = getOpReturn(t);
					
					byte[] op_to_byte = Hex.decode(opreturn);
					
					byte[] bitmask = Arrays.copyOfRange(op_to_byte, 1, 19);
					
					// encode to be saved on db
					String bitmaskToString = new String(Hex.encode(bitmask));
					
					providerChannel.setBitmask(bitmaskToString);
					
//					try {
//
//						ProviderRegister providerRegister = registerFactory.createProviderRegister();
//						
//						providerRegister.insertChannel(providerChannel);
//						
//					} catch (Exception e) {
//
//						LOGGER.error("Exception while inserting providerregister", e);
//
//					}

					LOGGER.info("GETCHANNELS txid: " + t.getHashAsString());
					LOGGER.info("GETCHANNELS provider: " + p_address.toBase58());
					LOGGER.info("GETCHANNELS user: " + u_address);
					LOGGER.info("GETCHANNELS revoca: " + ts.get(2).getAddressFromP2PKHScript(params));
					LOGGER.info("GETCHANNELS change_provider: " + ts.get(3).getAddressFromP2PKHScript(params));
					LOGGER.info("GETCHANNELS OPRETURN: " + Hex.toHexString(op_to_byte)  + "\n");

				}

			}
		};

		final ScheduledFuture<?> updater = scheduledExecutorService.scheduleAtFixedRate(walletSyncher, 0, 15,
				TimeUnit.MINUTES);

		// Set<Transaction> ts = wallet.getTransactions(false);
		// ArrayList<Transaction> transactions = new ArrayList<>();
		// transactions.addAll(ts);
		// ArrayList<Address> addresses = (ArrayList<Address>)
		// wallet.getIssuedReceiveAddresses();

	}
	
	/**
     * Check if a Transaction have a valid (Uniquid) OP_RETURN
     * */
	public static boolean isValidOpReturn(Transaction tx){
        String op_return = getOpReturn(tx);
        return Hex.decode(op_return).length == 80;
    }
    
    public static boolean isUnspent(String txid, String address){
        String result = httpGet(URL_UTXO, address);

        if(result == null)
            return false;

        JSONArray jArray = new JSONArray(result);

        for(int i = 0; i < jArray.length(); i++){
            JSONObject jsonObject = jArray.getJSONObject(i);
            if(jsonObject.getString("txid").equals(txid) && jsonObject.getInt("vout") == 2)
                return true;
        }

        return false;
    }
    
    public static String httpGet(String url, String param){
    	try {
            HttpURLConnection connection;
            if(param != null){
                connection = (HttpURLConnection) new URL(url.replace("%1&s", param)).openConnection();
            } else {
                connection = (HttpURLConnection) new URL(url).openConnection();
            }
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if(responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                LOGGER.info("HTTPGET_RESP: " + response.toString());
                return response.toString();
            }
        } catch (java.net.ProtocolException e) {
        		LOGGER.error("Exception", e);
        } catch (MalformedURLException e) {
        		LOGGER.error("Exception", e);
        } catch (IOException e) {
        		LOGGER.error("Exception", e);
        }

        return null;
    }

    /**
     * Retrieve OP_RETURN from a Transaction
     * */
    public static String getOpReturn(Transaction tx){
        List<TransactionOutput> to = tx.getOutputs();
        Script script = to.get(1).getScriptPubKey();
        if(script.isOpReturn()){
            String script_string = script.toString();
            return script_string.substring(script_string.indexOf("[")+1, script_string.indexOf("]"));
        }
        return null;
    }

	public void stopNode() {

		scheduledExecutorService.shutdown();
		try {

			scheduledExecutorService.awaitTermination(20, TimeUnit.SECONDS);

		} catch (InterruptedException e) {

			LOGGER.error("Exception while awaiting for termination", e);

		}

	}

	public String signTransaction(String s_tx)
			throws BlockStoreException, InterruptedException, ExecutionException, InsufficientMoneyException, Exception {
	
		Transaction originalTransaction = params.getDefaultSerializer().makeTransaction(Hex.decode(s_tx));

		String transactionToString = Hex.toHexString(originalTransaction.bitcoinSerialize());
		LOGGER.info("Serialized unsigned transaction: " + transactionToString);
		
		SendRequest send = SendRequest.forTx(originalTransaction);
		
		// fix our tx
		((UniquidWallet) masterWallet).completeTransaction(send);
		
		// delegate to walled the signing
		((UniquidWallet) masterWallet).signTransaction(send);
		
		String sr = Hex.toHexString(originalTransaction.bitcoinSerialize());
	    
		LOGGER.info("Serialized SIGNED transaction: " + sr);
		
		return NodeUtils.sendTransaction(params, masterWallet, chainFile, send);
	}

	public static class Builder {

		private String _mnemonic;
		private long _creationTime;

		private NetworkParameters _params;

		private File _walletFile;
		private File _userFile;
		private File _chainFile;
		private File _userChainFile;

		private Wallet _masterWallet;
		private Wallet _userWallet;

		private RegisterFactory _registerFactory;

		public Builder set_mnemonic(String _mnemonic) {
			this._mnemonic = _mnemonic;
			return this;
		}

		public Builder set_creationTime(long _creationTime) {
			this._creationTime = _creationTime;
			return this;
		}

		public Builder set_params(NetworkParameters _params) {
			this._params = _params;
			return this;
		}

		public Builder set_walletFile(File _walletFile) {
			this._walletFile = _walletFile;
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

		public Builder set_masterWallet(Wallet _masterWallet) {
			this._masterWallet = _masterWallet;
			return this;
		}
		
		public Builder set_userWallet(Wallet _userWallet) {
			this._userWallet = _userWallet;
			return this;
		}

		public Builder set_registerFactory(RegisterFactory _registerFactory) {
			this._registerFactory = _registerFactory;
			return this;
		}

		public SpvNode build() throws UnreadableWalletException, NoSuchAlgorithmException, UnsupportedEncodingException {

			return new SpvNode(this);

		}
	}
	
}