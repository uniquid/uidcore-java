package com.uniquid.core.connector.mqtt;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.core.InputMessage;
import com.uniquid.core.OutputMessage;
import com.uniquid.core.connector.Connector;
import com.uniquid.core.connector.ConnectorException;
import com.uniquid.core.connector.EndPoint;
import com.uniquid.core.connector.mqtt.provider.MQTTMessageRequest;
import com.uniquid.core.connector.mqtt.provider.MQTTMessageResponse;

/**
 * This class implements a Connector that uses the MQTT protocol
 * 
 * @author giuseppe
 *
 */
public class MQTTConnector implements Connector<JSONMessage> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTConnector.class.getName());

	private String providerTopic;
	private String broker;
	private Queue<JSONMessage> inputQueue;
	private Queue<OutputMessage<JSONMessage>> outputQueue;
	private Thread sendThread, receiveThread;

	private MQTTConnector(String topic, String broker) {

		this.providerTopic = topic;
		this.broker = broker;
		this.inputQueue = new LinkedList<JSONMessage>();
		this.outputQueue = new LinkedList<OutputMessage<JSONMessage>>();
		
		
		this.sendThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					
					while (!Thread.interrupted()) {
					
						sendProviderMessage();
					
					}
				} catch (Throwable t) {
					LOGGER.error("Catched throwable", t);
				}
			}
		});
		
		this.receiveThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					
					while (!Thread.interrupted()) {
					
						receiveProviderMessage();
					
					}
				} catch (Throwable t) {
					LOGGER.error("Catched throwable", t);
				}
			}
		});

	}

	public static class Builder {
		private String _topic;
		private String _broker;

		public Builder set_topic(String _topic) {
			this._topic = _topic;
			return this;
		}

		public Builder set_broker(String _broker) {
			this._broker = _broker;
			return this;
		}

		public MQTTConnector build() {

			return new MQTTConnector(_topic, _broker);
		}

	}

	@Override
	public EndPoint accept() throws ConnectorException {

		try {

			synchronized (inputQueue) {

				while (inputQueue.isEmpty()) {

					inputQueue.wait();

				}

				JSONMessage jsonMessage = inputQueue.poll();

				MQTTMessageRequest mqttMessageRequest = MQTTMessageRequest.fromJSONMessage(jsonMessage);
				
				MQTTMessageResponse mqttMessageResponse = new MQTTMessageResponse();

				EndPoint endPoint = new MQTTEndPoint(this, mqttMessageRequest, mqttMessageResponse);

				return endPoint;

			}

		} catch (Exception ex) {

			throw new ConnectorException(ex);

		}

	}

	private void receiveProviderMessage() throws ConnectorException, InterruptedException {

		try {

			// Create a JSON Message
			JSONMessage jsonMessage = receiveMessage(broker, providerTopic);

			synchronized (inputQueue) {

				inputQueue.add(jsonMessage);
				inputQueue.notifyAll();

			}

			// DONE!

		} catch (Throwable t) {

			LOGGER.error("Error", t);

//				throw new ConnectorException("Catched exception", t);

		}

	}
	
	private JSONMessage receiveMessage(String broker, String topic) throws Exception {
		
		BlockingConnection connection = null;

		try {

			MQTT mqtt = new MQTT();

			mqtt.setHost(broker);

			connection = mqtt.blockingConnection();
			connection.connect();

			// subscribe
			Topic[] topics = { new Topic(topic, QoS.AT_LEAST_ONCE) };
			/*byte[] qoses = */connection.subscribe(topics);

			// blocks!!!
			Message message = connection.receive();

			byte[] payload = message.getPayload();

			//
			message.ack();

			// Create a JSON Message
			return JSONMessage.fromJsonString(new String(payload));

			// DONE!

		} finally {

			// disconnect
			try {

				connection.disconnect();

			} catch (Exception ex) {

				LOGGER.error("Catched Exception", ex);

			}

		}
		
	}
	
	private JSONMessage receiveMessage(String broker, String topic, long timeoutInSeconds) throws Exception, TimeoutException {
		
		BlockingConnection connection = null;

		try {

			MQTT mqtt = new MQTT();

			mqtt.setHost(broker);

			connection = mqtt.blockingConnection();
			connection.connect();

			// subscribe
			Topic[] topics = { new Topic(topic, QoS.AT_LEAST_ONCE) };
			/*byte[] qoses = */connection.subscribe(topics);

			// blocks!!!
			Message message = connection.receive(timeoutInSeconds, TimeUnit.SECONDS);
			
			if (message == null) {

				throw new TimeoutException();

			}

			byte[] payload = message.getPayload();

			//
			message.ack();

			// Create a JSON Message
			return JSONMessage.fromJsonString(new String(payload));

			// DONE!

		} finally {

			// disconnect
			try {

				connection.disconnect();

			} catch (Exception ex) {

				LOGGER.error("Catched Exception", ex);

			}

		}
		
	}

	private void sendProviderMessage() throws ConnectorException, InterruptedException {

		synchronized (outputQueue) {

			while (outputQueue.isEmpty()) {

				outputQueue.wait();

			}
			
			OutputMessage<JSONMessage> outputMessage = outputQueue.poll();

			try {

				sendMessage(broker, outputMessage);

			} catch (Throwable t) {

				LOGGER.error("Error", t);
				
				// We should not loose this message!!! We put it again in the queue
				
				outputQueue.add(outputMessage);
				
				outputQueue.notifyAll();

			}

		}

	}
	
	private void sendMessage(String broker, OutputMessage<JSONMessage> outputMessage) throws Exception {
		
		BlockingConnection connection = null;

		try {

			MQTT mqtt = new MQTT();

			mqtt.setHost(broker);

			connection = mqtt.blockingConnection();
			connection.connect();
			
			String destinationTopic = (String) outputMessage.getParameter(OutputMessage.RECEIVER_ADDRESS);

			// to subscribe
			Topic[] topics = { new Topic(destinationTopic, QoS.AT_LEAST_ONCE) };
			/*byte[] qoses = */connection.subscribe(topics);

			// consume
			connection.publish(destinationTopic, outputMessage.getPayload().toJSONString().getBytes(), QoS.AT_LEAST_ONCE, false);

		} finally {

			// disconnect
			try {

				connection.disconnect();

			} catch (Exception ex) {

				LOGGER.error("Catched Exception", ex);

			}

		}
		
	}
	
	private JSONMessage sendReceiveMessage(String broker, OutputMessage<JSONMessage> outputMessage, long timeoutInSeconds) throws Exception {
		
		BlockingConnection connection = null;

		try {

			MQTT mqtt = new MQTT();

			mqtt.setHost(broker);

			connection = mqtt.blockingConnection();
			connection.connect();
			
			String destinationTopic = (String) outputMessage.getParameter(OutputMessage.RECEIVER_ADDRESS);
			
			String sender = (String) outputMessage.getParameter(OutputMessage.SENDER);

			// to subscribe
			Topic[] topics = { new Topic(sender, QoS.AT_LEAST_ONCE) };
			/*byte[] qoses = */connection.subscribe(topics);

			// consume
			connection.publish(destinationTopic, outputMessage.getPayload().toJSONString().getBytes(), QoS.AT_LEAST_ONCE, false);
			
			Message message = connection.receive(timeoutInSeconds, TimeUnit.SECONDS);
			
			if (message == null) {

				throw new TimeoutException();

			}

			byte[] payload = message.getPayload();

			//
			message.ack();

			// Create a JSON Message
			return JSONMessage.fromJsonString(new String(payload));

		} finally {

			// disconnect
			try {

				connection.disconnect();

			} catch (Exception ex) {

				LOGGER.error("Catched Exception", ex);

			}

		}
		
	}
	

	public void sendResponse(OutputMessage<JSONMessage> messageResponse) {

		synchronized (outputQueue) {

			outputQueue.add(messageResponse);
			outputQueue.notifyAll();

		}

	}

	@Override
	public OutputMessage<JSONMessage> createOutputMessage() throws ConnectorException {
		return new com.uniquid.core.connector.mqtt.user.MQTTMessageRequest();
	}

	/*
	 * This method should send the message via MQTT and then wait for a
	 * response.
	 */
	@Override
	public InputMessage<JSONMessage> sendOutputMessage(OutputMessage<JSONMessage> outputMessage, long timeout) throws ConnectorException {

		try {
			
			JSONMessage jsonMessage = sendReceiveMessage(broker, outputMessage, timeout);
			
			return new com.uniquid.core.connector.mqtt.user.MQTTMessageResponse(jsonMessage);
			
		} catch (TimeoutException ex) {
			
			throw new ConnectorException("Timeout while sendOutputMessage", ex);

		} catch (Exception ex) {
			
			throw new ConnectorException("Exception while sendOutputMessage", ex);
		}
			
	}

	@Override
	public void start() {
		
		sendThread.start();
		
		receiveThread.start();
		
	}

	@Override
	public void stop() {
		
		sendThread.interrupt();
		receiveThread.interrupt();
		
	}

}
