package com.uniquid.uniquid_core.function;

/**
 * Defines an object to provide client request information to a function. The function container creates a FunctionRequest object and passes it as an argument to the function's service method.
 */
public interface FunctionRequest {
	
	public static final String SENDER = "SENDER";
	public static final String METHOD = "METHOD";
	public static final String PARAMS = "PARAMS";
	public static final String ID = "ID";

	/**
	 ** Returns the value of a request parameter as a <code>String</code>, or
	 * <code>null</code> if the parameter does not exist.
	 */
	public String getParameter(String name);
	
	/**
	 * Retrieves the body of the request as binary data using a ServletInputStream.
	 * @return
	 */
	FunctionInputStream getInputStream();
}
