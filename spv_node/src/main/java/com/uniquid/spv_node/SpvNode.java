package com.uniquid.spv_node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletFiles;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.google.common.util.concurrent.ListenableFuture;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;

public class SpvNode {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpvNode.class.getName());

	private String mnemonic;
	private long creationTime;

	private NetworkParameters params;

	private File walletFile;
	private File chainFile;

	private Wallet wallet;

	private RegisterFactory registerFactory;

	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

	private SpvNode(Builder builder) throws UnreadableWalletException {

		this.mnemonic = builder._mnemonic;
		this.creationTime = builder._creationTime;
		this.params = builder._params;
		this.walletFile = builder._walletFile;
		this.chainFile = builder._chainFile;
		this.wallet = builder._wallet;
		this.registerFactory = builder._registerFactory;


		// Delegate the creating of the wallet
		wallet = NodeUtils.createOrLoadWallet(mnemonic, creationTime, walletFile, params);

		wallet.autosaveToFile(walletFile, 1000L, TimeUnit.MILLISECONDS, new WalletFiles.Listener() {

			@Override
			public void onBeforeAutoSave(File arg0) {
				// LOGGER.info("SYNC", "before");
			}

			@Override
			public void onAfterAutoSave(File arg0) {
				// LOGGER.info("SYNC", "saved");
			}
		});
		
		wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {

			@Override
			public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
	
				try {
					LOGGER.info("Filling register");
					// ArrayList<Transaction> transactions = new
					// ArrayList<>();
					// transactions.addAll(wallet.getTransactions(false));
					ArrayList<Address> addresses = (ArrayList<Address>) wallet.getIssuedReceiveAddresses();
					ProviderRegister register = registerFactory.createProviderRegister();
	
					ProviderChannel channel = new ProviderChannel();
					String sender = tx.getInput(0).getFromAddress().toBase58();
					//channel.setProviderName(sender); // TODO HACK
					channel.setProviderAddress(sender);
					List<TransactionOutput> transactionOutputs = tx.getOutputs();
					for (TransactionOutput to : transactionOutputs) {
						Address a = to.getAddressFromP2PKHScript(params);
						if (a != null && addresses.contains(a)) {
							channel.setUserAddress(a.toBase58());
						}
					}
	
					// Insert channel if it was not already present
//					if (register.getChannel(channel.getProviderAddress()) == null) {
//						register.insertChannel(channel);
//					}
	
				} catch (Exception ex) {
					LOGGER.error("Exception while populating Register", ex);
				}
	
				LOGGER.info("RECEIVED coins: " + wallet);
			}
		});
		
	}

	public Wallet getWallet() {

		return wallet;

	}
	
	public NetworkParameters getNetworkParameters() {
		
		return params;
	}

	public void startNode() {

		final Runnable walletSyncher = new Runnable() {

			public void run() {

				NodeUtils.syncBC(params, wallet, chainFile, walletFile);
		
			}
		};

		final ScheduledFuture<?> updater = scheduledExecutorService.scheduleAtFixedRate(walletSyncher, 0, 10,
				TimeUnit.MINUTES);

		// Set<Transaction> ts = wallet.getTransactions(false);
		// ArrayList<Transaction> transactions = new ArrayList<>();
		// transactions.addAll(ts);
		// ArrayList<Address> addresses = (ArrayList<Address>)
		// wallet.getIssuedReceiveAddresses();

	}
	
	public void stopNode() {
		
		scheduledExecutorService.shutdown();
		try {
			
			scheduledExecutorService.awaitTermination(20, TimeUnit.SECONDS);
		
		} catch (InterruptedException e) {

			LOGGER.error("Exception while awaiting for termination", e);
		
		}
		
	}

	public boolean hasTransaction(String txid) {
		// NativeSecp256k1.schnorrSign();
		return (wallet.getTransaction(Sha256Hash.of(txid.getBytes())) != null);
	}
	
	public String signTransaction(String s_tx) throws BlockStoreException, InterruptedException, ExecutionException {
        byte[] transaction = Hex.decode(s_tx);
                
        Transaction tx = params.getDefaultSerializer().makeTransaction(transaction);
        
        SendRequest request = SendRequest.forTx(tx);
        
        wallet.signTransaction(request);
        
        LOGGER.info("" + request.tx);
        
        return NodeUtils.sendTransaction(params, wallet, chainFile, request);
        
    }

	public static class Builder {

		private String _mnemonic;
		private long _creationTime;

		private NetworkParameters _params;

		private File _walletFile;
		private File _chainFile;

		private Wallet _wallet;

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

		public Builder set_chainFile(File _chainFile) {
			this._chainFile = _chainFile;
			return this;
		}

		public Builder set_wallet(Wallet _wallet) {
			this._wallet = _wallet;
			return this;
		}

		public Builder set_registerFactory(RegisterFactory _registerFactory) {
			this._registerFactory = _registerFactory;
			return this;
		}

		public SpvNode build() throws UnreadableWalletException {

			return new SpvNode(this);

		}
	}

}