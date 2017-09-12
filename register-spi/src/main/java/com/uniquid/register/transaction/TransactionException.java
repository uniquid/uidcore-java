package com.uniquid.register.transaction;

import com.uniquid.register.exception.RegisterException;

public class TransactionException extends RegisterException {

	private static final long serialVersionUID = 1L;
	
	public TransactionException(String message) {
		super(message);
	}
	
	public TransactionException(String message, Throwable t) {
		super(message, t);
	}

}
