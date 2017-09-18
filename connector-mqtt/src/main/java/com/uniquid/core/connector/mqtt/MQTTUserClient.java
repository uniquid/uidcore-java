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
import com.uniquid.core.user.UserClient;
import com.uniquid.core.user.UserRequest;
import com.uniquid.core.user.UserResponse;

/**
 * Implementation of {@link UserClient} that uses MQTT protocol
 */
public class MQTTUserClient implements UserClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTUserClient.class);
	
	private String broker;
	private int timeoutInSeconds;
	private String destination;
	
	/**
	 * Creates an instance from broker, destination topic and timeout
	 * @param broker the broker to use
	 * @param destinationTopic the topic that will receive the message
	 * @param timeoutInSeconds the timeout in seconds to wait for a response
	 */
	public MQTTUserClient(final String broker, final String destinationTopic, final int timeoutInSeconds) {
		this.broker = broker;
		this.destination = destinationTopic;
		this.timeoutInSeconds = timeoutInSeconds;
	}

	@Override
	public UserResponse execute(final UserRequest userRequest) throws ConnectorException {
		
		LOGGER.info("Sending output message to {}", destination);
		
		byte[] payload;
		
		if (userRequest instanceof RPCProviderRequest) {
			
			payload = ((RPCProviderRequest) userRequest).toJSONString().getBytes();
			
		} else if (userRequest instanceof AnnouncerProviderRequest) {
			
			payload = ((AnnouncerProviderRequest) userRequest).toJSONString().getBytes();
			
		} else {
			
			LOGGER.error("unexpected ProviderRequest instance!");
			
			throw new ConnectorException("unexpected ProviderRequest instance!");
			
		}
		
		BlockingConnection connection = null;
		
		try {
			final MQTT mqtt = new MQTT();
			
			mqtt.setHost(broker);
			
			connection = mqtt.blockingConnection();
			connection.connect();
			
			final String destinationTopic = destination;
			
			final String sender = userRequest.getUser();
			
			// to subscribe
			final Topic[] topics = { new Topic(sender, QoS.AT_LEAST_ONCE) };
			/*byte[] qoses = */connection.subscribe(topics);

			// consume
			connection.publish(destinationTopic, payload, QoS.AT_LEAST_ONCE, false);
			
			final Message message = connection.receive(timeoutInSeconds, TimeUnit.SECONDS);
			
			if (message == null) {

				throw new TimeoutException();

			}

			payload = message.getPayload();

			message.ack();

			// Create a JSON Message
			return RPCProviderResponse.fromJSONString(new String(payload));
			
		} catch (Throwable t) {
			
			LOGGER.error("Exception", t);
			
			throw new ConnectorException("Exception", t);
			
		} finally {
			
			// disconnect
			try {

				if (connection != null) {
				
					LOGGER.info("Disconnecting");
					
					connection.disconnect();
					
				}

			} catch (Exception ex) {

				LOGGER.error("Catched Exception", ex);

			}

		} 
	
	}

}
