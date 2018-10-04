package com.uniquid.core;

import com.uniquid.core.impl.UniquidSimplifier;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.messages.UniquidMessage;

public interface MessageHandler {

    FunctionResponseMessage handleMessage(UniquidSimplifier simplifier, UniquidMessage message);
}
