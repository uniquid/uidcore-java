package com.uniquid.uniquid_core.connector.mqtt;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.uniquid.uniquid_core.connector.StringBufferOutputStream;
import com.uniquid.uniquid_core.function.FunctionOutputStream;
import com.uniquid.uniquid_core.function.FunctionResponse;

public class MQTTMessageResponse implements FunctionResponse {
	
	private StringWriter stringWriter;
	private JSONMessage jsonRequest;
	private int status;

	public MQTTMessageResponse(JSONMessage jsonRequest) {
		
		stringWriter =  new StringWriter();
		this.jsonRequest = jsonRequest;
		
	}
	
	@Override
	public FunctionOutputStream getOutputStream() {

		return new StringBufferOutputStream(stringWriter.getBuffer());

	}
	
	public JSONMessage getJSONRequest() {
		return jsonRequest;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		
		PrintWriter printWriter = new PrintWriter(stringWriter);
		
		return printWriter;
	}
	
	public String getOutputString() {
		return stringWriter.toString();
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public int getStatus() {
		return status;
	}

}
