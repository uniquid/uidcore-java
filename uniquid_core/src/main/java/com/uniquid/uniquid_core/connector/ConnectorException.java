package com.uniquid.uniquid_core.connector;

public class ConnectorException extends Exception {

	/**
	 * 
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
