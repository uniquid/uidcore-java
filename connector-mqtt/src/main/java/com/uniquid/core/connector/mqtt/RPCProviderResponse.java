package com.uniquid.core.connector.mqtt;

import org.json.JSONObject;

import com.uniquid.core.ProviderResponse;

public class RPCProviderResponse implements ProviderResponse {
	
	private String sender;
	private String result;
	private int error;
	private long id;
	
	private RPCProviderResponse() {
	}
	
	public String getSender() {
		return sender;
	}

	public void setSender(final String sender) {
		this.sender = sender;
	}
	
	public String getResult() {
		return result;
	}
	
	public void setResult(final String result) {
		this.result = result;
	}
	
	public int getError() {
		return error;
	}

	public void setError(final int error) {
		this.error = error;
	}
	
	public long getId() {
		return id;
	}

	private void setId(final long id) {
		this.id = id;
	}

	public String toJSONString() {
		
		final JSONObject jsonBody = new JSONObject();
		
		jsonBody.put("result", result);
		jsonBody.put("error", error);
		jsonBody.put("id", id);

		final JSONObject jsonResponse = new JSONObject();

		jsonResponse.put("sender", sender);
		
		jsonResponse.put("body", jsonBody);
		
		return jsonResponse.toString();
	}
	
	public static RPCProviderResponse fromJSONString(String jsonString) throws Exception {
		
		final JSONObject jsonMessage = new JSONObject(jsonString);

		final String sender = jsonMessage.getString("sender");

		final JSONObject jsonBody = jsonMessage.getJSONObject("body");

		final String result = jsonBody.getString("result");
		
		final int error =jsonBody.getInt("error"); 
		
		final long id = jsonBody.getLong("id");
		
		final RPCProviderResponse rpcProviderResponse = new RPCProviderResponse();
		
		rpcProviderResponse.setSender(sender);
		rpcProviderResponse.setResult(result);
		rpcProviderResponse.setError(error);
		rpcProviderResponse.setId(id);
		
		// check for valid request message
		if (sender != null &&
				result != null &&
				id != 0) {
			return rpcProviderResponse;
		}

		throw new Exception("Received invalid message: " + jsonString);
		
	}
	
	public static class Builder {
		private String _sender;
		private String _result;
		private int _error;
		
		public Builder set_sender(String _sender) {
			this._sender = _sender;
			return this;
		}
		
		public Builder set_result(String _result) {
			this._result = _result;
			return this;
		}
		
		public Builder set_error(int _error) {
			this._error = _error;
			return this;
		}
		
		public RPCProviderResponse buildFromId(long _id) {
			
			RPCProviderResponse rpcProviderResponse = new RPCProviderResponse();
			rpcProviderResponse.setSender(_sender);
			rpcProviderResponse.setResult(_result);
			rpcProviderResponse.setError(_error);
			rpcProviderResponse.setId(_id);
			
			return rpcProviderResponse;
		}
		
	}
	

}
