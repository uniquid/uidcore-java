package com.uniquid.uniquid_core.provider;

import java.io.IOException;
import java.io.PrintWriter;

import com.uniquid.uniquid_core.OutputMessage;

public interface ProviderResponse extends OutputMessage<Object> {
	
	public static final String SENDER = "SENDER";
	public static final String METHOD = "METHOD";
	public static final String PARAMS = "PARAMS";
	public static final String ID = "ID";
	
	public FunctionOutputStream getOutputStream() throws IOException;
	
	public PrintWriter getWriter() throws IOException;
	
	public void setStatus(int status);
	
	public int getStatus();
	
	public void setSender(String sender);
	
	public String getSender();
}
