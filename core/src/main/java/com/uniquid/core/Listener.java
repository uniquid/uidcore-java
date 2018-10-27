package com.uniquid.core;

import com.uniquid.connector.Connector;
import com.uniquid.connector.ConnectorException;
import com.uniquid.connector.EndPoint;
import com.uniquid.connector.impl.MQTTConnector;
import com.uniquid.core.impl.UniquidSimplifier;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.messages.UniquidMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class.getName());

    private String broker;
    private String topic;
    private MessageHandler handler;
    private UniquidSimplifier parentSimplifier;

    public Listener(String broker, String topic, MessageHandler handler) {
        this.broker = broker;
        this.topic = topic;
        this.handler = handler;
    }

    public void setParentSimplifier(UniquidSimplifier parent) {
        parentSimplifier = parent;
    }

    @Override
    public void run() {
        // start connector
        try (Connector connector = new MQTTConnector(broker, topic)) {

            // start connection
            connector.connect();

            // until not interrupted
            while (!Thread.currentThread().isInterrupted()) {
                LOGGER.info("Wait to receive request...");

                // this will block until a message is received
                EndPoint endPoint = connector.accept();

                UniquidMessage request = endPoint.getRequest();
                if (request != null) {
                    LOGGER.info("Received {} message!", request.getMessageType());

                    FunctionResponseMessage response = handler.handleMessage(parentSimplifier, request);
                    if (response != null) {
                        endPoint.setResponse(response);
                    }
                }

                endPoint.flush();
            }

        } catch (InterruptedException e) {
            LOGGER.error("Received request to stop. Exiting");
            // since flag value is not used here, restore it to check the flag later and have it be true
            Thread.currentThread().interrupt();

        } catch (ConnectorException e) {
            LOGGER.error("Connection problem. MQTTConnector throw exception ", e);

        }
    }
}
