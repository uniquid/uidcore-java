package com.uniquid.core.provider.impl;

import java.io.IOException;
import java.io.PrintWriter;

import com.uniquid.core.InputMessage;
import com.uniquid.core.OutputMessage;
import com.uniquid.core.provider.exception.FunctionException;

public class EchoFunction extends GenericFunction {

	@Override
	public void service(InputMessage inputMessage, OutputMessage outputMessage, byte[] payload)
			throws FunctionException, IOException {
		
		PrintWriter printWriter = outputMessage.getWriter();
		
		printWriter.print("UID_echo: " + inputMessage.getParameter(InputMessage.PARAMS));
		
	}
	
}
