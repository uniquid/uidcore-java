package com.uniquid.node.impl.state;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.node.impl.UniquidNodeStateContext;
import com.uniquid.node.impl.contract.ContractStrategy;
import com.uniquid.node.impl.contract.ImprintingContract;
import com.uniquid.node.impl.contract.UserContract;
import com.uniquid.node.impl.utils.UniquidNodeStateUtils;

/**
 * State that represents a node in Imprinting State: it is ready to receive coins.
 */
public class ImprintingState implements UniquidNodeState {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ImprintingState.class);

	protected UniquidNodeStateContext uniquidNodeStateContext;

	public ImprintingState(final UniquidNodeStateContext uniquidNodeStateContext) {

		this.uniquidNodeStateContext = uniquidNodeStateContext;

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
				if (UniquidNodeStateUtils.isValidImprintingTransaction(tx, uniquidNodeStateContext.getNetworkParameters(), uniquidNodeStateContext.getImprintingAddress())) {

					LOGGER.info("Valid Imprinting contract received!");

					// imprint!
					ContractStrategy contractStrategy = createImprintingContract();
					
					contractStrategy.manageContractCreation(tx);

					// We can move now to ReadyState
					uniquidNodeStateContext.setUniquidNodeState(getReadyState());

				} else {

					LOGGER.warn("Not valid imprinting contract. Skipping");

				}

			} catch (Exception ex) {

				LOGGER.error("Exception while imprinting", ex);

			}

		} else if (wallet.equals(uniquidNodeStateContext.getUserWallet()) || "user".equalsIgnoreCase(wallet.getDescription())) {

			LOGGER.info("Received coins on user wallet");

			try {

				ContractStrategy contractStrategy = createUserContract();
				
				contractStrategy.manageContractCreation(tx);

			} catch (Exception ex) {

				LOGGER.error("Exception while creating user contract", ex);

			}

		} else {

			LOGGER.warn("We received coins on a wallet that we don't expect!");

		}

	}

	@Override
	public com.uniquid.node.UniquidNodeState getNodeState() {

		return com.uniquid.node.UniquidNodeState.IMPRINTING;

	}

	protected UniquidNodeState getReadyState() {
		return new ReadyState(uniquidNodeStateContext);
	}
	
	protected ContractStrategy createImprintingContract() {
		
		return new ImprintingContract(uniquidNodeStateContext.getNetworkParameters(), uniquidNodeStateContext.getUserWallet(),
				uniquidNodeStateContext.getProviderWallet(), uniquidNodeStateContext.getRegisterFactory(),
				uniquidNodeStateContext.getUniquidNodeEventService(), uniquidNodeStateContext.getPublicKey(), uniquidNodeStateContext.getImprintingAddress());
		
	}
	
	protected ContractStrategy createUserContract() {
		
		return new UserContract(uniquidNodeStateContext.getNetworkParameters(), uniquidNodeStateContext.getUserWallet(),
				uniquidNodeStateContext.getProviderWallet(), uniquidNodeStateContext.getRegisterFactory(),
				uniquidNodeStateContext.getUniquidNodeEventService(), uniquidNodeStateContext.getPublicKey());
	
	}

}