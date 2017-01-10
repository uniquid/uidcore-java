package com.uniquid.uniquid_core.connector.mqtt;

public class JSONMessageResponse {
	
	private JSONMessage message;
	
	public JSONMessageResponse() {
		this.message = new JSONMessage();
	}
	
	public JSONMessageResponse(JSONMessage message) {
		this.message = message;
	}
	
	public String getSender() {
		return message.getSender();
	}

	public void setSender(String sender) {
		message.setSender(sender);
	}
	
	public String getResult() {
		return (String) message.getBody().get("result");
	}

	public void setResult(String result) {
		message.getBody().put("result", result);
	}

	public Object getError() {
		return message.getBody().get("error");
	}

	public void setError(int error) {
		message.getBody().put("error", error);
	}

	public int getId() {
		return (int) message.getBody().get("id");
	}

	public void setId(int id) {
		message.getBody().put("id", id);
	}
	
	public String toJSONString() {
		return message.toJSON();
	}

}
