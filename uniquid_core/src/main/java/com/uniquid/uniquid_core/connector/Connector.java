package com.uniquid.uniquid_core.connector;

import com.uniquid.uniquid_core.function.FunctionRequest;
import com.uniquid.uniquid_core.function.FunctionResponse;

/**
 * Interface of Connector
 */
public interface Connector {
	
	public abstract FunctionRequest receiveRequest() throws ConnectorException;
	
	public abstract void sendResponse(FunctionResponse response) throws ConnectorException;
	
}
