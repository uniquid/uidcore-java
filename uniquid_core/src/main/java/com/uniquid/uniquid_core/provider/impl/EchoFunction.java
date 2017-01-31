package com.uniquid.uniquid_core.provider.impl;

import java.io.IOException;
import java.io.PrintWriter;

import com.uniquid.uniquid_core.InputMessage;
import com.uniquid.uniquid_core.OutputMessage;
import com.uniquid.uniquid_core.provider.exception.FunctionException;

public class EchoFunction extends GenericFunction {

	@Override
	public void service(InputMessage inputMessage, OutputMessage outputMessage)
			throws FunctionException, IOException {
		
		PrintWriter printWriter = outputMessage.getWriter();
		
		printWriter.print("UID_echo: " + inputMessage.getParameter(InputMessage.PARAMS));
		
	}
	
}
