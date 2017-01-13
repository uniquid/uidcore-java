package com.uniquid.uniquid_core.connector.mqtt.provider;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.uniquid.uniquid_core.OutputMessage;
import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;
import com.uniquid.uniquid_core.connector.mqtt.StringBufferOutputStream;
import com.uniquid.uniquid_core.provider.FunctionOutputStream;

public class MQTTMessageResponse implements OutputMessage<JSONMessage> {
	
	private StringWriter stringWriter;
	private JSONMessage jsonResponse;
	private String destination;

	public MQTTMessageResponse() {
		
		stringWriter =  new StringWriter();
		this.jsonResponse = new JSONMessage();
		
	}
	
	@Override
	public FunctionOutputStream getOutputStream() {

		return new StringBufferOutputStream(stringWriter.getBuffer());

	}
	
	public JSONMessage getJSONResponse() {
		return jsonResponse;
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
	public JSONMessage getContent() {
		return getJSONResponse();
	}

	@Override
	public void setParameter(String name, Object value) {
		
		if (OutputMessage.SENDER.equals(name)) {
			jsonResponse.setSender((String) value);
		} else if (OutputMessage.ERROR.equals(name)) {
			jsonResponse.getBody().put("error", value);
		}
//		} else if (OutputMessage.PARAMS.equals(name)) {
//			return (String) jsonMessage.getBody().get("params");
//		} else if (OutputMessage.ID.equals(name)) {
//			return String.valueOf(jsonMessage.getBody().get("id"));
//		}
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
