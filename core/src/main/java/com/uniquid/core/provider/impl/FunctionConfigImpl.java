package com.uniquid.core.provider.impl;

import com.uniquid.core.provider.FunctionConfig;
import com.uniquid.core.provider.FunctionContext;

/**
 * Implementation of {@link FunctionConfig}
 */
public class FunctionConfigImpl implements FunctionConfig {
	
	private FunctionContext functionContext;
	
	/**
	 * Creates an instance from the {@link FunctionContext}
	 * @param functionContext the {@link FunctionContext} to use.
	 */
	public FunctionConfigImpl(FunctionContext functionContext) {
		this.functionContext = functionContext;
	}

	@Override
	public FunctionContext getFunctionContext() {
		return functionContext;
	}

}
