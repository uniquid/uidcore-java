package com.uniquid.core.provider.impl;

import java.io.IOException;
import java.util.Enumeration;

import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;
import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.FunctionConfig;
import com.uniquid.core.provider.FunctionContext;
import com.uniquid.core.provider.exception.FunctionException;

/*
 * Defines a generic, protocol-independent function.
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
	
	public void init() throws FunctionException {
        // NOOP by default
    }

	@Override
	public abstract void service(ProviderRequest inputMessage, ProviderResponse outputMessage, byte[] payload)
			throws FunctionException, IOException;

	@Override
	public FunctionContext getFunctionContext() {
		return getFunctionConfig().getFunctionContext();
	}

	@Override
	public String getInitParameter(String name) {
		return getFunctionConfig().getInitParameter(name);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return getFunctionConfig().getInitParameterNames();
	}

}
