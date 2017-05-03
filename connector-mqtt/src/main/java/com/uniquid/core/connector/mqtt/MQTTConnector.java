package com.uniquid.core.connector.mqtt;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.core.connector.Connector;
import com.uniquid.core.connector.ConnectorException;
import com.uniquid.core.connector.EndPoint;

/**
 * This class implements a Connector that uses the MQTT protocol
 * 
 * @author giuseppe
 *
 */
public class MQTTConnector implements Connector {

	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTConnector.class.getName());

	private String providerTopic;
	private String broker;
	private Queue<byte[]> inputQueue;

	private ScheduledExecutorService receiverExecutorService;

	private MQTTConnector(String topic, String broker) {

		this.providerTopic = topic;
		this.broker = broker;
		this.inputQueue = new LinkedList<byte[]>();
		
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

				byte[] inputMessage = inputQueue.poll();

				return new MQTTEndPoint(inputMessage, broker);

			}

		} catch (Exception ex) {

			throw new ConnectorException(ex);

		}

	}

	@Override
	public void start() {

		receiverExecutorService = Executors.newSingleThreadScheduledExecutor();

		final Runnable receiver = new Runnable() {

			@Override
			public void run() {

				while (!Thread.currentThread().isInterrupted()) {
					
					try {

						BlockingConnection connection = null;
	
						try {
	
							MQTT mqtt = new MQTT();
	
							mqtt.setHost(broker);
	
							connection = mqtt.blockingConnection();
							connection.connect();
	
							// subscribe
							Topic[] topics = { new Topic(providerTopic, QoS.AT_LEAST_ONCE) };
							/*byte[] qoses = */connection.subscribe(topics);
	
							// blocks!!!
							Message message = connection.receive();
							
							byte[] payload = message.getPayload();
	
							//
							message.ack();
	
							// Create a JSON Message
							synchronized (inputQueue) {
	
								inputQueue.add(payload);
								inputQueue.notifyAll();
	
							}
	
							// DONE!
	
						} finally {
	
							// disconnect
							try {
	
								connection.disconnect();
	
							} catch (Exception ex) {
	
								LOGGER.error("Catched Exception", ex);
	
							}
	
						}
					
					} catch (Throwable t) {
						
						LOGGER.error("Catched Exception", t);
						
					}

				}

			}

		};

		// Start receiver
		receiverExecutorService.execute(receiver);

	}

	@Override
	public void stop() {

		receiverExecutorService.shutdown();

		try {

			receiverExecutorService.awaitTermination(5, TimeUnit.SECONDS);

		} catch (InterruptedException e) {

			LOGGER.error("Exception while awaiting for termination", e);

		}
		
	}

}
