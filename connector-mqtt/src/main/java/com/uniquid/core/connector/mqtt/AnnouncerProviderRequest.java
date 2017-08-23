package com.uniquid.core.connector.mqtt;

import java.security.SecureRandom;

import org.json.JSONObject;

import com.uniquid.core.ProviderRequest;

/**
 * Default implementation of {@link ProviderRequest} that uses JSON-RPC like format.
 */
public class AnnouncerProviderRequest implements ProviderRequest {
	
	private String sender;
	private String name;
	private String xpub;
	
	private AnnouncerProviderRequest() {
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
		return 0;
	}
	
	/**
	 * Set the integer representing the function to execute
	 */
	private void setFunction(final int rpcMethod) {
	}
	
	@Override
	public String getParams() {
		return null;
	}
	
	/**
	 * Set the parameters of the function.
	 * @param params the String representing the parameter of the request
	 */
	private void setParams(final String params) {
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getXpub() {
		return xpub;
	}

	public void setXpub(String xpub) {
		this.xpub = xpub;
	}

	/**
	 * Returns a JSON String representation of this {@link ProviderRequest}
	 * @return a JSON String representation of this {@link ProviderRequest}
	 */
	public String toJSONString() {
		
		// Create empty json object
		JSONObject jsonObject = new JSONObject();

		// populate sender
		jsonObject.put("name", name);
		
		jsonObject.put("xpub", xpub);

		return jsonObject.toString();
		
	}
	
	/**
	 * Builder pattern for creating {@link AnnouncerProviderRequest}
	 */
	public static class Builder {
		
		private String _sender;
		private String _name;
		private String _xpub;
		
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
		public Builder set_name(String _name) {
			this._name = _name;
			return this;
		}
		
		/**
		 * Set the parameters of the request
		 * @param _params the parameters of the request
		 * @return the Builder
		 */
		public Builder set_xpub(String _xpub) {
			this._xpub = _xpub;
			return this;
		}
		
		/**
		 * Creates an instance of {@link Builder}
		 * @return an instance of {@link Builder}
		 */
		public AnnouncerProviderRequest build() {
			
			AnnouncerProviderRequest rpcProviderRequest = new AnnouncerProviderRequest();
			
			rpcProviderRequest.setSender(_sender);
			rpcProviderRequest.setName(_name);
			rpcProviderRequest.setXpub(_xpub);
			
			return rpcProviderRequest;
		}
		
	}
	
}
