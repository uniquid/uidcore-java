package com.uniquid.core.connector;

import com.uniquid.messages.UniquidMessage;

/**
 * Represents a communication between a Provider and a User
 */
public interface EndPoint {
	
	/**
	 * Returns the {@link ProviderRequest} performed by the User
	 * @return the {@link ProviderRequest} performed by the User
	 */
	public UniquidMessage getInputMessage();

	/**
	 * Returns the {@link ProviderResponse} to be returned to the User
	 * @return the {@link ProviderResponse} to be returned to the User
	 */
	public UniquidMessage getOutputMessage();
	
	/**
	 * Closes this instance sending all the communication to the User.
	 */
	public void flush() throws ConnectorException;

}
