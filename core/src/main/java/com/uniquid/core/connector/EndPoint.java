package com.uniquid.core.connector;

import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;

/**
 * Represents a communication between a Provider and a User
 */
public interface EndPoint {
	
	/**
	 * Returns the {@link ProviderRequest} performed by the User
	 * @return the {@link ProviderRequest} performed by the User
	 */
	public ProviderRequest getInputMessage();

	/**
	 * Returns the {@link ProviderResponse} to be returned to the User
	 * @return the {@link ProviderResponse} to be returned to the User
	 */
	public ProviderResponse getOutputMessage();
	
	/**
	 * Closes this instance sending all the communication to the User.
	 */
	public void flush() throws ConnectorException;

}
