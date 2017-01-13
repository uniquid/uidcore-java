package com.uniquid.uniquid_core.connector.mqtt.user;

import java.io.InputStream;

import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;
import com.uniquid.uniquid_core.user.UserResponse;

public class MQTTMessageResponse implements UserResponse {

	private JSONMessage jsonMessage;
	
	public MQTTMessageResponse(JSONMessage jsonMessage) {
		this.jsonMessage = jsonMessage;
	}
	
	@Override
	public String getParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getContent() {
		// TODO Auto-generated method stub
		return null;
	}

}
