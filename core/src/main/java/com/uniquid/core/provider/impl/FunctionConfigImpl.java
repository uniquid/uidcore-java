package com.uniquid.core.provider.impl;

import com.uniquid.core.provider.FunctionConfig;
import com.uniquid.core.provider.FunctionContext;

public class FunctionConfigImpl implements FunctionConfig {
	
	private FunctionContext functionContext;
	
	public FunctionConfigImpl(FunctionContext functionContext) {
		this.functionContext = functionContext;
	}

	@Override
	public FunctionContext getFunctionContext() {
		return functionContext;
	}

}
