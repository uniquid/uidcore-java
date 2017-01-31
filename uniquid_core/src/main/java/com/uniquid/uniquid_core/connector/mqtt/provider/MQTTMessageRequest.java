package com.uniquid.uniquid_core.connector.mqtt.provider;

import com.uniquid.uniquid_core.InputMessage;
import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;
import com.uniquid.uniquid_core.provider.FunctionInputStream;

public class MQTTMessageRequest implements InputMessage<JSONMessage> {
	
	private JSONMessage jsonMessage;
	
	public MQTTMessageRequest(JSONMessage jsonMessage) {
		this.jsonMessage = jsonMessage;
	}

	@Override
	public String getParameter(String name) {
		
		if (InputMessage.SENDER.equals(name)) {
			return jsonMessage.getSender();
		} else if (InputMessage.RPC_METHOD.equals(name)) {
			return String.valueOf(jsonMessage.getBody().get("method"));
		} else if (InputMessage.PARAMS.equals(name)) {
			return (String) jsonMessage.getBody().get("params");
		} else if (InputMessage.ID.equals(name)) {
			return String.valueOf(jsonMessage.getBody().get("id"));
		}
		
		return null;
	}

	@Override
	public FunctionInputStream getInputStream() {
		throw new UnsupportedOperationException("Method not yet implemented");
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
	
	public static MQTTMessageRequest fromJSONMessage(JSONMessage jsonMessage) throws Exception {
		
		// check for valid request message
		if (jsonMessage.getSender() != null &&
				jsonMessage.getBody().get("method") != null &&
						jsonMessage.getBody().get("id") != null &&
								jsonMessage.getBody().get("params") != null) {
			return new MQTTMessageRequest(jsonMessage);
		}

		throw new Exception("Received invalid message: " + jsonMessage);
		
	}
	
	@Override
	public JSONMessage getPayload() {
		return jsonMessage;
	}

}
