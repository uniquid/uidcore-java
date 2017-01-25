package com.uniquid.uniquid_core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * This interface represents an output RPC message
 *
 * @param <T>
 */
public interface OutputMessage<T> {
	
	public static final String SENDER = "SENDER";
	public static final String RECEIVER = "RECEIVER";
	public static final String RECEIVER_ADDRESS = "RECEIVER_ADDRESS";
	public static final String METHOD = "METHOD";
	public static final String PARAMS = "PARAMS";
	public static final String ID = "ID";
	public static final String ERROR = "ERROR";
	public static final String RESULT = "RESULT";
	
	public OutputStream getOutputStream() throws IOException;

	public PrintWriter getWriter() throws IOException;
	
	public void setParameter(String name, Object value);
	
	public Object getParameter(String name);
	
	public T getContent();
	
}
