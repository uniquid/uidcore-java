package com.uniquid.core.user;

import com.uniquid.core.connector.ConnectorException;
import com.uniquid.core.messages.UniquidMessage;

/**
 * Allow an User to send a {@link UserRequest} to a Provider and have the {@link UserResponse} back
 */
public interface UserClient {

	/**
	 * Send the {@link UserRequest} to a Provider and return the {@link UserResponse} back.
	 * @param userRequest the request to send to the Provider
	 * @return the {@link UserResponse} from the Provider.
	 * @throws ConnectorException in case a problem occurs.
	 */
	public UniquidMessage execute(UniquidMessage userRequest) throws ConnectorException;

}
