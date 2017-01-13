package com.uniquid.uniquid_core.connector.mqtt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.uniquid_core.InputMessage;
import com.uniquid.uniquid_core.OutputMessage;
import com.uniquid.uniquid_core.connector.Connector;
import com.uniquid.uniquid_core.connector.ConnectorException;
import com.uniquid.uniquid_core.connector.EndPoint;
import com.uniquid.uniquid_core.connector.mqtt.provider.MQTTMessageRequest;
import com.uniquid.uniquid_core.connector.mqtt.provider.MQTTMessageResponse;
import com.uniquid.uniquid_core.connector.mqtt.user.MQTTMessageListener;
import com.uniquid.uniquid_core.connector.mqtt.user.MQTTMessageListenerImpl;

/**
 * This class implements a Connector that uses the MQTT protocol
 * 
 * @author giuseppe
 *
 */
public class MQTTConnector implements Connector {

	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTConnector.class.getName());

	private String topic;
	private String broker;
	private Map<Integer, MQTTMessageListener> userListners;
	private Queue<JSONMessage> providerQueue;
	private Queue<OutputMessage> outputQueue;
	private Thread sendThread, receiveThread;

	private MQTTConnector(String topic, String broker) {

		this.topic = topic;
		this.broker = broker;
		this.userListners = new HashMap<Integer, MQTTMessageListener>();
		this.providerQueue = new LinkedList<JSONMessage>();
		this.outputQueue = new LinkedList<OutputMessage>();
		
		
		this.sendThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					sendForever();
				} catch (Throwable t) {
					LOGGER.error("Catched throwable", t);
				}
			}
		});
		
		this.receiveThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					receiveForever();
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
			synchronized (providerQueue) {

				while (providerQueue.isEmpty()) {

					providerQueue.wait();

				}

				JSONMessage jsonMessage = providerQueue.poll();

				MQTTMessageRequest mqttMessageRequest = MQTTMessageRequest.fromJSONMessage(jsonMessage);
				
				MQTTMessageResponse mqttMessageResponse = new MQTTMessageResponse();

				EndPoint endPoint = new MQTTEndPoint(this, mqttMessageRequest, mqttMessageResponse);

				return endPoint;

			}
		} catch (Exception ex) {
			throw new ConnectorException(ex);
		}

	}

	private void receiveForever() throws ConnectorException, InterruptedException {
		// the receive a message.
		// if there is a listner, then extract the id from the received message
		// and pass that to the listner
		// otherwise it is a message to the provider

		while (!Thread.interrupted()) {

			BlockingConnection connection = null;

			try {

				MQTT mqtt = new MQTT();

				mqtt.setHost(broker);

				connection = mqtt.blockingConnection();
				connection.connect();

				// subscribe
				Topic[] topics = { new Topic(topic, QoS.AT_LEAST_ONCE) };
				byte[] qoses = connection.subscribe(topics);

				// blocks!!!
				Message message = connection.receive();

				byte[] payload = message.getPayload();

				//
				message.ack();

				// Create a JSON Message
				JSONMessage jsonMessage = JSONMessage.fromJsonString(new String(payload));

				// If there is a user waiting for a response
				if (!userListners.isEmpty()) {

					// then fetch the id from the jsonMessage
					Integer id = (Integer) jsonMessage.getBody().get("id");

					MQTTMessageListener userListner = userListners.get(id);

					if (userListner != null) {

						userListner.receive(jsonMessage);

					}
				} else {

					// no user waiting for this message! It's a Provider message
					synchronized (providerQueue) {

						providerQueue.add(jsonMessage);
						providerQueue.notifyAll();

					}

				}

				// DONE!

			} catch (Exception ex) {

				LOGGER.error("Catched Exception", ex);

				throw new ConnectorException("Catched exception", ex);

			} finally {

				// disconnect
				try {

					connection.disconnect();

				} catch (Exception ex) {

					LOGGER.error("Catched Exception", ex);

					throw new ConnectorException("Catched exception", ex);

				}

			}

		}
	}

	private void sendForever() throws ConnectorException, InterruptedException {
		// the client send expects a response.
		// this means that a listner will be register and wait for a response

		while (!Thread.interrupted()) {

			synchronized (outputQueue) {

				while (outputQueue.isEmpty()) {

					outputQueue.wait();

				}
				
				OutputMessage outputMessage = outputQueue.poll();
				Object content = outputMessage.getContent();

				BlockingConnection connection = null;

				try {

					MQTT mqtt = new MQTT();

					mqtt.setHost(broker);

					connection = mqtt.blockingConnection();
					connection.connect();

					// to subscribe
					Topic[] topics = { new Topic(outputMessage.getDestination(), QoS.AT_LEAST_ONCE) };
					byte[] qoses = connection.subscribe(topics);

					// consume
					connection.publish(outputMessage.getDestination(), ((JSONMessage) content).toJSON().getBytes(), QoS.AT_LEAST_ONCE, false);

				} catch (Exception ex) {

					LOGGER.error("Catched Exception", ex);

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
	}

	public void sendResponse(OutputMessage messageResponse) {

		synchronized (outputQueue) {

			outputQueue.add(messageResponse);
			outputQueue.notifyAll();

		}

	}

	@Override
	public OutputMessage<?> createOutputMessage() throws ConnectorException {
		return new com.uniquid.uniquid_core.connector.mqtt.user.MQTTMessageRequest();
	}

	/*
	 * This method should send the message via MQTT and then wait for a
	 * response.
	 */
	@Override
	public InputMessage sendOutputMessage(OutputMessage<?> outputMessage, long timeout) {

		// add output message to queue
		JSONMessage jsonMessage = (JSONMessage) outputMessage.getContent();

		// retrieve id
		int id = (Integer) jsonMessage.getBody().get("id");

		// register listner fo receiving response
		MQTTMessageListener mqttMessageListener = new MQTTMessageListenerImpl(id);
		
		synchronized (outputQueue) {

			outputQueue.add(outputMessage);
			outputQueue.notifyAll();

		}

		// wait
		return mqttMessageListener.waitForResponse(timeout);
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
