package com.uniquid.core.connector.mqtt;

import java.security.SecureRandom;

import org.json.JSONObject;

import com.uniquid.core.ProviderRequest;

public class RPCProviderRequest implements ProviderRequest {
	
	private String sender;
	private int rpcMethod;
	private String params;
	private long id;
	
	private RPCProviderRequest() {
	}

	public String getSender() {
		return sender;
	}
	
	private void setSender(final String sender) {
		this.sender = sender;
	}
	
	public int getFunction() {
		return rpcMethod;
	}
	
	private void setFunction(final int rpcMethod) {
		this.rpcMethod = rpcMethod;
	}
	
	public String getParams() {
		return params;
	}
	
	private void setParams(final String params) {
		this.params = params;
	}
	
	public long getId() {
		return id;
	}
	
	private void setId(final long id) {
		this.id = id;
	}
	
	public String toJSONString() {
		
		// Create empty json object
		JSONObject jsonObject = new JSONObject();

		// populate sender
		jsonObject.put("sender", sender);

		// Create empty json child
		JSONObject jsonbody = new JSONObject();

		// Put all keys inside body
		jsonbody.put("method", rpcMethod);
		
		jsonbody.put("params", params);
		
		jsonbody.put("id", id);

		// Add body
		jsonObject.put("body", jsonbody);

		return jsonObject.toString();
		
	}
	
	public static class Builder {
		
		private String _sender;
		private int _rpcMethod;
		private String _params;
		
		public Builder set_sender(String _sender) {
			this._sender = _sender;
			return this;
		}
		
		public Builder set_rpcMethod(int _rpcMethod) {
			this._rpcMethod = _rpcMethod;
			return this;
		}
		
		public Builder set_params(String _params) {
			this._params = _params;
			return this;
		}
		
		public RPCProviderRequest build() {
			
			RPCProviderRequest rpcProviderRequest = new RPCProviderRequest();
			
			SecureRandom random = new SecureRandom();
			
			rpcProviderRequest.setSender(_sender);
			rpcProviderRequest.setFunction(_rpcMethod);
			rpcProviderRequest.setParams(_params);
			rpcProviderRequest.setId(random.nextLong());
			
			return rpcProviderRequest;
		}
		
	}
	
	public static RPCProviderRequest fromJSONString(String jsonString) throws Exception {
		
		final JSONObject jsonMessage = new JSONObject(jsonString);

		final String sender = jsonMessage.getString("sender");

		final JSONObject jsonBody = jsonMessage.getJSONObject("body");

		final int method = jsonBody.getInt("method");
		
		final String params =jsonBody.getString("params"); 
		
		final long id = jsonBody.getLong("id");
		
		final RPCProviderRequest rpcProviderRequest = new RPCProviderRequest();
		
		rpcProviderRequest.setSender(sender);
		rpcProviderRequest.setFunction(method);
		rpcProviderRequest.setParams(params);
		rpcProviderRequest.setId(id);
		
		// check for valid request message
		if (sender != null &&
				method > 0 &&
				id != 0 &&
				params != null) {
			return rpcProviderRequest;
		}

		throw new Exception("Received invalid message: " + jsonString);
		
	}

}