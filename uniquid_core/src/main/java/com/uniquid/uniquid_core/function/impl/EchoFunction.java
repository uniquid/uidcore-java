package com.uniquid.uniquid_core.function.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.uniquid.uniquid_core.function.FunctionException;
import com.uniquid.uniquid_core.function.FunctionRequest;
import com.uniquid.uniquid_core.function.FunctionResponse;
import com.uniquid.uniquid_core.function.GenericFunction;

public class EchoFunction extends GenericFunction {

	@Override
	public void service(FunctionRequest functionRequest, FunctionResponse functionResponse)
			throws FunctionException, IOException {
		
		String params = (String) functionRequest.getAttribute("param");
		
		OutputStream outputStream = functionResponse.getOutputStream();
		
		Writer writer = new OutputStreamWriter(outputStream);
		
		writer.write("UID_echo: " + params);
		
		writer.close();
		
	}
	
	@Override
	public String getFunctionName() {
		return "ECHO";
	}

}
