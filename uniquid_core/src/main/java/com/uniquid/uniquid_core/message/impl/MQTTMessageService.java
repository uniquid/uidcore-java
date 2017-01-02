package com.uniquid.uniquid_core.message.impl;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.uniquid_core.message.MessageRequest;
import com.uniquid.uniquid_core.message.MessageResponse;
import com.uniquid.uniquid_core.message.MessageService;

public class MQTTMessageService extends MessageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTMessageService.class.getName());

	private String topic;
	private String broker;

	private MQTTMessageService(String topic, String broker) {

		this.topic = topic;
		this.broker = broker;

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
		
		public MQTTMessageService build() {
			
			return new MQTTMessageService(_topic, _broker);
		}
		
	}

	@Override
	public MessageRequest receiveRequest() {

		BlockingConnection connection = null;
		

		try {

			MQTT mqtt = new MQTT();

			mqtt.setHost(broker);

			connection = mqtt.blockingConnection();
			connection.connect();

			// subscribe
			Topic[] topics = { new Topic(topic, QoS.AT_LEAST_ONCE) };
			byte[] qoses = connection.subscribe(topics);

			// consume
			Message message = connection.receive();

			byte[] payload = message.getPayload();

			MessageRequest messageRequest = new MessageRequest();
			
			parseReqMessage(payload, messageRequest);
			
			// process the message then
			message.ack();
			
			return messageRequest;
			
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

		return null;

	}

	@Override
	public void sendResponse(MessageResponse messageResponse) {
		
		BlockingConnection connection = null;

		try {

			MQTT mqtt = new MQTT();

			mqtt.setHost(broker);

			connection = mqtt.blockingConnection();
			connection.connect();

			// to subscribe
			Topic[] topics = { new Topic(topic, QoS.AT_LEAST_ONCE) };
			byte[] qoses = connection.subscribe(topics);

			// consume
			connection.publish(topic, messageResponse.createRegistryMsgResponse().getBytes(), QoS.AT_LEAST_ONCE, false);
			
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
