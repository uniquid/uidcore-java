package com.uniquid.uniquid_core.connector.mqtt;

import com.uniquid.uniquid_core.function.FunctionInputStream;
import com.uniquid.uniquid_core.function.FunctionRequest;

public class MQTTMessageRequest implements FunctionRequest {
	
	private JSONMessage jsonMessage;
	
	public MQTTMessageRequest(JSONMessage jsonMessage) {
		this.jsonMessage = jsonMessage;
	}

	@Override
	public String getParameter(String name) {
		
		if (FunctionRequest.SENDER.equals(name)) {
			return jsonMessage.getSender();
		} else if (FunctionRequest.METHOD.equals(name)) {
			return String.valueOf(jsonMessage.getBody().get("method"));
		} else if (FunctionRequest.PARAMS.equals(name)) {
			return (String) jsonMessage.getBody().get("params");
		} else if (FunctionRequest.ID.equals(name)) {
			return String.valueOf(jsonMessage.getBody().get("id"));
		}
		
		return null;
	}

	@Override
	public FunctionInputStream getInputStream() {
		throw new UnsupportedOperationException("Method not yet implemented");
	}
	
	public JSONMessage getJSONMessage() {
		return jsonMessage;
	}
	
	public static MQTTMessageRequest fromJSONString(String jsonString) throws Exception {
		JSONMessage message = JSONMessage.fromJsonString(jsonString);
		
		// check for valid request message
		if (message.getSender() != null &&
				message.getBody().get("method") != null &&
				message.getBody().get("id") != null &&
				message.getBody().get("params") != null) {
			return new MQTTMessageRequest(message);
		}

		throw new Exception("Received invalid message: " + jsonString);
		
	}

}
