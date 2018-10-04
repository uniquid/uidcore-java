package com.uniquid.core.impl;

import com.subgraph.orchid.encoders.Hex;
import com.uniquid.messages.CapabilityMessage;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.node.UniquidCapability;
import com.uniquid.node.UniquidNode;
import com.uniquid.node.UniquidNodeState;
import com.uniquid.node.exception.NodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRequestHandler extends RequestMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestHandler.class.getName());
    private static final String CONSOLE = "CONSOLE";

    @Override
    public FunctionResponseMessage handleFunctionRequest(FunctionRequestMessage message) {
        LOGGER.info(CONSOLE, "Received FunctionRequest!");

        try {
            // Check if sender is authorized or throw exception
            byte[] payload = simplifier.checkSender(message);

            LOGGER.info(CONSOLE, "Performing function...");
            return simplifier.performProviderRequest(message, payload);

        } catch (Exception e) {
            LOGGER.error(CONSOLE, "Error performing function: ", e);
        }
        return null;
    }

    @Override
    public void handleUniquidCapability(CapabilityMessage message) {
        LOGGER.info(CONSOLE, "Received capability!");

        UniquidNode node = simplifier.getNode();

        if (!UniquidNodeState.READY.equals(node.getNodeState())) {
            LOGGER.warn(CONSOLE, "Node is not yet READY! Skipping request");
            return;
        }

        // transform inputMessage to uniquidCapability
        UniquidCapability capability = null;
        try {
            capability = new UniquidCapability.UniquidCapabilityBuilder()
                    .setResourceID(message.getResourceID())
                    .setAssigner(message.getAssigner())
                    .setAssignee(message.getAssignee())
                    .setRights(Hex.decode(message.getRights()))
                    .setSince(message.getSince())
                    .setUntil(message.getUntil())
                    .setAssignerSignature(message.getAssignerSignature())
                    .build();
        } catch (Exception e) {
            LOGGER.error(CONSOLE, "Error creating capability: ", e);
        }

        try {
            // tell node to receive provider capability
            node.receiveProviderCapability(capability);
        } catch (NodeException e) {
            LOGGER.error(CONSOLE, "Error receiving provider capability: ", e);
        }
    }
}
