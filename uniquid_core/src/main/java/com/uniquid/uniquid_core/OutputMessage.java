package com.uniquid.uniquid_core;

public interface OutputMessage<T> {
	
	public void setParameter(String name, Object value);
	
	public String getDestination();
	
	public void setDestination(String destination);
	
	public T getContent();

}
