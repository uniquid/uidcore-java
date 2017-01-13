package com.uniquid.uniquid_core.connector.mqtt;

import com.uniquid.uniquid_core.connector.EndPoint;
import com.uniquid.uniquid_core.connector.mqtt.provider.MQTTMessageRequest;
import com.uniquid.uniquid_core.connector.mqtt.provider.MQTTMessageResponse;
import com.uniquid.uniquid_core.provider.ProviderRequest;
import com.uniquid.uniquid_core.provider.ProviderResponse;

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
	public ProviderRequest getProviderRequest() {
		return mqttMessageRequest;
	}

	@Override
	public ProviderResponse getProviderResponse() {
		return mqttMessageResponse;
	}

	@Override
	public void close() {
		
		mqttConnector.sendResponse(mqttMessageResponse);
	}

}
