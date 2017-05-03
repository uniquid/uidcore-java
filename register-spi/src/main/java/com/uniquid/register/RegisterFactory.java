package com.uniquid.register;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

/**
 * Implementation of Factory Design Pattern for DAO (Data Access Object).
 * 
 * Is used to delegate to an implementor class the creation of DAO concrete objects that
 * manage data toward a data source.
 *
 */
public interface RegisterFactory {

	/**
	 * Returns a Provider Register
	 * 
	 * @return
	 * @throws RegisterException
	 */
	public ProviderRegister getProviderRegister() throws RegisterException;

	/**
	 * Returns an User Register
	 * 
	 * @return
	 * @throws RegisterException
	 */
	public UserRegister getUserRegister() throws RegisterException;

}
