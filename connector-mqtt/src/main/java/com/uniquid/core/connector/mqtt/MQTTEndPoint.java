package com.uniquid.core.connector.mqtt;

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
import com.uniquid.userclient.UserClientException;
import com.uniquid.userclient.impl.MQTTUserClient;

/**
 * Implementation of a {@link EndPoint} used by {@link MQTTConnector}
 */
public class MQTTEndPoint implements EndPoint {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTEndPoint.class);
	private static final int DEFAULT_TIMEOUT = 60;
	
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
		
		MQTTUserClient mqttUserClient = new MQTTUserClient(broker, providerRequest.getUser(), DEFAULT_TIMEOUT, providerResponse.getProvider());
		
		try {
			
			mqttUserClient.send(providerResponse);
			
		} catch (UserClientException e) {
			
			throw new ConnectorException("Exception", e);
			
		}

	}

}