package com.uniquid.core.client;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.junit.Assert;
import org.junit.Test;

import com.uniquid.core.connector.ConnectorException;
import com.uniquid.core.messages.FunctionRequestMessage;
import com.uniquid.core.messages.FunctionResponseMessage;
import com.uniquid.core.user.impl.mqtt.MQTTMessageSerializer;
import com.uniquid.core.user.impl.mqtt.MQTTUserClient;

public class MQTTUserClientTest {

	@Test
	public void testConstructor() {
		String broker = "tcp://appliance4.uniquid.co:1883"; 
		String destination = "test";
		int timeout = 10;
		
		MQTTUserClient mqttUserClient = new MQTTUserClient(broker, destination, timeout, "test2");
		Assert.assertNotNull(mqttUserClient);
	}
	
	@Test
	public void testSendOutputMessage() {
		String sender = "sender";
		int method = 5;
		String params = "params";
		
		FunctionRequestMessage providerRequest = new FunctionRequestMessage();
		providerRequest.setUser(sender);
		providerRequest.setMethod(method);
		providerRequest.setParameters(params);
		
		String broker = "tcp://appliance4.uniquid.co:1883"; 
		String destination = "test";
		int timeout = 10;
		
		final MQTTUserClient mqttUserClient = new MQTTUserClient(broker, destination, timeout, sender);
		Assert.assertNotNull(mqttUserClient);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {				
				startMqttServerMock();				
			}
		}).start();

		try {
			FunctionResponseMessage providerResponse = (FunctionResponseMessage) mqttUserClient.execute(providerRequest);
			Assert.assertNotNull(providerResponse);
			
			Assert.assertEquals(providerResponse.getId(), providerRequest.getId());
		} catch (ConnectorException e) {
			Assert.fail();
		}				

	}
	
	private void startMqttServerMock() {
		
		String broker = "tcp://appliance4.uniquid.co:1883";
		String topic = "test";
		Topic[] topics = {new Topic(topic, QoS.AT_LEAST_ONCE)};
		BlockingConnection connection = null;
		
		try{
			MQTT mqtt = new MQTT();
			mqtt.setHost(broker);
			connection = mqtt.blockingConnection();
			connection.connect();
			connection.subscribe(topics);
			// blocks!!!
			Message message = connection.receive();
			
			byte[] payload = message.getPayload();

			message.ack();
			
			Assert.assertNotNull(message);

			FunctionRequestMessage rpcProviderRequest = (FunctionRequestMessage) new MQTTMessageSerializer().deserialize(payload);
			Assert.assertNotNull(rpcProviderRequest);
			
			FunctionResponseMessage rpcProviderResponse = new FunctionResponseMessage();
			rpcProviderResponse.setProvider("sender");
			rpcProviderResponse.setError(0);
			rpcProviderResponse.setResult("result");
			rpcProviderResponse.setId(rpcProviderRequest.getId());
			
			connection.publish(rpcProviderRequest.getUser(), new MQTTMessageSerializer().serialize(rpcProviderResponse), QoS.AT_LEAST_ONCE, false);
			
			connection.disconnect();			
			
		} catch (Throwable t) {
			Assert.fail();
		}
	}
	
	@Test(expected = ConnectorException.class)
	public void testSendOutputMessageException() throws ConnectorException{
		String sender = "sender";
		int method = 5;
		String params = "params";
		
		FunctionRequestMessage providerRequest = new FunctionRequestMessage();
		providerRequest.setUser(sender);
		providerRequest.setMethod(method);
		providerRequest.setParameters(params);
		providerRequest.setId(1234);
		
		String broker = "tcp://appliance4.uniquid.co:1883"; 
		String destination = "test";
		int timeout = 10;
		
		final MQTTUserClient mqttUserClient = new MQTTUserClient(broker, destination, timeout, sender);
		Assert.assertNotNull(mqttUserClient);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {				
				startMqttServerMockException();				
			}
		}).start();

		FunctionRequestMessage providerResponse = (FunctionRequestMessage) mqttUserClient.execute(providerRequest);
		Assert.assertNotNull(providerResponse);
		
		Assert.assertEquals(providerRequest.getId(), providerResponse.getId());
	
	}
	
private void startMqttServerMockException() {
		
		String broker = "tcp://appliance4.uniquid.co:1883";
		String topic = "test";
		Topic[] topics = {new Topic(topic, QoS.AT_LEAST_ONCE)};
		BlockingConnection connection = null;
		
		try{
			MQTT mqtt = new MQTT();
			mqtt.setHost(broker);
			connection = mqtt.blockingConnection();
			connection.connect();
			connection.subscribe(topics);
			// blocks!!!
			Message message = connection.receive();
			
			byte[] payload = message.getPayload();

			message.ack();
			
			Assert.assertNotNull(message);
						
			FunctionRequestMessage functionRequestMessage = (FunctionRequestMessage) new MQTTMessageSerializer().deserialize(payload);
			Assert.assertNotNull(functionRequestMessage);

			FunctionResponseMessage functionResponseMessage = new FunctionResponseMessage();
			functionResponseMessage.setProvider("sender");
			functionResponseMessage.setResult("result");
			functionResponseMessage.setError(0);
			
			connection.disconnect();			
			
		} catch (Throwable t) {
			Assert.fail();
		}
	}
	
}
