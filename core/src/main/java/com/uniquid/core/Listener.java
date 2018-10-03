package com.uniquid.core;

import com.uniquid.connector.Connector;
import com.uniquid.connector.ConnectorException;
import com.uniquid.connector.EndPoint;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.messages.UniquidMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class.getName());

    private Connector connector;
    private MessageHandler handler;

    public Listener(Connector connector, MessageHandler handler) {
        this.connector = connector;
        this.handler = handler;
    }

    private boolean startConnector() {
        try {
            connector.start();
            return true;
        } catch (ConnectorException e) {
            LOGGER.error("Connection problem. Connector::start() throw exception ", e);
        }
        return false;
    }

    private boolean stopConnector() {
        try {
            connector.stop();
            return true;
        } catch (ConnectorException e) {
            LOGGER.error("Connection problem. Connector::stop() throw exception ", e);
        }
        return false;
    }

    @Override
    public void run() {
        // start connector
        if (startConnector()) {

            // until not interrupted
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    LOGGER.info("Wait to receive request...");

                    // this will block until a message is received
                    EndPoint endPoint = connector.accept();

                    UniquidMessage request = endPoint.getRequest();
                    if (request != null) {
                        LOGGER.info("Received {} message!", request.getMessageType());

                        FunctionResponseMessage response = handler.handleMessage(request);
                        if (response != null) {
                            endPoint.setResponse(response);
                        }
                    }

                    endPoint.flush();

                } catch (ConnectorException e) {
                    LOGGER.error("Connection problem. Connector::accept() throw exception ", e);
                    break;
                } catch (InterruptedException e) {
                    LOGGER.error("Received request to stop. Exiting");
                    break;
                }
            }

            // stop active connector
            stopConnector();
        }
    }
}
