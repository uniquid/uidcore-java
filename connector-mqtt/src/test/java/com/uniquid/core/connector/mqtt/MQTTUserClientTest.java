package com.uniquid.core.connector.mqtt;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.junit.Assert;
import org.junit.Test;

import com.uniquid.core.ProviderRequest;
import com.uniquid.core.connector.ConnectorException;

public class MQTTUserClientTest {

	@Test
	public void constructorTest() {
		String broker = "tcp://appliance4.uniquid.co:1883"; 
		String destination = "test";
		int timeout = 10;
		
		MQTTUserClient mqttUserClient = new MQTTUserClient(broker, destination, timeout);
		Assert.assertNotNull(mqttUserClient);
	}
	
	@Test
	public void sendOutputMessageTest() {
		String sender = "sender";
		int method = 5;
		String params = "params";
		
		final ProviderRequest providerRequest = new RPCProviderRequest.Builder()
				.set_sender(sender)
				.set_rpcMethod(method)
				.set_params(params)
				.build();
		
		String broker = "tcp://appliance4.uniquid.co:1883"; 
		String destination = "test";
		int timeout = 20;
		
		final MQTTUserClient mqttUserClient = new MQTTUserClient(broker, destination, timeout);
		Assert.assertNotNull(mqttUserClient);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {				
				startMqttServerMock();				
			}
		}).start();

		try {
			mqttUserClient.sendOutputMessage(providerRequest);
		} catch (ConnectorException e) {
			System.out.println(e.getMessage());
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
						
			String request = new String(payload);
			RPCProviderRequest rpcProviderRequest = RPCProviderRequest.fromJSONString(request);
			Assert.assertNotNull(rpcProviderRequest);
			
			RPCProviderResponse rpcProviderResponse = new RPCProviderResponse.Builder().buildFromId(rpcProviderRequest.getId());
			rpcProviderResponse.setSender("sender");
			rpcProviderResponse.setResult("result");
			rpcProviderResponse.setError(0);
			String response = rpcProviderResponse.toJSONString();
			connection.publish(rpcProviderRequest.getSender(), response.getBytes(), QoS.AT_LEAST_ONCE, false);
			
			connection.disconnect();			
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
