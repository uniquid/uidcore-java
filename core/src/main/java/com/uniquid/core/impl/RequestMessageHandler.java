package com.uniquid.core.impl;

import com.uniquid.core.MessageHandler;
import com.uniquid.messages.*;

public abstract class RequestMessageHandler implements MessageHandler {

    public abstract FunctionResponseMessage handleFunctionRequest(FunctionRequestMessage message);

    public abstract void handleUniquidCapability(CapabilityMessage message);

    @Override
    public FunctionResponseMessage handleMessage(UniquidMessage message) {
        if (message != null) {
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
