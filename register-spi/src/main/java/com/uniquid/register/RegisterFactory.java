package com.uniquid.register;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.transaction.TransactionManager;
import com.uniquid.register.user.UserRegister;

/**
 * Implementation of Factory Design Pattern for DAO (Data Access Object).
 * 
 * Is used to delegate to an implementor class the creation of DAO concrete objects that
 * manage data toward a data source.
 */
public interface RegisterFactory {

	/**
	 * Returns a ProviderRegister instance
	 * 
	 * @return a ProviderRegister instance
	 * @throws RegisterException in case of problem occurs
	 */
	public ProviderRegister getProviderRegister() throws RegisterException;

	/**
	 * Returns a UserRegister instance
	 * 
	 * @return a UserRegister instance
	 * @throws RegisterException in case of problem occurs
	 */
	public UserRegister getUserRegister() throws RegisterException;
	
	/**
	 * Return a transaction manager
	 * @return
	 * @throws RegisterException
	 */
	public TransactionManager getTransactionManager() throws RegisterException;

}
