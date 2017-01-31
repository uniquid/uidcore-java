package com.uniquid.uniquid_core.connector.mqtt.provider;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.uniquid.uniquid_core.OutputMessage;
import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;
import com.uniquid.uniquid_core.connector.mqtt.StringBufferOutputStream;
import com.uniquid.uniquid_core.provider.FunctionOutputStream;

public class MQTTMessageResponse implements OutputMessage<JSONMessage> {
	
	private StringWriter stringWriter;
	private JSONMessage jsonResponse;
	private Map<String, Object> parameters;

	public MQTTMessageResponse() {
		
		this.stringWriter =  new StringWriter();
		this.jsonResponse = new JSONMessage();
		this.parameters = new HashMap<String, Object>();
		
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
	public JSONMessage getPayload() {
		return getJSONResponse();
	}

	@Override
	public void setParameter(String name, Object value) {
		
		if (OutputMessage.SENDER.equals(name)) {
			jsonResponse.setSender((String) value);
		} else if (OutputMessage.ERROR.equals(name)) {
			jsonResponse.getBody().put("error", value);
		} else if (OutputMessage.ID.equals(name)) {
			jsonResponse.getBody().put("id", value);
		} else {
			parameters.put(name, value);
		}
	}


	@Override
	public Object getParameter(String name) {
		if (OutputMessage.SENDER.equals(name)) {
			return jsonResponse.getSender();
		} else if (OutputMessage.ERROR.equals(name)) {
			return jsonResponse.getBody().get("error");
		} else if (OutputMessage.ID.equals(name)) {
			return jsonResponse.getBody().get("id");
		} else {
			return parameters.get(name);
		}
		
	}

}
