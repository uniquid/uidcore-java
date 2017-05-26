package com.uniquid.core.connector.mqtt;

import org.json.JSONObject;

import com.uniquid.core.ProviderResponse;

/**
 * Default implementation of {@link ProviderResponse} that uses JSON-RPC like format.
 */
public class RPCProviderResponse implements ProviderResponse {
	
	private String sender;
	private String result;
	private int error;
	private long id;
	
	private RPCProviderResponse() {
		// DO NOTHING
	}
	
	@Override
	public String getSender() {
		return sender;
	}

	@Override
	public void setSender(final String sender) {
		this.sender = sender;
	}
	
	@Override
	public String getResult() {
		return result;
	}
	
	@Override
	public void setResult(final String result) {
		this.result = result;
	}
	
	@Override
	public int getError() {
		return error;
	}

	@Override
	public void setError(final int error) {
		this.error = error;
	}
	
	/**
	 * Returns the unique id of this request
	 * @return the unique id of this request
	 */
	public long getId() {
		return id;
	}

	/**
	 * Set the unique id of this request
	 * @param id the unique id of this request
	 */
	private void setId(final long id) {
		this.id = id;
	}

	/**
	 * Returns a JSON String representation of this {@link ProviderResponse}
	 * @return a JSON String representation of this {@link ProviderResponse}
	 */
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
	
	/**
	 * Construct an instance from a JSON String
	 * @param jsonString the string containing JSON message
	 * @return an instance of RPCProviderResponse
	 * @throws Exception in case an error occurs.
	 */
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
	
	/**
	 * Builder pattern for creating {@link RPCProviderRequest}
	 */
	public static class Builder {
		private String _sender;
		private String _result;
		private int _error;
		
		/**
		 * Set the sender of the response
		 * @param _sender the sender of the response
		 * @return the Builder
		 */
		public Builder set_sender(String _sender) {
			this._sender = _sender;
			return this;
		}
		
		/**
		 * Set the result of the response
		 * @param _result the result of the response
		 * @return the Builder
		 */
		public Builder set_result(String _result) {
			this._result = _result;
			return this;
		}
		
		/**
		 * Set the error of the response
		 * @param _error the error of the response
		 * @return the Builder
		 */
		public Builder set_error(int _error) {
			this._error = _error;
			return this;
		}
		
		/**
		 * Creates an instance of {@link Builder} with the specified id
		 * @return an instance of {@link Builder} with the specified id
		 */
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
