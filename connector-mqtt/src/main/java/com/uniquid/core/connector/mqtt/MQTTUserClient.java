package com.uniquid.core.connector.mqtt;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;
import com.uniquid.core.connector.ConnectorException;
import com.uniquid.core.connector.UserClient;

public class MQTTUserClient implements UserClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTUserClient.class);
	
	private String broker;
	private int timeoutInSeconds;
	private String destination;
	
	public MQTTUserClient(String broker, String destination, int timeoutInSeconds) {
		this.broker = broker;
		this.destination = destination;
		this.timeoutInSeconds = timeoutInSeconds;
	}

	@Override
	public ProviderResponse sendOutputMessage(ProviderRequest providerRequest) throws ConnectorException {
		
		RPCProviderRequest request = (RPCProviderRequest) providerRequest;
		
		BlockingConnection connection = null;
		
		try {
			MQTT mqtt = new MQTT();
			
			mqtt.setHost(broker);
			
			connection = mqtt.blockingConnection();
			connection.connect();
			
			String destinationTopic = destination;
			
			String sender = providerRequest.getSender();
			
			// to subscribe
			Topic[] topics = { new Topic(sender, QoS.AT_LEAST_ONCE) };
			/*byte[] qoses = */connection.subscribe(topics);

			// consume
			connection.publish(destinationTopic, request.toJSONString().getBytes(), QoS.AT_LEAST_ONCE, false);
			
			Message message = connection.receive(timeoutInSeconds, TimeUnit.SECONDS);
			
			if (message == null) {

				throw new TimeoutException();

			}

			byte[] payload = message.getPayload();

			//
			message.ack();

			// Create a JSON Message
			return RPCProviderResponse.fromJSONString(new String(payload));
			
		} catch (Throwable t) {
			
			throw new ConnectorException("Exception", t);
			
		} finally {
			
			// disconnect
			try {

				connection.disconnect();

			} catch (Exception ex) {

				LOGGER.error("Catched Exception", ex);

			}

		} 
	
	}

}
