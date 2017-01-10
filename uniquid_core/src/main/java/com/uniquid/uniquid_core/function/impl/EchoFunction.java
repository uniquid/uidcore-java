package com.uniquid.uniquid_core.function.impl;

import java.io.IOException;
import java.io.PrintWriter;

import com.uniquid.spv_node.SpvNode;
import com.uniquid.uniquid_core.function.FunctionException;
import com.uniquid.uniquid_core.function.FunctionRequest;
import com.uniquid.uniquid_core.function.FunctionResponse;
import com.uniquid.uniquid_core.function.GenericFunction;

public class EchoFunction extends GenericFunction {

	public EchoFunction(SpvNode spvNode) {
		super(spvNode);
	}

	@Override
	public void service(FunctionRequest functionRequest, FunctionResponse functionResponse)
			throws FunctionException, IOException {
		
		PrintWriter printWriter = functionResponse.getWriter();
		
		printWriter.print("UID_echo: " + functionRequest.getParameter(FunctionRequest.PARAMS));
		
	}
	
	@Override
	public String getFunctionName() {
		return "ECHO";
	}

}
