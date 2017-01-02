package com.uniquid.uniquid_core;

import com.uniquid.uniquid_core.message.MessageRequest;

public interface Function {
	
	/**
	 * Returns the value of this funciton
	 * 
	 * @return
	 */
	public int getValue();
	
	/**
	 * Returns a descriptive name of this function
	 * @return
	 */
	public String getName();
	
	/**
	 * Request to execute the function
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void performFunction(MessageRequest request, StringBuilder response) throws Exception;
	
}
