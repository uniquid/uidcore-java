package com.uniquid.uniquid_core.connector.impl;

import java.util.Map;

import com.uniquid.uniquid_core.connector.ConnectorService;
import com.uniquid.uniquid_core.connector.ConnectorServiceFactory;

public class MQTTConnectorServiceFactory extends ConnectorServiceFactory {
	
	private static final String PREFIX = "MQTTMessageServiceFactory";
	public static final String TOPIC = PREFIX + ".topic";
	public static final String BROKER = PREFIX + ".broker";
	
	private static MQTTConnectorService INSTANCE;

	public MQTTConnectorServiceFactory(Map<String, Object> configuration) {
		super(configuration);
	}

	@Override
	public ConnectorService createConnectorService() throws Exception {
		
		if (INSTANCE == null) {
			
			String broker = (String) factoryConfiguration.get(BROKER);
			String topic = (String) factoryConfiguration.get(TOPIC);
			
			INSTANCE = new MQTTConnectorService.Builder().
					set_topic(topic).
					set_broker(broker).build();
			
		}
		
		return INSTANCE;
		
	}

}
