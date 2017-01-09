package com.uniquid.uniquid_core.function;

import java.io.IOException;

/**
 * Defines methods that all functions must implement.
 *
 */
public interface Function {
	
	/**
	 * Called by the function container to indicate to a function that the function is being taken out of service.
	 */
	public void destroy();
	
	/**
	 * Returns a FunctionConfig object, which contains initialization and startup parameters for this function.
	 */
	public FunctionConfig getFunctionConfig();
	
	/**
	 * Returns information about the function, such as author, version, and copyright.
	 */
	public String getFunctionInfo();
	
	/**
	 * Called by the function container to indicate to a function that the function is being placed into service.
	 */
	public void init(FunctionConfig config) throws FunctionException;
	
	/**
	 * Request to execute the function
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void service(FunctionRequest functionRequest, FunctionResponse functionResponse) throws FunctionException, IOException;
	
}
