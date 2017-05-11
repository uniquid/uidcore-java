package com.uniquid.core.provider.impl;

import java.io.IOException;

import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;
import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.exception.FunctionException;

/**
 * {@link Function} designed to echo with the content received from the User 
 */
public class EchoFunction extends GenericFunction {

	@Override
	public void service(ProviderRequest inputMessage, ProviderResponse outputMessage, byte[] payload)
			throws FunctionException, IOException {
		
		outputMessage.setResult("UID_echo: " + inputMessage.getParams());
		
	}
	
}
