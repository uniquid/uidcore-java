package com.uniquid.register.transaction;

public class TransactionException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public TransactionException(String message) {
		super(message);
	}
	
	public TransactionException(String message, Throwable t) {
		super(message, t);
	}

}
