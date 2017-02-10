package com.uniquid.node.exception;

public class NodeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NodeException() {
        super();
    }

    public NodeException(String message) {
        super(message);
    }

    public NodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeException(Throwable cause) {
        super(cause);
    }
	
}
