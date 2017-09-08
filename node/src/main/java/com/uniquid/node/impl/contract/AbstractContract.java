package com.uniquid.node.impl.contract;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.node.impl.UniquidNodeStateContext;

/**
 * Abstract implementation of State pattern with some boilerplate code for
 * transactions callback
 */
public abstract class AbstractContract implements ContractStrategy {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContract.class);
	
	protected UniquidNodeStateContext uniquidNodeStateContext;
	
	public AbstractContract(UniquidNodeStateContext uniquidNodeStateContext) {
		this.uniquidNodeStateContext = uniquidNodeStateContext;
	}
	
	@Override
	public void manageContractCreation(final Transaction tx) throws Exception {

		// Transaction already confirmed
		if (tx.getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)) {
			
			LOGGER.info("TX {} was included in a block. Checking for a contract", tx.getHashAsString());

			doRealContract(tx);

			// DONE

		} else {

			final Listener listener = new Listener() {

				@Override
				public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {

					try {

						if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)
								&& reason.equals(ChangeReason.TYPE)) {
							
							LOGGER.info("TX {} was included in a block. Checking for a contract", tx.getHashAsString());

							doRealContract(tx);

							tx.getConfidence().removeEventListener(this);

							LOGGER.info("Contract Done!");

						} else if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.DEAD)
								&& reason.equals(ChangeReason.TYPE)) {

							LOGGER.warn("Something bad happened! TX {} is DEAD!", tx.getHashAsString());

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
