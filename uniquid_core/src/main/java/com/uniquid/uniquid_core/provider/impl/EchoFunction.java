package com.uniquid.uniquid_core.provider.impl;

import java.io.IOException;
import java.io.PrintWriter;

import com.uniquid.uniquid_core.InputMessage;
import com.uniquid.uniquid_core.OutputMessage;
import com.uniquid.uniquid_core.provider.FunctionException;
import com.uniquid.uniquid_core.provider.GenericFunction;

public class EchoFunction extends GenericFunction {

	@Override
	public void service(InputMessage functionRequest, OutputMessage functionResponse)
			throws FunctionException, IOException {
		
		PrintWriter printWriter = functionResponse.getWriter();
		
		printWriter.print("UID_echo: " + functionRequest.getParameter(InputMessage.PARAMS));
		
	}
	
}
