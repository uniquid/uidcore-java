package com.uniquid.core.connector.mqtt;

import java.security.SecureRandom;

import org.json.JSONObject;

import com.uniquid.core.ProviderRequest;

/**
 * Default implementation of {@link ProviderRequest} that uses JSON-RPC like format.
 */
public class RPCProviderRequest implements ProviderRequest {
	
	private String sender;
	private int rpcMethod;
	private String params;
	private long id;
	
	private RPCProviderRequest() {
		// DO NOTHING
	}

	@Override
	public String getSender() {
		return sender;
	}
	
	/**
	 * Set the sender of the request
	 * @param the sender of the request
	 */
	private void setSender(final String sender) {
		this.sender = sender;
	}
	
	@Override
	public int getFunction() {
		return rpcMethod;
	}
	
	/**
	 * Set the integer representing the function to execute
	 */
	private void setFunction(final int rpcMethod) {
		this.rpcMethod = rpcMethod;
	}
	
	@Override
	public String getParams() {
		return params;
	}
	
	/**
	 * Set the parameters of the function.
	 * @param params the String representing the parameter of the request
	 */
	private void setParams(final String params) {
		this.params = params;
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
	 * Returns a JSON String representation of this {@linke ProviderRequest}
	 * @return a JSON String representation of this {@linke ProviderRequest}
	 */
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
	
	/**
	 * Builder pattern for creating {@link RPCProviderRequest}
	 */
	public static class Builder {
		
		private String _sender;
		private int _rpcMethod;
		private String _params;
		
		/**
		 * Set the sender of the request
		 * @param _sender the sender of the request
		 * @return the Builder
		 */
		public Builder set_sender(String _sender) {
			this._sender = _sender;
			return this;
		}
		
		/**
		 * Set the integer representing the function of the request
		 * @param _rpcMethod the integer representing the function of the request
		 * @return the Builder
		 */
		public Builder set_rpcMethod(int _rpcMethod) {
			this._rpcMethod = _rpcMethod;
			return this;
		}
		
		/**
		 * Set the parameters of the request
		 * @param _params the parameters of the request
		 * @return the Builder
		 */
		public Builder set_params(String _params) {
			this._params = _params;
			return this;
		}
		
		/**
		 * Creates an instance of {@link Builder}
		 * @return an instance of {@link Builder}
		 */
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
	
	/**
	 * Construct an instance from a JSON String
	 * @param jsonString the string containing JSON message
	 * @return an instance of RPCProviderRequest
	 * @throws Exception in case an error occurs.
	 */
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
