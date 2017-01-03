package com.uniquid.uniquid_core;

public interface Connector {

	public void start();
	
	public void stop();
	
	public boolean isRunning();
	
	String getName();
	
	void open();
	
	void close();
	
	void getServer();
	
	void setServer();
	
	void setPort();
	
	void getPort();
	
	void setHost();
	
	void getHost();
}
