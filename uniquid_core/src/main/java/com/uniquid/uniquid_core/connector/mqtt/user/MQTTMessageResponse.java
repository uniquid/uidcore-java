package com.uniquid.uniquid_core.connector.mqtt.user;

import java.io.InputStream;

import com.uniquid.uniquid_core.InputMessage;
import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;

public class MQTTMessageResponse implements InputMessage<JSONMessage> {

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
	public JSONMessage getContent() {
		return jsonMessage;
	}

}
