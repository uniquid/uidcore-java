package com.uniquid.node.exception;

/**
 * A NodeException represents an Exception related to a problem of the Uniquid Node
 * 
 * @author Giuseppe Magnotta
 *
 */
public class NodeException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new exception with null as its detail message
	 */
	public NodeException() {
        super();
    }

	/**
	 * Constructs a new exception with the specified detail message
	 * 
     * @param message the detail message (which is saved for later retrieval by the getMessage() 
	 method).
	 */
    public NodeException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with null as its detail message
     * @param message the detail message (which is saved for later retrieval by the getMessage() 
	 method).
     * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null 
	 value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public NodeException(String message, Throwable cause) {
        super(message, cause);
    }

}
