package com.uniquid.core.provider;

/**
 * A function configuration object used by a function container to pass information to a function during initialization.
 */
public interface FunctionConfig {

    /**
     *  Returns a reference to the FunctionContext in which the caller is executing.
     */
    public FunctionContext getFunctionContext();

}
