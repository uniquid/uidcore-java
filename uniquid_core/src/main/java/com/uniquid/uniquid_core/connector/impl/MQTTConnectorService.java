package com.uniquid.uniquid_core.connector.impl;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.uniquid_core.connector.JSONMessageRequest;
import com.uniquid.uniquid_core.connector.JSONMessageResponse;
import com.uniquid.uniquid_core.function.FunctionResponse;
import com.uniquid.uniquid_core.connector.ConnectorService;

public class MQTTConnectorService extends ConnectorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTConnectorService.class.getName());

	private String topic;
	private String broker;

	private MQTTConnectorService(String topic, String broker) {

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
		
		public MQTTConnectorService build() {
			
			return new MQTTConnectorService(_topic, _broker);
		}
		
	}

	@Override
	public MQTTFunctionRequest receiveRequest() {

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

			JSONMessageRequest messageRequest = JSONMessageRequest.fromJSONString(new String(payload));
			
//			parseReqMessage(payload, messageRequest);
			
			// process the message then
			message.ack();
			
			return new MQTTFunctionRequest();
			
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
	public void sendResponse(FunctionResponse messageResponse) {
		
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
			//connection.publish(topic, messageResponse.toJSONString().getBytes(), QoS.AT_LEAST_ONCE, false);
			
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
