package com.uniquid.uniquid_core.connector;

import com.uniquid.uniquid_core.function.FunctionRequest;
import com.uniquid.uniquid_core.function.FunctionResponse;

public interface EndPoint {
	
	public FunctionRequest getFunctionRequest();

	public FunctionResponse getFunctionResponse();
	
	public void close();

}
