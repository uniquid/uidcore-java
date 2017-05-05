package com.uniquid.core.impl.test;

import com.uniquid.core.ProviderRequest;

public class DummyProviderRequest implements ProviderRequest {
	
	private String sender;
	private String params;
	private int function;
	
	public DummyProviderRequest(final String sender, final int function, final String params) {
		this.sender = sender;
		this.function = function;
		this.params = params;
	}

	@Override
	public String getSender() {
		return sender;
	}

	@Override
	public int getFunction() {
		return function;
	}

	@Override
	public String getParams() {
		return params;
	}

}
