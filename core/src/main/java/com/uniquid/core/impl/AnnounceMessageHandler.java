package com.uniquid.core.impl;

import com.uniquid.core.MessageHandler;
import com.uniquid.messages.AnnounceMessage;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.messages.MessageType;
import com.uniquid.messages.UniquidMessage;

public abstract class AnnounceMessageHandler implements MessageHandler {

    public abstract void handleAnnounceMessage(AnnounceMessage message);

    @Override
    public FunctionResponseMessage handleMessage(UniquidMessage message) {
        if (isAnnounceMessage(message)) {
            handleAnnounceMessage((AnnounceMessage) message);
        }
        return null;
    }

    private boolean isAnnounceMessage(UniquidMessage message) {
        return message != null && MessageType.ANNOUNCE.equals(message.getMessageType());
    }
}
