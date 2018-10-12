package com.uniquid.node.impl.state;

import com.uniquid.node.impl.UniquidNodeConfiguration;
import com.uniquid.node.impl.UniquidNodeStateContext;
import com.uniquid.node.impl.contract.ContractStrategy;
import com.uniquid.node.impl.contract.ProviderContract;
import com.uniquid.node.impl.contract.UserContract;
import com.uniquid.node.impl.utils.UniquidNodeStateUtils;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State that represents a Node ready to work.
 */
public class ReadyState<T extends UniquidNodeConfiguration> implements UniquidNodeState {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReadyState.class);

	protected UniquidNodeStateContext<T> uniquidNodeStateContext;

	public ReadyState(final UniquidNodeStateContext<T> uniquidNodeStateContext) {

		this.uniquidNodeStateContext = uniquidNodeStateContext;

	}

	@Override
	public void onCoinsSent(final Wallet wallet, final Transaction tx) {

		// We sent some coins. Probably we created a contract as Provider
		if (wallet.equals(uniquidNodeStateContext.getProviderWallet()) || "provider".equalsIgnoreCase(wallet.getDescription())) {

			LOGGER.info("Sent coins from provider wallet");

			try {

				LOGGER.info("Creating provider contract!");
				ContractStrategy contractStrategy = createProviderContract();
				
				contractStrategy.manageContractCreation(tx);

			} catch (Exception ex) {

				LOGGER.error("Exception while creating provider contract", ex);

			}

		} else if (wallet.equals(uniquidNodeStateContext.getUserWallet()) || "user".equalsIgnoreCase(wallet.getDescription())) {

			LOGGER.info("Sent coins from user wallet");

			// if (UniquidNodeStateUtils.isValidRevokeContract(tx,
			// nodeStateContext)) {

			try {
				LOGGER.info("Revoking contract!");
				ContractStrategy contractStrategy = createProviderContract();
				
				contractStrategy.manageContractRevocation(tx);

			} catch (Exception ex) {

				LOGGER.error("Exception while revoking provider contract", ex);

			}

		} else {

			LOGGER.info("We sent coins from a wallet that we don't expect!");

		}

	}

	@Override
	public void onCoinsReceived(final Wallet wallet, final Transaction tx) {

		// Received a contract!!!
		if (wallet.equals(uniquidNodeStateContext.getProviderWallet()) || "provider".equalsIgnoreCase(wallet.getDescription())) {

			LOGGER.info("Received coins on provider wallet");
			
			// If is imprinting transaction...
			if (UniquidNodeStateUtils.isValidImprintingTransaction(tx, uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters(), uniquidNodeStateContext.getImprintingAddress())) {

				// imprint!
				LOGGER.warn("Attention! Another machine tried to imprint US! Skip request!");

			} else if (UniquidNodeStateUtils.isValidRevokeContract(tx, uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters(), uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory())) {

				try {
					// Revoking a contract will move coins from provider wallet to another provider address
					LOGGER.info("Revoking contract!");

					ContractStrategy contractStrategy = createProviderContract();
					
					contractStrategy.manageContractRevocation(tx);

				} catch (Exception ex) {

					LOGGER.error("Exception", ex);
				}

			} else {

				LOGGER.info("Unknown contract");

			}

		} else if (wallet.equals(uniquidNodeStateContext.getUserWallet()) || "user".equalsIgnoreCase(wallet.getDescription())) {

			LOGGER.info("Received coins on user wallet");

			if (UniquidNodeStateUtils.isValidRevokeUserContract(tx, uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters(), uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory())) {

				try {

					LOGGER.info("Revoking user contract!");

					ContractStrategy contractStrategy = createUserContract();

					contractStrategy.manageContractRevocation(tx);

				} catch (Exception ex) {

					LOGGER.error("Exception", ex);
				}

			} else {

				try {

					LOGGER.info("Creating user contract!");
					ContractStrategy contractStrategy = createUserContract();

					contractStrategy.manageContractCreation(tx);

				} catch (Exception ex) {

					LOGGER.error("Exception while creating provider contract", ex);

				}

			}

		} else {

			LOGGER.warn("We received coins on a wallet that we don't expect!");

		}

	}

	@Override
	public com.uniquid.node.UniquidNodeState getNodeState() {

		return com.uniquid.node.UniquidNodeState.READY;

	}

	protected ContractStrategy createProviderContract() {
		
		return new ProviderContract(uniquidNodeStateContext);

	}
	
	protected ContractStrategy createUserContract() {
		
		return new UserContract(uniquidNodeStateContext);
	
	}
	
}
