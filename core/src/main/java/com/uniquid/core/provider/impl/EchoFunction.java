package com.uniquid.core.provider.impl;

import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;

import java.io.IOException;

/**
 * {@link Function} designed to echo with the content received from the User 
 */
public class EchoFunction extends GenericFunction {

	@Override
	public void service(FunctionRequestMessage inputMessage, FunctionResponseMessage outputMessage, byte[] payload)
			throws FunctionException, IOException {
		
		outputMessage.setResult("UID_echo: " + inputMessage.getParameters());
		
	}
	
}
