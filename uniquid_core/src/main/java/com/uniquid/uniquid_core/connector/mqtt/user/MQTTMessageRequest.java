package com.uniquid.uniquid_core.connector.mqtt.user;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;
import com.uniquid.uniquid_core.user.UserRequest;

public class MQTTMessageRequest implements UserRequest {
	
	private JSONMessage jsonMessage;
	private String destination;
	
	public MQTTMessageRequest() {
		this.jsonMessage = new JSONMessage();
		jsonMessage.getBody().put("id", 1234);
	}

	@Override
	public void setParameter(String name, Object value) {
		
		if (UserRequest.SENDER.equals(name)) {
			jsonMessage.setSender((String) value);
		} else if (UserRequest.METHOD.equals(name)) {
			jsonMessage.getBody().put("method", value);
		} else if (UserRequest.PARAMS.equals(name)) {
			jsonMessage.getBody().put("params", value);
		} else if (UserRequest.ID.equals(name)) {
			jsonMessage.getBody().put("id", value);
		}
		
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return null;
	}
	
	public JSONMessage getJSONMessage() {
		return jsonMessage;
	}

	@Override
	public String getDestination() {
		return destination;
	}

	@Override
	public Object getContent() {
		return jsonMessage;
	}

	@Override
	public void setDestination(String destination) {
		this.destination = destination;
		
	}

}
