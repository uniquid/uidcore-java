package com.uniquid.uniquid_core.provider;

import com.uniquid.uniquid_core.InputMessage;

/**
 * Defines an object to provide client request information to a function. The function container creates a FunctionRequest object and passes it as an argument to the function's service method.
 */
public interface ProviderRequest extends InputMessage<Object> {
	
	public static final String SENDER = "SENDER";
	public static final String METHOD = "METHOD";
	public static final String PARAMS = "PARAMS";
	public static final String ID = "ID";

	/**
	 * Retrieves the body of the request as binary data using a ServletInputStream.
	 * @return
	 */
	FunctionInputStream getInputStream();
}
