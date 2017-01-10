package com.uniquid.uniquid_core.connector.mqtt;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.uniquid_core.connector.Connector;
import com.uniquid.uniquid_core.connector.ConnectorException;
import com.uniquid.uniquid_core.connector.EndPoint;
import com.uniquid.uniquid_core.function.FunctionResponse;

public class MQTTConnector implements Connector {

	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTConnector.class.getName());

	private String topic;
	private String broker;

	private MQTTConnector(String topic, String broker) {

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

		public MQTTConnector build() {

			return new MQTTConnector(_topic, _broker);
		}

	}

	@Override
	public EndPoint accept() throws ConnectorException {

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

			// process the message then
			message.ack();

			MQTTMessageRequest mqttMessageRequest = MQTTMessageRequest.fromJSONString(new String(payload));

			EndPoint endPoint = new MQTTEndPoint(this, mqttMessageRequest,
					new MQTTMessageResponse(mqttMessageRequest.getJSONMessage()));

			return endPoint;

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

	public void sendResponse(JSONMessageResponse messageResponse) {

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
			connection.publish(topic, messageResponse.toJSONString().getBytes(), QoS.AT_LEAST_ONCE, false);

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
