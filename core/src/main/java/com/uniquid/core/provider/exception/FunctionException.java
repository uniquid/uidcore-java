package com.uniquid.core.provider.exception;

public class FunctionException extends Exception {

	private static final long serialVersionUID = 1L;

	public FunctionException() {
        super();
    }

    public FunctionException(String message) {
        super(message);
    }

    public FunctionException(String message, Throwable cause) {
        super(message, cause);
    }
}
