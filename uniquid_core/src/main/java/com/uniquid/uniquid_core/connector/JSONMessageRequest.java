package com.uniquid.uniquid_core.connector;

public class JSONMessageRequest {
	
	private JSONMessage message;
	
	public JSONMessageRequest(JSONMessage message) {
		this.message = message;
	}
	
	public String getSender() {
		return message.getSender();
	}

	public void setSender(String sender) {
		message.setSender(sender);
	}

	public int getMethod() {
		return (int) message.getBody().get("method");
	}

	public void setMethod(int method) {
		message.getBody().put("method", method);
	}

	public Object getParams() {
		return message.getBody().get("params");
	}

	public void setParams(String params) {
		message.getBody().put("params", params);
	}

	public int getId() {
		return (int) message.getBody().get("id");
	}

	public void setId(int id) {
		message.getBody().put("id", id);
	}
	
	public String toJSON() {
		return message.toJSON();
	}
	
	public static JSONMessageRequest fromJSONString(String jsonString) {
		JSONMessage message = JSONMessage.fromJsonString(jsonString);
		
		return new JSONMessageRequest(message);
	}

}
