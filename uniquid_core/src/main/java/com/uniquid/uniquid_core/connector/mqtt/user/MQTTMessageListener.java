package com.uniquid.uniquid_core.connector.mqtt.user;

import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;
import com.uniquid.uniquid_core.user.UserResponse;

public interface MQTTMessageListener {
	
	public void receive(JSONMessage message);
	
	public int getId();

	public UserResponse waitForResponse(long timeout);

}
