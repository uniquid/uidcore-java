package com.uniquid.core;

/**
 * Represents a message request coming from an User and directed to a Provider 
 */
public interface UserRequest {
	
	/**
	 * Returns the user (sender) of the request.
	 * 
	 * @return the user of the request.
	 */
	public String getUser();
	
	/**
	 * Returns the provider (recipient) of the request.
	 * 
	 * @return the provider of the request.
	 */
	public String getProvider();
	
	/**
	 * Returns the integer representing the function to execute.
	 * 
	 * @return the integer representing the function to execute.
	 */
	public int getFunction();
	
	/**
	 * Returns the parameters of the function as a string.
	 * 
	 * @return a String representing the parameters of the function.
	 */
	public String getParameters();
	
}
