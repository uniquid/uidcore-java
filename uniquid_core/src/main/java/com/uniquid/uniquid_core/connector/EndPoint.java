package com.uniquid.uniquid_core.connector;

import com.uniquid.uniquid_core.InputMessage;
import com.uniquid.uniquid_core.OutputMessage;

/**
 * Represent a communication between a Provider and a User
 * 
 * @author giuseppe
 *
 */
public interface EndPoint {
	
	/**
	 * Returns the request performed by the User
	 * @return
	 */
	public InputMessage getInputMessage();

	/**
	 * Returns the response to be returned to the User
	 * @return
	 */
	public OutputMessage getOutputMessage();
	
	/**
	 * Close this EndPoint sending all the communication to the User.
	 */
	public void close();

}
