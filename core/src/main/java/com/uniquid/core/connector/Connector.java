package com.uniquid.core.connector;

/**
 * Connector interface hides the specifics of a communication protocol and allows the library to works independently from
 * a particular implementation.
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
	 * Listens for a connection to be made to this connector and accepts it. The method blocks until a
	 * connection is made
	 * 
	 * @return {@link EndPoint} the endpoint that wrap the communication with the User.
	 * @throws ConnectorException in case a problem occurs.
	 */
	public EndPoint accept() throws ConnectorException;
	
}
