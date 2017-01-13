package com.uniquid.uniquid_core.connector.mqtt.user;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.uniquid.uniquid_core.OutputMessage;
import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;

public class MQTTMessageRequest implements OutputMessage<JSONMessage> {
	
	private JSONMessage jsonMessage;
	private String destination;
	
	public MQTTMessageRequest() {
		this.jsonMessage = new JSONMessage();
	}

	@Override
	public void setParameter(String name, Object value) {
		
		if (OutputMessage.SENDER.equals(name)) {
			jsonMessage.setSender((String) value);
		} else if (OutputMessage.METHOD.equals(name)) {
			jsonMessage.getBody().put("method", value);
		} else if (OutputMessage.PARAMS.equals(name)) {
			jsonMessage.getBody().put("params", value);
		} else if (OutputMessage.ID.equals(name)) {
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
	public JSONMessage getContent() {
		return jsonMessage;
	}

	@Override
	public void setDestination(String destination) {
		this.destination = destination;
	}

}
