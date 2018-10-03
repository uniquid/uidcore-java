package com.uniquid.core;

import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.messages.UniquidMessage;

public interface MessageHandler {

    FunctionResponseMessage handleMessage(UniquidMessage message);
}
