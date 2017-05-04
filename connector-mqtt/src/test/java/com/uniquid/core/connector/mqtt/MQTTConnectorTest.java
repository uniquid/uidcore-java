package com.uniquid.core.connector.mqtt;

import org.junit.Assert;
import org.junit.Test;

public class MQTTConnectorTest {

	@Test
	public void testBuild() {
		String topic = "topic";
		String broker = "broker";
		
		MQTTConnector mqttConnector = new MQTTConnector.Builder().build();
		Assert.assertNotNull(mqttConnector);
		
	}
}
