package com.uniquid.uniquid_core.connector;

import com.uniquid.uniquid_core.provider.ProviderRequest;
import com.uniquid.uniquid_core.provider.ProviderResponse;

/**
 * Represent a communication between a Provider and a User
 * 
 * @author giuseppe
 *
 */
public interface EndPoint {
	
	/**
	 * Returns the request performed by the User
	 * @return
	 */
	public ProviderRequest getProviderRequest();

	/**
	 * Returns the response to be returned to the User
	 * @return
	 */
	public ProviderResponse getProviderResponse();
	
	/**
	 * Close this EndPoint sending all the communication to the User.
	 */
	public void close();

}
