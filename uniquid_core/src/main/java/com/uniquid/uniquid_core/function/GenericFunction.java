package com.uniquid.uniquid_core.function;

import java.io.IOException;
import java.util.Enumeration;

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
		return "";
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
	public abstract void service(FunctionRequest functionRequest, FunctionResponse functionResponse)
			throws FunctionException, IOException;

	@Override
	public String getFunctionName() {
		return config.getFunctionName();
	}

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
