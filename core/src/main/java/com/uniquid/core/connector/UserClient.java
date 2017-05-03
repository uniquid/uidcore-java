package com.uniquid.core.connector;

import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;

public interface UserClient {
	
	public ProviderResponse sendOutputMessage(ProviderRequest providerRequest) throws ConnectorException;

}
