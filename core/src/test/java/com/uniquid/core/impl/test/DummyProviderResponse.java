package com.uniquid.core.impl.test;

import com.uniquid.core.ProviderResponse;

public class DummyProviderResponse implements ProviderResponse {
	
	private String sender;
	private String result;
	private int error;

	@Override
	public String getSender() {
		return sender;
	}

	@Override
	public void setSender(String sender) {
		this.sender = sender;
	}

	@Override
	public String getResult() {
		return result;
	}

	@Override
	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public int getError() {
		return error;
	}

	@Override
	public void setError(int error) {
		this.error = error;
	}

}
