package com.uniquid.uniquid_core.connector.mqtt;

import com.uniquid.uniquid_core.connector.EndPoint;
import com.uniquid.uniquid_core.function.FunctionRequest;
import com.uniquid.uniquid_core.function.FunctionResponse;

public class MQTTEndPoint implements EndPoint {
	
	private MQTTConnector mqttConnector;
	private MQTTMessageRequest mqttMessageRequest;
	private MQTTMessageResponse mqttMessageResponse;
	
	public MQTTEndPoint(MQTTConnector mqttConnector, MQTTMessageRequest mqttMessageRequest, MQTTMessageResponse mqttMessageResponse) {
		
		this.mqttConnector = mqttConnector;
		this.mqttMessageRequest = mqttMessageRequest;
		this.mqttMessageResponse = mqttMessageResponse;
	}

	@Override
	public FunctionRequest getFunctionRequest() {
		return mqttMessageRequest;
	}

	@Override
	public FunctionResponse getFunctionResponse() {
		return mqttMessageResponse;
	}

	@Override
	public void close() {
		
		JSONMessageResponse jsonMessageResponse = new JSONMessageResponse();
		
		Integer id = (Integer) mqttMessageRequest.getJSONMessage().getBody().get("id");
		
		jsonMessageResponse.setSender("sender");
		jsonMessageResponse.setResult(mqttMessageResponse.getOutputString());
		jsonMessageResponse.setError(mqttMessageResponse.getStatus());
		jsonMessageResponse.setId(id);
		
		mqttConnector.sendResponse(jsonMessageResponse);
	}

}
