package com.uniquid.uniquid_core.connector.mqtt.user;

import com.uniquid.uniquid_core.InputMessage;

public interface MQTTMessageListener<T> {
	
	public void receive(T message);
	
	public int getId();

	public InputMessage<T> waitForResponse(long timeout);

}
