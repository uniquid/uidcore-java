package com.uniquid.core.connector.mqtt;

import com.uniquid.core.InputMessage;
import com.uniquid.core.OutputMessage;
import com.uniquid.core.connector.EndPoint;
import com.uniquid.core.connector.mqtt.provider.MQTTMessageRequest;
import com.uniquid.core.connector.mqtt.provider.MQTTMessageResponse;

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
	public InputMessage getInputMessage() {
		return mqttMessageRequest;
	}

	@Override
	public OutputMessage getOutputMessage() {
		return mqttMessageResponse;
	}

	@Override
	public void close() {
		
		JSONMessage jsonResponse = mqttMessageResponse.getJSONResponse();
		
		jsonResponse.getBody().put("result", mqttMessageResponse.getOutputString());
		
		mqttConnector.sendResponse(mqttMessageResponse);
	}

}
