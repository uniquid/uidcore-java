package com.uniquid.uniquid_core.connector.mqtt;

import java.util.Map;

import com.uniquid.uniquid_core.connector.Connector;
import com.uniquid.uniquid_core.connector.ConnectorFactory;

public class MQTTConnectorFactory extends ConnectorFactory {
	
	private static final String PREFIX = "MQTTConnectorFactory";
	public static final String TOPIC = PREFIX + ".topic";
	public static final String BROKER = PREFIX + ".broker";
	
	private static MQTTConnector INSTANCE;

	public MQTTConnectorFactory(Map<String, Object> configuration) {
		super(configuration);
	}

	@Override
	public Connector createConnector() throws Exception {
		
		if (INSTANCE == null) {
			
			String broker = (String) factoryConfiguration.get(BROKER);
			String topic = (String) factoryConfiguration.get(TOPIC);
			
			INSTANCE = new MQTTConnector.Builder().
					set_topic(topic).
					set_broker(broker).build();
			
		}
		
		return INSTANCE;
		
	}

}
