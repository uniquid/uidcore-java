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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

public class DefaultRequestHandler extends RequestMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestHandler.class.getName());

    @Override
    public FunctionResponseMessage handleFunctionRequest(FunctionRequestMessage message) {
        LOGGER.info("Received FunctionRequest!");

        try {
            // Check if sender is authorized or throw exception
            ProviderChannel providerChannel = simplifier.getProvider(message);

            if (providerChannel != null) {
                // Get bytes from Smart Contract
                byte[] payload = simplifier.getBitmask(providerChannel, message.getFunction());

                LOGGER.info("Performing function...");
                return simplifier.performProviderRequest(message, payload);

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
}
