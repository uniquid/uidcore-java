package com.uniquid.core.connector;

import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;

/**
 * Allow a User to send a {@link ProviderRequest} to a Provider and have the {@link ProviderResponse} back
 */
public interface UserClient {

	/**
	 * Send the {@link ProviderRequest} to a Provider and return the {@link ProviderResponse} back.
	 * @param providerRequest the request to send to the Provider
	 * @return the {@link ProviderResponse} from the Provider.
	 * @throws ConnectorException in case a problem occurs.
	 */
	public ProviderResponse sendOutputMessage(ProviderRequest providerRequest) throws ConnectorException;

}
