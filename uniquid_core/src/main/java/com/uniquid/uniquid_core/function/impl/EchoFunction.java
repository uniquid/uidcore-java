package com.uniquid.uniquid_core.function.impl;

import java.io.IOException;
import java.io.PrintWriter;

import com.uniquid.uniquid_core.function.FunctionException;
import com.uniquid.uniquid_core.function.FunctionRequest;
import com.uniquid.uniquid_core.function.FunctionResponse;
import com.uniquid.uniquid_core.function.GenericFunction;

public class EchoFunction extends GenericFunction {

	@Override
	public void service(FunctionRequest functionRequest, FunctionResponse functionResponse)
			throws FunctionException, IOException {
		
		PrintWriter printWriter = functionResponse.getWriter();
		
		printWriter.print("UID_echo: " + functionRequest.getParameter(FunctionRequest.PARAMS));
		
	}
	
}
