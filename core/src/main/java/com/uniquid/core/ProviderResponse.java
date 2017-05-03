package com.uniquid.core;

/**
 * This interface represents a response RPC message
 *
 * @param <T>
 */
public interface ProviderResponse {
	
	/**
	 * Code used when everything is ok
	 */
	public static final int RESULT_OK = 0;
	
	/**
	 * Error code used to signal the the user doesn't have permission to execute the requested function.
	 */
	public static final int RESULT_NO_PERMISSION = 2;
	
	/**
	 * Error code used to signal that the user asked to execute a function that is not available in the environment.
	 */
	public static final int RESULT_FUNCTION_NOT_AVAILABLE = 3;
	
	/**
	 * Error code used to signal a generic error.
	 */
	public static final int RESULT_ERROR = 4;
	
	/**
	 * Returns the sender of the response.
	 * 
	 * @return the sender of the response.
	 */
	public String getSender();

	/**
	 * Set the sender of the response.
	 * 
	 * @param sender the sender of the response.
	 */
	public void setSender(final String sender);
	
	/**
	 * Returns the result of the previously executed function if no error was raised.
	 * 
	 * @return a String representing the executed function if no error was raised.
	 */
	public String getResult();
	
	/**
	 * Set the result of the previously executed function.
	 * 
	 * @param result the String representing the previously executed function.
	 */
	public void setResult(final String result);
	
	/**
	 * Return the error code of the previously executed function.
	 * 
	 * @return the int representing the error code of the previously executed function.
	 */
	public int getError();

	/**
	 * Set the error code of the previously execute function.
	 * 
	 * @param error the int representing the error code of the previously executed function.
	 */
	public void setError(final int error);
	
}
