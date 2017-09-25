package com.uniquid.core.connector.mqtt;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
 * Implementation of a {@link Connector} that uses the MQTT protocol.
 */
public class MQTTConnector implements Connector {

	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTConnector.class);

	private String providerTopic;
	private String broker;
	private Queue<byte[]> inputQueue;

	private ScheduledExecutorService receiverExecutorService;

	/**
	 * Creates a MQTTConnector that listen on the specified receiving topic and on the specified broker.  
	 * @param topic the topic to listen to
	 * @param broker the MQTT broker to use 
	 */
	private MQTTConnector(final String topic, final String broker) {

		this.providerTopic = topic;
		this.broker = broker;
		this.inputQueue = new LinkedList<byte[]>();
		
	}

	/**
	 * Builder for {@link MQTTConnector}
	 */
	public static class Builder {
		private String _topic;
		private String _broker;

		/**
		 * Set the listening topic
		 * @param _topic the topic to listen to
		 * @return the Builder
		 */
		public Builder set_topic(String _topic) {
			this._topic = _topic;
			return this;
		}

		/**
		 * Set the broker to use
		 * @param _broker the broker to use
		 * @return the Builder
		 */
		public Builder set_broker(String _broker) {
			this._broker = _broker;
			return this;
		}

		/**
		 * Returns an instance of a {@link MQTTConnector}
		 * @return an instance of a {@link MQTTConnector}
		 */
		public MQTTConnector build() {

			return new MQTTConnector(_topic, _broker);
		}

	}

	@Override
	public EndPoint accept() throws ConnectorException, InterruptedException {

		try {

			synchronized (inputQueue) {

				while (inputQueue.isEmpty()) {

					LOGGER.trace("inputQueue is empty. waiting");

					inputQueue.wait();

				}

				LOGGER.trace("inputQueue not empty! fetching element");
				
				byte[] inputMessage = inputQueue.poll();

				LOGGER.trace("returning MQTTEndPoint");
				
				return new MQTTEndPoint(inputMessage, broker);

			}

		} catch (InterruptedException ex) {
			
			LOGGER.error("Catched InterruptedException", ex);
			
			throw ex;
			
		} catch (Exception ex) {
			
			LOGGER.error("Catched Exception", ex);

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
						
						LOGGER.info("Starting MQTTConnector");

						BlockingConnection connection = null;
	
						try {
	
							MQTT mqtt = new MQTT();
	
							mqtt.setHost(broker);
							
							LOGGER.info("Connecting to MQTT");
	
							connection = mqtt.blockingConnection();
							connection.connect();
	
							// subscribe
							Topic[] topics = { new Topic(providerTopic, QoS.AT_LEAST_ONCE) };
							/*byte[] qoses = */connection.subscribe(topics);
	
							LOGGER.info("Waiting for a message!");
							
							// blocks!!!
							Message message = connection.receive();
							
							LOGGER.info("Message received!");
							
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
								
								LOGGER.info("Disconnecting");
	
								connection.disconnect();
	
							} catch (Exception ex) {
	
								LOGGER.error("Catched Exception", ex);
	
							}
	
						}
					
					} catch (InterruptedException ex) {
						
						LOGGER.info("Received interrupt request. Exiting");
						
						return;
						
					} catch (Throwable t) {
						
						LOGGER.error("Catched Exception", t);
						
					}

				}

			}

		};
		
		LOGGER.info("Starting receiving");

		// Start receiver
		receiverExecutorService.execute(receiver);

	}

	@Override
	public void stop() {
		
		LOGGER.info("Stopping MQTTConnector");

		receiverExecutorService.shutdownNow();

	}

}
