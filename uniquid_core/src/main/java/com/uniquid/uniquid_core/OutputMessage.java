package com.uniquid.uniquid_core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public interface OutputMessage<T> {
	
	public static final String SENDER = "SENDER";
	public static final String METHOD = "METHOD";
	public static final String PARAMS = "PARAMS";
	public static final String ID = "ID";
	public static final String ERROR = "ERROR";
	
	public OutputStream getOutputStream() throws IOException;

	public PrintWriter getWriter() throws IOException;
	
	public void setParameter(String name, Object value);
	
	public String getDestination();
	
	public void setDestination(String destination);
	
	public T getContent();
	
}
