package com.uniquid.core.connector;

import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;

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
	public ProviderRequest getInputMessage();

	/**
	 * Returns the response to be returned to the User
	 * @return
	 */
	public ProviderResponse getOutputMessage();
	
	/**
	 * Close this EndPoint sending all the communication to the User.
	 */
	public void flush() throws ConnectorException;

}
