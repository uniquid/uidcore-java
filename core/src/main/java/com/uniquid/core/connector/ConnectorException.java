package com.uniquid.core.connector;

public class ConnectorException extends Exception {

	/**
	 * Thrown to indicate that there is an error creating or accessing a Connector
	 */
	private static final long serialVersionUID = 1L;

	public ConnectorException() {
        super();
    }
	
	public ConnectorException(String message) {
        super(message);
    }
	
	public ConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
	
	public ConnectorException(Throwable cause) {
        super(cause);
    }
	
}
