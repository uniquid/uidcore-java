package com.uniquid.core;

/**
 * Represents a message response coming from a Provider and directed to an User 
 */
public interface UserResponse {
	
	/**
	 * Integer code used when everything is ok
	 */
	public static final int RESULT_OK = 0;
	
	/**
	 * Integer code used to signal the the user doesn't have permission to execute the requested function.
	 */
	public static final int RESULT_NO_PERMISSION = 2;
	
	/**
	 * Integer code used to signal that the user asked to execute a function that is not available in the environment.
	 */
	public static final int RESULT_FUNCTION_NOT_AVAILABLE = 3;
	
	/**
	 * Integer code used to signal a generic error.
	 */
	public static final int RESULT_ERROR = 4;
	
	/**
	 * Returns the provider (sender) of the request.
	 * 
	 * @return the provider of the request.
	 */
	public String getProvider();
	
	/**
	 * Returns the user (recipient) of the request.
	 * 
	 * @return the user of the request.
	 */
	public String getUser();

	/**
	 * Returns the result of the previously executed function if no error was raised.
	 * 
	 * @return a String representing the executed function if no error was raised.
	 */
	public String getResult();
	
	/**
	 * Return the error code of the previously executed function.
	 * 
	 * @return the int representing the error code of the previously executed function.
	 */
	public int getError();

}
