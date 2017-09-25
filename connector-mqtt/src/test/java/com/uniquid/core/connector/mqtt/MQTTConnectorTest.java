package com.uniquid.core.connector.mqtt;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.junit.Assert;
import org.junit.Test;

import com.uniquid.core.connector.EndPoint;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.serializers.JSONMessageSerializer;

public class MQTTConnectorTest {

	@Test
	public void testEmptyBuild() {
		String topic = "topic";
		String broker = "broker";
		
		MQTTConnector mqttConnector = new MQTTConnector.Builder()
				.set_broker(broker)
				.set_topic(topic)
				.build();
		Assert.assertNotNull(mqttConnector);
		
	}
	
	@Test
	public void testFlow() {
		
		String broker = "tcp://appliance4.uniquid.co:1883";
		String topic = "test";
		final MQTTConnector mqttConnector = new MQTTConnector.Builder()
			.set_broker(broker)
			.set_topic(topic)
			.build();
		
		Assert.assertNotNull(mqttConnector);
		
		mqttConnector.start();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try{
					Thread.sleep(3000);
				} catch (Throwable t) {
					System.out.println("sleep exception");
				}
				startMQTTClientMock();
			}
		}).start();
		
		
		try {
			EndPoint endPoint = mqttConnector.accept();
			
			Assert.assertEquals("hola!", ((FunctionRequestMessage)endPoint.getInputMessage()).getParameters());
		} catch (Exception e) {
			Assert.fail();
		}
		
	}
	
	private void startMQTTClientMock() {
		String broker = "tcp://appliance4.uniquid.co:1883";
		String topic = "test";
		
		FunctionRequestMessage functionRequestMessage = new FunctionRequestMessage();
		functionRequestMessage.setUser("sender");
		functionRequestMessage.setFunction(33);
		functionRequestMessage.setParameters("hola!");
		
		String sender = "sender";
		Topic[] topics = {new Topic(sender, QoS.AT_LEAST_ONCE)};
		BlockingConnection connection = null;
		
		try{
			MQTT mqtt = new MQTT();
			mqtt.setHost(broker);
			connection = mqtt.blockingConnection();
			connection.connect();
			connection.subscribe(topics);
			connection.publish(topic, new JSONMessageSerializer().serialize(functionRequestMessage), QoS.AT_LEAST_ONCE, false);
			connection.disconnect();
		} catch (Throwable t) {
			Assert.fail();
		}
				
	}
	
	@Test
	public void testAcceptException() throws Exception {
		String broker = "tcp://appliance4.uniquid.co:1883";
		String topic = "test";
		final MQTTConnector mqttConnector = new MQTTConnector.Builder()
			.set_broker(broker)
			.set_topic(topic)
			.build();
		
		Assert.assertNotNull(mqttConnector);
		
		mqttConnector.start();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try{
					Thread.sleep(3000);
				} catch (Throwable t) {
					System.out.println("sleep exception");
				}
				startMQTTClientMockException();
			}
		}).start();
			
		EndPoint endPoint = mqttConnector.accept();
		Assert.assertEquals("hola!", ((FunctionRequestMessage)endPoint.getInputMessage()).getParameters());
		
	}
	
	private void startMQTTClientMockException() {
		String broker = "tcp://appliance4.uniquid.co:1883";
		String topic = "test";
		
		FunctionRequestMessage functionRequestMessage = new FunctionRequestMessage();
		functionRequestMessage.setUser("sender");
		functionRequestMessage.setFunction(33);
		functionRequestMessage.setParameters("hola!");
		
		String sender = "sender";
		Topic[] topics = {new Topic(sender, QoS.AT_LEAST_ONCE)};
		BlockingConnection connection = null;
		
		try{
			MQTT mqtt = new MQTT();
			mqtt.setHost(broker);
			connection = mqtt.blockingConnection();
			connection.connect();
			connection.subscribe(topics);
			connection.publish(topic, new JSONMessageSerializer().serialize(functionRequestMessage), QoS.AT_LEAST_ONCE, false);
			connection.disconnect();
		} catch (Throwable t) {
			Assert.fail();
		}
				
	}
	
}