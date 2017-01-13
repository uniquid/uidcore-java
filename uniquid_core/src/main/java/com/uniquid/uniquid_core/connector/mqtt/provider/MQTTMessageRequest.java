package com.uniquid.uniquid_core.connector.mqtt.provider;

import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;
import com.uniquid.uniquid_core.provider.FunctionInputStream;
import com.uniquid.uniquid_core.provider.ProviderRequest;

public class MQTTMessageRequest implements ProviderRequest {
	
	private JSONMessage jsonMessage;
	
	public MQTTMessageRequest(JSONMessage jsonMessage) {
		this.jsonMessage = jsonMessage;
	}

	@Override
	public String getParameter(String name) {
		
		if (ProviderRequest.SENDER.equals(name)) {
			return jsonMessage.getSender();
		} else if (ProviderRequest.METHOD.equals(name)) {
			return String.valueOf(jsonMessage.getBody().get("method"));
		} else if (ProviderRequest.PARAMS.equals(name)) {
			return (String) jsonMessage.getBody().get("params");
		} else if (ProviderRequest.ID.equals(name)) {
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
	
	public static MQTTMessageRequest createEmptyMQTTMessageRequest() throws Exception {
		
		return fromJSONString("{\"sender\":\"\",\"body\": {\"method\":0, \"params\":\"{}\",\"id\":0}}");
	}

	@Override
	public Object getContent() {
		return getJSONMessage();
	}


}
