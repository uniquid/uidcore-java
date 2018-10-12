package com.uniquid.node.impl.state;

import com.uniquid.node.impl.UniquidNodeConfiguration;
import com.uniquid.node.impl.UniquidNodeStateContext;
import com.uniquid.node.impl.contract.ContractStrategy;
import com.uniquid.node.impl.contract.ImprintingContract;
import com.uniquid.node.impl.contract.UserContract;
import com.uniquid.node.impl.utils.UniquidNodeStateUtils;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State that represents a node in Imprinting State: it is ready to receive coins.
 */
public class ImprintingState<T extends UniquidNodeConfiguration> implements UniquidNodeState {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ImprintingState.class);

	protected UniquidNodeStateContext<T> uniquidNodeStateContext;
	private UniquidNodeState readyState;

	public ImprintingState(final UniquidNodeStateContext<T> uniquidNodeStateContext, UniquidNodeState readyState) {

		this.uniquidNodeStateContext = uniquidNodeStateContext;
		this.readyState = readyState;

	}

	@Override
	public void onCoinsSent(final Wallet wallet, final Transaction tx) {

		LOGGER.warn("We sent coins from a wallet that we don't expect!");

	}

	@Override
	public void onCoinsReceived(final Wallet wallet, final Transaction tx) {

		if (wallet.equals(uniquidNodeStateContext.getProviderWallet()) || "provider".equalsIgnoreCase(wallet.getDescription())) {

			LOGGER.info("Received coins on provider wallet");

			try {
				
				// If is imprinting transaction...
				if (UniquidNodeStateUtils.isValidImprintingTransaction(tx, uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters(), uniquidNodeStateContext.getImprintingAddress())) {

					LOGGER.info("Valid Imprinting contract received!");

					// imprint!
					ContractStrategy contractStrategy = createImprintingContract();
					
					contractStrategy.manageContractCreation(tx);

					// We can move now to ReadyState
					uniquidNodeStateContext.setUniquidNodeState(readyState);

				} else {

					LOGGER.warn("Not valid imprinting contract. Skipping");

				}

			} catch (Exception ex) {

				LOGGER.error("Exception while imprinting", ex);

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

		return com.uniquid.node.UniquidNodeState.IMPRINTING;

	}

	protected ContractStrategy createImprintingContract() {
		
		return new ImprintingContract(uniquidNodeStateContext);
		
	}
	
	protected ContractStrategy createUserContract() {
		
		return new UserContract(uniquidNodeStateContext);
	
	}

}