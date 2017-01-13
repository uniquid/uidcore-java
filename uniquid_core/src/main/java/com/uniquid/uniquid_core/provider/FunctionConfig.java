package com.uniquid.uniquid_core.provider;

import java.util.Enumeration;

/**
 * A function configuration object used by a function container to pass information to a function during initialization.
 */
public interface FunctionConfig {
	
	 /**
	  *  Returns a reference to the FunctionContext in which the caller is executing.
	  */
	 public FunctionContext getFunctionContext();
	 
	 /**
	  * Returns the names of the function's initialization parameters as an Enumeration of String objects, or an empty Enumeration if the function has no initialization parameters.
	  */
	 public String getInitParameter(String name);
	 
	 /**
	  * Gets the value of the initialization parameter with the given name.
	  */
	 public Enumeration getInitParameterNames();

}
