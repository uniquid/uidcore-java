package com.uniquid.uniquid_core.function;

/**
 * Defines an object to provide client request information to a function. The function container creates a FunctionRequest object and passes it as an argument to the function's service method.
 */
public interface FunctionRequest {
	
	public static final String SENDER = "SENDER";
	public static final String FUNCTION_NUMBER = "FUNCTION_NUMBER";

	/**
	 * Returns the value of the named attribute as an Object, or null if no attribute of the given name exists.
	 */
	public Object getAttribute(String name);
	
	/**
	 * Retrieves the body of the request as binary data using a ServletInputStream.
	 * @return
	 */
	FunctionInputStream getInputStream();
}
