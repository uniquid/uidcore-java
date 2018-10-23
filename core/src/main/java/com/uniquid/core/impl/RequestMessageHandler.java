package com.uniquid.core.impl;

import com.uniquid.core.MessageHandler;
import com.uniquid.messages.CapabilityMessage;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.messages.UniquidMessage;

public abstract class RequestMessageHandler implements MessageHandler {

    protected UniquidSimplifier simplifier;

    public abstract FunctionResponseMessage handleFunctionRequest(FunctionRequestMessage message);

    public abstract void handleUniquidCapability(CapabilityMessage message);

    @Override
    public FunctionResponseMessage handleMessage(UniquidSimplifier simplifier, UniquidMessage message) {
        if (message != null) {
            this.simplifier = simplifier;
            switch (message.getMessageType()) {
                case FUNCTION_REQUEST:
                    return handleFunctionRequest((FunctionRequestMessage)message);
                case UNIQUID_CAPABILITY:
                    handleUniquidCapability((CapabilityMessage)message);
                    break;
                default:
                    break;
            }
        }
        return null;
    }
}
