package com.uniquid.uniquid_core.message.impl;

import java.util.Map;

import com.uniquid.uniquid_core.message.MessageService;
import com.uniquid.uniquid_core.message.MessageServiceFactory;

public class MQTTMessageServiceFactory extends MessageServiceFactory {
	
	private static final String PREFIX = "MQTTMessageServiceFactory";
	public static final String TOPIC = PREFIX + ".topic";
	public static final String BROKER = PREFIX + ".broker";
	
	private static MQTTMessageService INSTANCE;

	public MQTTMessageServiceFactory(Map<String, Object> configuration) {
		super(configuration);
	}

	@Override
	public MessageService createMessageService() throws Exception {
		
		if (INSTANCE == null) {
			
			String broker = (String) factoryConfiguration.get(BROKER);
			String topic = (String) factoryConfiguration.get(TOPIC);
			
			INSTANCE = new MQTTMessageService.Builder().
					set_topic(topic).
					set_broker(broker).build();
			
		}
		
		return INSTANCE;
		
	}

}
