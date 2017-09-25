package com.uniquid.core.connector.mqtt;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.junit.Assert;
import org.junit.Test;

import com.uniquid.core.connector.ConnectorException;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.serializers.JSONMessageSerializer;

public class MQTTEndPointTest {
	
	@Test
	public void testConstructor() throws Exception {
		String broker = "tcp://appliance4.uniquid.co:1883";
		
		FunctionRequestMessage functionRequestMessage = new FunctionRequestMessage();
		functionRequestMessage.setUser("sender");
		functionRequestMessage.setFunction(33);
		functionRequestMessage.setParameters("hola!");
		
		byte[] mqttMessageRequest = new JSONMessageSerializer().serialize(functionRequestMessage);
		
		try {
			MQTTEndPoint mqttEndPoint = new MQTTEndPoint(mqttMessageRequest, broker);
			
			FunctionRequestMessage endpointRequest = (FunctionRequestMessage) mqttEndPoint.getInputMessage();
			
			Assert.assertEquals(functionRequestMessage, endpointRequest);
			Assert.assertNotNull(mqttEndPoint.getOutputMessage());
			
		} catch (ConnectorException e) {
			Assert.fail();
		}
				
	}
	
	@Test
	public void testFlush() throws Exception {
		
		final String broker = "tcp://appliance4.uniquid.co:1883";
		
		final FunctionRequestMessage functionRequestMessage = new FunctionRequestMessage();
		functionRequestMessage.setUser("sender");
		functionRequestMessage.setFunction(33);
		functionRequestMessage.setParameters("hola!");
		functionRequestMessage.setId(123456);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				startMqttClientMock(functionRequestMessage.getId());
			}
		}).start();
		
		final byte[] mqttMessageRequest = new JSONMessageSerializer().serialize(functionRequestMessage);
		
		try {
			MQTTEndPoint mqttEndPoint = new MQTTEndPoint(mqttMessageRequest, broker);
			mqttEndPoint.getOutputMessage().setError(0);
			mqttEndPoint.getOutputMessage().setProvider("sender");
			mqttEndPoint.getOutputMessage().setResult("result");
			mqttEndPoint.flush();
		} catch (ConnectorException e) {
			Assert.fail();
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
			
			Assert.assertNotNull(payload);
						
			FunctionRequestMessage functionRequestMessage = (FunctionRequestMessage) new JSONMessageSerializer().deserialize(payload);
			
			Assert.assertNotNull(functionRequestMessage);
			Assert.assertEquals(id, functionRequestMessage.getId());
			
			connection.disconnect();
			
		} catch (Throwable t) {
			Assert.fail();
		}
	}
	
	@Test(expected = ConnectorException.class)
	public void testFlushException() throws Exception {
		final String broker = "tcp://appliance4.uniquid.co:1883";
		
		final FunctionRequestMessage functionRequestMessage = new FunctionRequestMessage();
		functionRequestMessage.setUser("sender");
		functionRequestMessage.setFunction(33);
		functionRequestMessage.setParameters("hola!");
		functionRequestMessage.setId(123456);
		
		final byte[] mqttMessageRequest = new JSONMessageSerializer().serialize(functionRequestMessage);
		
		MQTTEndPoint mqttEndPoint = new MQTTEndPoint(mqttMessageRequest, null);
		mqttEndPoint.getOutputMessage().setError(0);
		mqttEndPoint.getOutputMessage().setProvider("sender");
		mqttEndPoint.getOutputMessage().setResult("result");
		mqttEndPoint.flush();
		
	}

}
