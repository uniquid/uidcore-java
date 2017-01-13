package com.uniquid.uniquid_core.connector.mqtt.user;

import com.uniquid.uniquid_core.InputMessage;
import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;

public interface MQTTMessageListener {
	
	public void receive(JSONMessage message);
	
	public int getId();

	public InputMessage waitForResponse(long timeout);

}
