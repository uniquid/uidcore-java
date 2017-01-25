package com.uniquid.uniquid_core.connector.mqtt.user;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.uniquid.uniquid_core.OutputMessage;
import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;

public class MQTTMessageRequest implements OutputMessage<JSONMessage> {
	
	private JSONMessage jsonMessage;
	private Map<String, Object> parameters;
	
	public MQTTMessageRequest() {
		this.jsonMessage = new JSONMessage();
		this.parameters = new HashMap<String, Object>();
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
		} else {
			parameters.put(name, value);
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
	public JSONMessage getContent() {
		return jsonMessage;
	}

	@Override
	public Object getParameter(String name) {
		if (OutputMessage.SENDER.equals(name)) {
			return jsonMessage.getSender();
		} else if (OutputMessage.METHOD.equals(name)) {
			return jsonMessage.getBody().get("method");
		} else if (OutputMessage.PARAMS.equals(name)) {
			return jsonMessage.getBody().get("params");
		} else if (OutputMessage.ID.equals(name)) {
			return jsonMessage.getBody().get("id");
		} else {
			return parameters.get(name);
		}
		
	}

}
