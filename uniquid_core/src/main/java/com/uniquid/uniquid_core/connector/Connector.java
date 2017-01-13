package com.uniquid.uniquid_core.connector;

import com.uniquid.uniquid_core.InputMessage;
import com.uniquid.uniquid_core.OutputMessage;

/**
 * Interface of Connector
 */
public interface Connector {

	public void start();
	
	public void stop();
	
	public abstract EndPoint accept() throws ConnectorException;
	
	public OutputMessage<?> createOutputMessage() throws ConnectorException;
	
	public InputMessage<?> sendOutputMessage(OutputMessage<?> outputMessage, long timeout);
	
}
