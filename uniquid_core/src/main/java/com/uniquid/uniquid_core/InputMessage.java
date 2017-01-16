package com.uniquid.uniquid_core;

import java.io.InputStream;

/**
 * This interface represents an input RPC message 
 *
 * @param <T>
 */
public interface InputMessage<T> {
	
	public static final String SENDER = "SENDER";
	public static final String METHOD = "METHOD";
	public static final String PARAMS = "PARAMS";
	public static final String ID = "ID";
	
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
	 * Returns the content of this message
	 */
	public T getContent();

}
