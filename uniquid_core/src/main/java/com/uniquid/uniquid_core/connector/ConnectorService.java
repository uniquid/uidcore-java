package com.uniquid.uniquid_core.connector;

import com.uniquid.uniquid_core.function.FunctionRequest;
import com.uniquid.uniquid_core.function.FunctionResponse;

public abstract class ConnectorService {
	
	public abstract FunctionRequest receiveRequest();
	
	public abstract void sendResponse(FunctionResponse response);
	
}
