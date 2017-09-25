package com.uniquid.core.connector.mqtt;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.core.connector.ConnectorException;
import com.uniquid.core.connector.EndPoint;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.messages.MessageSerializer;
import com.uniquid.messages.MessageType;
import com.uniquid.messages.UniquidMessage;
import com.uniquid.messages.serializers.JSONMessageSerializer;

/**
 * Implementation of a {@link EndPoint} used by {@link MQTTConnector}
 */
public class MQTTEndPoint implements EndPoint {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTEndPoint.class);
	
	private String broker;
	
	private final FunctionRequestMessage providerRequest;
	private final FunctionResponseMessage providerResponse;
	
	private MessageSerializer messageSerializer = new JSONMessageSerializer();
	
	/**
	 * Creates a new instance from the byte array message and broker
	 * @param mqttMessageRequest the message received
	 * @param broker the broker to use
	 * @throws ConnectorException in case a problem occurs.
	 */
	MQTTEndPoint(final byte[] mqttMessageRequest, final String broker) throws ConnectorException {
		
		this.broker = broker;

		try {
			
			UniquidMessage messageReceived = messageSerializer.deserialize(mqttMessageRequest);
			
			if (MessageType.FUNCTION_REQUEST.equals(messageReceived.getMessageType())) {
				
				// Retrieve message
				providerRequest = (FunctionRequestMessage) messageReceived;
						
				providerResponse = new FunctionResponseMessage();
				providerResponse.setId(providerRequest.getId());
				
			} else {
			
				throw new Exception("Received an invalid message type " + messageReceived.getMessageType());
			
			}
		
		} catch (Exception ex) {
			
			throw new ConnectorException("Exception during creation of endpoint", ex);
		
		}
	}

	@Override
	public FunctionRequestMessage getInputMessage() {
		return providerRequest;
	}

	@Override
	public FunctionResponseMessage getOutputMessage() {
		return providerResponse;
	}

	@Override
	public void flush() throws ConnectorException {
		
		LOGGER.info("Sending response");
		
		BlockingConnection connection = null;

		try {

			MQTT mqtt = new MQTT();

			mqtt.setHost(broker);

			connection = mqtt.blockingConnection();
			connection.connect();
			
			String destinationTopic = providerRequest.getUser(); 
			
			// to subscribe
			Topic[] topics = { new Topic(destinationTopic, QoS.AT_LEAST_ONCE) };
			connection.subscribe(topics);

			// consume
			connection.publish(destinationTopic, messageSerializer.serialize(providerResponse), QoS.AT_LEAST_ONCE, false);

		} catch (Exception ex) {
			
			LOGGER.error("Catched Exception", ex);
			
			throw new ConnectorException(ex);
			
		} finally {

			// disconnect
			try {
				
				LOGGER.info("Disconnecting");

				connection.disconnect();

			} catch (Exception ex) {

				LOGGER.error("Catched Exception", ex);

			}

		}

	}

}