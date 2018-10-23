package com.uniquid.core.provider.impl;

import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.FunctionConfig;
import com.uniquid.core.provider.FunctionContext;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;

import java.io.IOException;

/**
 * Defines a generic, protocol-independent function.
 * Designed to be subclassed by real {@link Function} implementation
 */
public abstract class GenericFunction implements Function, FunctionConfig {

    private transient FunctionConfig config;

    public GenericFunction() {
        // NOTHING TO DO
    }

    @Override
    public void destroy() {
        // NOTHING TO DO
    }

    @Override
    public FunctionConfig getFunctionConfig() {
        return config;
    }

    @Override
    public String getFunctionInfo() {
        return "GenericFunction from Uniquid";
    }

    @Override
    public void init(FunctionConfig config) throws FunctionException {
        this.config = config;
        this.init();
    }

    /**
     * Designed to be extended by subclasses.
     *
     * @throws FunctionException in case a problem occurs.
     */
    public void init() throws FunctionException {
        // NOOP by default
    }

    @Override
    public abstract void service(FunctionRequestMessage inputMessage, FunctionResponseMessage outputMessage, byte[] payload)
            throws FunctionException, IOException;

    @Override
    public FunctionContext getFunctionContext() {
        return getFunctionConfig().getFunctionContext();
    }

}
