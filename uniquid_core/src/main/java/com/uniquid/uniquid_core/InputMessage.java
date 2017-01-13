package com.uniquid.uniquid_core;

public interface InputMessage<T> {
	
	/**
	 ** Returns the value of a request parameter as a <code>String</code>, or
	 * <code>null</code> if the parameter does not exist.
	 */
	public String getParameter(String name);
	
	public T getContent();

}
