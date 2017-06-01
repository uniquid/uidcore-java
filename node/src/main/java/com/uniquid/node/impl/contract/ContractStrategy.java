package com.uniquid.node.impl.contract;

import org.bitcoinj.core.Transaction;

/**
 * Implementation of Strategy Design Pattern
 */
public interface ContractStrategy {

	/**
	 * Defines the creation of a contract
	 * 
	 * @param tx
	 * @throws Exception
	 */
	public void manageContractCreation(final Transaction tx) throws Exception;

	/**
	 * Defines the revocation of a contract
	 * 
	 * @param tx
	 * @throws Exception
	 */
	public void manageContractRevocation(final Transaction tx) throws Exception;

}
