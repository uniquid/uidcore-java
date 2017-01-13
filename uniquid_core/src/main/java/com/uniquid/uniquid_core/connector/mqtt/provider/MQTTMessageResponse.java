package com.uniquid.uniquid_core.connector.mqtt.provider;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;
import com.uniquid.uniquid_core.connector.mqtt.StringBufferOutputStream;
import com.uniquid.uniquid_core.provider.FunctionOutputStream;
import com.uniquid.uniquid_core.provider.ProviderResponse;

public class MQTTMessageResponse implements ProviderResponse {
	
	private StringWriter stringWriter;
	private JSONMessage jsonRequest;
	private int status;
	private String sender;
	private String destination;

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

	@Override
	public void setSender(String sender) {
		this.sender = sender;
	}

	@Override
	public String getSender() {
		return sender;
	}

	@Override
	public JSONMessage getContent() {
		return getJSONRequest();
	}

	@Override
	public void setParameter(String name, Object value) {
		// DO NOTHING
	}

	@Override
	public String getDestination() {
		return destination;
	}
	
	@Override
	public void setDestination(String destination) {
		this.destination = destination; 
	}

}
