package com.uniquid.core.messages;

/**
 * A Message is a data structure that can be serialized/deserialized
 */
public interface UniquidMessage {
	
	/**
	 * Return the {@link MessageType} representation
	 * 
	 * @return
	 */
	public MessageType getMessageType();

}
