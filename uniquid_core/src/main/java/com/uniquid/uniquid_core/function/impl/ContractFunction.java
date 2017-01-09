package com.uniquid.uniquid_core.function.impl;

import java.io.IOException;

import com.uniquid.uniquid_core.function.FunctionException;
import com.uniquid.uniquid_core.function.FunctionRequest;
import com.uniquid.uniquid_core.function.FunctionResponse;
import com.uniquid.uniquid_core.function.GenericFunction;

public class ContractFunction extends GenericFunction {

	@Override
	public void service(FunctionRequest functionRequest, FunctionResponse functionResponse)
			throws FunctionException, IOException {
		// TODO
	}
	
	@Override
	public String getFunctionName() {
		return "CONTRACT";
	}

}
