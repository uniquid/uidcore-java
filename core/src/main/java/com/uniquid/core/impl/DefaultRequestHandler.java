/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.core.impl;

import com.uniquid.messages.CapabilityMessage;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.node.UniquidCapability;
import com.uniquid.node.UniquidNode;
import com.uniquid.node.UniquidNodeState;
import com.uniquid.node.exception.NodeException;
import com.uniquid.register.provider.ProviderChannel;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRequestHandler extends RequestMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestHandler.class.getName());

    @Override
    public FunctionResponseMessage handleFunctionRequest(FunctionRequestMessage message) {
        LOGGER.info("Received FunctionRequest!");

        try {
            // Check if sender is authorized or throw exception
            ProviderChannel providerChannel = simplifier.getNode().getProviderChannel(message);

            if (providerChannel != null && isValidMessage(message.getId())) {
                // Get bytes from Smart Contract
                byte[] payload = simplifier.getBitmask(providerChannel, message.getMethod());

                LOGGER.info("Performing function...");
                message.setSender(providerChannel.getUserAddress());
                return simplifier.performProviderRequest(message, payload, providerChannel.getPath());
            }

        } catch (Exception e) {
            LOGGER.error("Error performing function: ", e);
        }
        return null;
    }

    @Override
    public void handleUniquidCapability(CapabilityMessage message) {
        LOGGER.info("Received capability!");

        UniquidNode node = simplifier.getNode();

        if (!UniquidNodeState.READY.equals(node.getNodeState())) {
            LOGGER.warn("Node is not yet READY! Skipping request");
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
            LOGGER.error("Error creating capability: ", e);
        }

        try {
            // tell node to receive provider capability
            node.receiveProviderCapability(capability);
        } catch (NodeException e) {
            LOGGER.error("Error receiving provider capability: ", e);
        }
    }

    /**
     * Check if message is still valid
     * @param id is the id of the received message
     * @return true if message id is less then current time in milliseconds + 60 seconds
     * and greater then current time - 60 seconds, false otherwise
     * */
    private boolean isValidMessage(long id) {
        long now = System.currentTimeMillis();
        long minLimit = now - (60 * 1000);
        long maxLimit = now + (60 * 1000);

        return (id > minLimit && id < maxLimit);
    }
}
