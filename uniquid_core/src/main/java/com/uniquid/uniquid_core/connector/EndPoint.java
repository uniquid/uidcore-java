package com.uniquid.uniquid_core.connector;

import com.uniquid.uniquid_core.provider.ProviderRequest;
import com.uniquid.uniquid_core.provider.ProviderResponse;

public interface EndPoint {
	
	public ProviderRequest getFunctionRequest();

	public ProviderResponse getFunctionResponse();
	
	public void close();

}
