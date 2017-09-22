package com.uniquid.core.messages;

public class FunctionResponseMessage implements UniquidMessage {

	private long id;
	
	private String provider, result;
	
	private int error;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public int getError() {
		return error;
	}

	public void setError(int error) {
		this.error = error;
	}

	@Override
	public MessageType getMessageType() {

		return MessageType.FUNCTION_RESPONSE;

	}
	
}
