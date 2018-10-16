package com.uniquid.core.provider;

import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;

import java.io.IOException;

/**
 * Defines methods that all functions must implement.
 */
public interface Function {

	/**
	 * Called by the function container to indicate to a function that the function is being placed into service.
	 */
	void init(FunctionConfig config) throws FunctionException;

	/**
	 * Called by the function container to indicate to a function that the function is being taken out of service.
	 */
	void destroy();

	/**
	 * Returns a FunctionConfig object, which contains initialization and startup parameters for this function.
	 */
	FunctionConfig getFunctionConfig();

	/**
	 * Returns information about the function, such as author, version, and copyright.
	 */
	String getFunctionInfo();

	/**
	 * Request to execute the function
	 *
	 * @param inputMessage message coming from user
	 * @param outputMessage message coming from provider
	 * @param payload payload from contract
	 * @throws FunctionException in case a problem occurs during executing the function
	 * @throws IOException in case a problem occurs
	 */
	void service(FunctionRequestMessage inputMessage, FunctionResponseMessage outputMessage, byte[] payload) throws FunctionException, IOException;

}
