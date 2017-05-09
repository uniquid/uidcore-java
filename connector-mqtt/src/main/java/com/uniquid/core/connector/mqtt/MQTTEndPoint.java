package com.uniquid.core.connector.mqtt;

import java.nio.charset.StandardCharsets;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;
import com.uniquid.core.connector.ConnectorException;
import com.uniquid.core.connector.EndPoint;

public class MQTTEndPoint implements EndPoint {
	
private String broker;
	
	private final RPCProviderRequest rpcProviderRequest;
	private final RPCProviderResponse rpcProviderResponse;
	
	public MQTTEndPoint(final byte[] mqttMessageRequest, final String broker) throws ConnectorException {
		
		this.broker = broker;

		final String inputstring = new String(mqttMessageRequest, StandardCharsets.UTF_8); // Or any encoding.
		
		try {
			// Retrieve message
			rpcProviderRequest = RPCProviderRequest.fromJSONString(inputstring);
			
			rpcProviderResponse = new RPCProviderResponse.Builder().buildFromId(rpcProviderRequest.getId());
		
		} catch (Exception ex) {
			
			throw new ConnectorException("Exception during creation of endpoint", ex);
		
		}
	}

	@Override
	public ProviderRequest getInputMessage() {
		return rpcProviderRequest;
	}

	@Override
	public ProviderResponse getOutputMessage() {
		return rpcProviderResponse;
	}

	@Override
	public void flush() throws ConnectorException {
		
		BlockingConnection connection = null;

			try {
	
				MQTT mqtt = new MQTT();
	
				mqtt.setHost(broker);
	
				connection = mqtt.blockingConnection();
				connection.connect();
				
				String destinationTopic = rpcProviderRequest.getSender(); 
				
				// to subscribe
				Topic[] topics = { new Topic(destinationTopic, QoS.AT_LEAST_ONCE) };
				connection.subscribe(topics);
	
				// consume
				connection.publish(destinationTopic, rpcProviderResponse.toJSONString().getBytes(), QoS.AT_LEAST_ONCE, false);
	
			} catch (Exception ex) {
				
				throw new ConnectorException(ex);
				
			} finally {
	
				// disconnect
				try {
	
					connection.disconnect();
	
				} catch (Exception ex) {
	
					//LOGGER.error("Catched Exception", ex);
	
				}
	
			}

	}

}