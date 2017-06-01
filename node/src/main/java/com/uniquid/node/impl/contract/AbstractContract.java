package com.uniquid.node.impl.contract;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.Listener;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.node.impl.UniquidNodeEventService;
import com.uniquid.register.RegisterFactory;

/**
 * Abstract implementation of State pattern with some boilerplate code for
 * transactions callback
 */
public abstract class AbstractContract implements ContractStrategy {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContract.class);
	
	protected NetworkParameters networkParameters;
	protected Wallet userWallet;
	protected Wallet providerWallet;
	protected RegisterFactory registerFactory;
	protected UniquidNodeEventService uniquidNodeEventService;
	protected String pubKey;
	
	public AbstractContract(final NetworkParameters networkParameters, final Wallet userWallet, final Wallet providerWallet,
			final RegisterFactory registerFactory, final UniquidNodeEventService uniquidNodeEventService,
			String pubKey) {

		this.networkParameters = networkParameters;
		this.userWallet = userWallet;
		this.providerWallet = providerWallet;
		this.registerFactory = registerFactory;
		this.uniquidNodeEventService = uniquidNodeEventService;
		this.pubKey = pubKey;

	}

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
