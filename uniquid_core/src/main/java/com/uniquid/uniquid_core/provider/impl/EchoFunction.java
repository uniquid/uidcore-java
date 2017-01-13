package com.uniquid.uniquid_core.provider.impl;

import java.io.IOException;
import java.io.PrintWriter;

import com.uniquid.uniquid_core.provider.FunctionException;
import com.uniquid.uniquid_core.provider.GenericFunction;
import com.uniquid.uniquid_core.provider.ProviderRequest;
import com.uniquid.uniquid_core.provider.ProviderResponse;

public class EchoFunction extends GenericFunction {

	@Override
	public void service(ProviderRequest functionRequest, ProviderResponse functionResponse)
			throws FunctionException, IOException {
		
		PrintWriter printWriter = functionResponse.getWriter();
		
		printWriter.print("UID_echo: " + functionRequest.getParameter(ProviderRequest.PARAMS));
		
	}
	
}
