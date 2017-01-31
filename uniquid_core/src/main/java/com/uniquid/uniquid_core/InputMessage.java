package com.uniquid.uniquid_core;

import java.io.InputStream;

/**
 * This interface represents an input RPC message 
 *
 * @param <T>
 */
public interface InputMessage<T> {
	
	public static final String SENDER = "SENDER";
	public static final String RPC_METHOD = "RPC_METHOD";
	public static final String PARAMS = "PARAMS";
	public static final String ID = "ID";
	public static final String RESULT = "RESULT";
	public static final String ERROR = "ERROR";
	
	/**
	 * Retrieves the body of the message as binary data using an InputStream.
	 * @return
	 */
	public InputStream getInputStream();
	
	/**
	 ** Returns the value of a request parameter as a <code>String</code>, or
	 * <code>null</code> if the parameter does not exist.
	 */
	public String getParameter(String name);
	
	/**
	 * Returns the payload of this message
	 */
	public T getPayload();

}
