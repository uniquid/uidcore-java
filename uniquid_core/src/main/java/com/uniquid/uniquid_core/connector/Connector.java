package com.uniquid.uniquid_core.connector;

/**
 * Interface of Connector
 */
public interface Connector {
	
	public abstract EndPoint accept() throws ConnectorException;
	
}
