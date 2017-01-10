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
	
	public static MQTTMessageRequest fromJSONString(String jsonString) {
		JSONMessage message = JSONMessage.fromJsonString(jsonString);
		
		return new MQTTMessageRequest(message);
	}

}
