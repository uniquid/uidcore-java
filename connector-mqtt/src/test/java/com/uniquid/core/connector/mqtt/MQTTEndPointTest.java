package com.uniquid.core.connector.mqtt;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.junit.Assert;
import org.junit.Test;

import com.uniquid.core.connector.ConnectorException;

public class MQTTEndPointTest {
	
	@Test
	public void constructorTest() {
		String broker = "tcp://appliance4.uniquid.co:1883";
		
		RPCProviderRequest rpcProviderRequest = new RPCProviderRequest
				.Builder()
				.set_sender("sender")
				.set_rpcMethod(33)
				.set_params("hola!")
				.build();
		
		String request = rpcProviderRequest.toJSONString();
		byte[] mqttMessageRequest = request.getBytes();
		
		try {
			MQTTEndPoint mqttEndPoint = new MQTTEndPoint(mqttMessageRequest, broker);
			RPCProviderRequest endpointRequest = (RPCProviderRequest) mqttEndPoint.getInputMessage();
			Assert.assertEquals(rpcProviderRequest.getSender(), endpointRequest.getSender());
			Assert.assertEquals(rpcProviderRequest.getParams(), endpointRequest.getParams());
			Assert.assertEquals(rpcProviderRequest.getFunction(), endpointRequest.getFunction());
			Assert.assertEquals(rpcProviderRequest.getId(), endpointRequest.getId());
			Assert.assertNotNull(mqttEndPoint.getOutputMessage());
			
		} catch (ConnectorException e) {
			e.printStackTrace();
		}
				
	}
	
	@Test
	public void flushTest() {
		
		final String broker = "tcp://appliance4.uniquid.co:1883";
		
		final RPCProviderRequest rpcProviderRequest = new RPCProviderRequest
				.Builder()
				.set_sender("sender")
				.set_rpcMethod(33)
				.set_params("hola!")
				.build();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				startMqttClientMock(rpcProviderRequest.getId());
			}
		}).start();
		
		String request = rpcProviderRequest.toJSONString();
		System.out.println(request);
		
		final byte[] mqttMessageRequest = request.getBytes();
		
		try {
			MQTTEndPoint mqttEndPoint = new MQTTEndPoint(mqttMessageRequest, broker);
			mqttEndPoint.getOutputMessage().setError(0);
			mqttEndPoint.getOutputMessage().setSender("sender");
			mqttEndPoint.getOutputMessage().setResult("result");
			mqttEndPoint.flush();
		} catch (ConnectorException e) {
			e.printStackTrace();
		}
		
	}
	
	private void startMqttClientMock(long id) {
				
		String broker = "tcp://appliance4.uniquid.co:1883";
		String sender = "sender";
		Topic[] topics = {new Topic(sender, QoS.AT_LEAST_ONCE)};
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

			//
			message.ack();
			
			Assert.assertNotNull(message);
						
			String response = new String(payload);
			RPCProviderResponse rpcProviderResponse = RPCProviderResponse.fromJSONString(response);
			Assert.assertNotNull(rpcProviderResponse);
			Assert.assertEquals(id, rpcProviderResponse.getId());
			
			connection.disconnect();
			
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
