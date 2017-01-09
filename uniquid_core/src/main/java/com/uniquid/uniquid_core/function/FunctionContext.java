package com.uniquid.uniquid_core.function;

/**
 * Defines a set of methods that a function uses to communicate with its function container, for example, to get the MIME type of a file, dispatch requests, or write to a log file.
 */
public interface FunctionContext {

	public int getMajorVersion();
	
	public int getMinorVersion();
	
	public java.lang.String getServerInfo();
	
}
