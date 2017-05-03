package com.uniquid.core.connector;

/**
 * The connector component support a custom transport protocol and enables the system to works independently from its implementation.
 */
public interface Connector {

	/**
	 * Starts the connector
	 */
	public void start() throws ConnectorException;
	
	/**
	 * Stop the connector
	 */
	public void stop() throws ConnectorException;
	
	/**
	 * Listens for a connection to be made to this connector and accepts it. The method blocks until a connection is made
	 * 
	 * @return {@link EndPoint}
	 * @throws ConnectorException
	 */
	public EndPoint accept() throws ConnectorException;
	
}
