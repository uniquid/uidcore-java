package com.uniquid.uniquid_core.function;

import java.io.IOException;
import java.io.PrintWriter;

public interface FunctionResponse {
	
	public static final String SENDER = "SENDER";
	public static final String RESULT = "RESULT";
	public static final String ERROR = "ERROR";
	public static final String ID = "ID";

	public FunctionOutputStream getOutputStream() throws IOException;
	
	public PrintWriter getWriter() throws IOException;
	
	public void setStatus(int status);
	
	public int getStatus();
}
