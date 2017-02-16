package com.uniquid.core.connector.mqtt.user;

import java.io.InputStream;

import com.uniquid.core.InputMessage;
import com.uniquid.core.connector.mqtt.JSONMessage;

public class MQTTMessageResponse implements InputMessage<JSONMessage> {

	private JSONMessage jsonMessage;
	
	public MQTTMessageResponse(JSONMessage jsonMessage) {
		this.jsonMessage = jsonMessage;
	}
	
	@Override
	public String getParameter(String name) {
		if (InputMessage.SENDER.equals(name)) {
			return jsonMessage.getSender();
		} else if (InputMessage.RESULT.equals(name)) {
			return String.valueOf(jsonMessage.getBody().get("result"));
		} else if (InputMessage.ERROR.equals(name)) {
			return String.valueOf(jsonMessage.getBody().get("error"));
		} else if (InputMessage.ID.equals(name)) {
			return String.valueOf(jsonMessage.getBody().get("id"));
		}
		
		return null;
	}

	@Override
	public InputStream getInputStream() {
		throw new UnsupportedOperationException("Method not yet implemented");
	}

	@Override
	public JSONMessage getPayload() {
		return jsonMessage;
	}

}
