package com.uniquid.core.messages;

public class FunctionRequestMessage implements UniquidMessage {

	private long id;
	
	private String user, parameters;
	
	private int method;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	@Override
	public MessageType getMessageType() {

		return MessageType.FUNCTION_REQUEST;

	}
	
}
