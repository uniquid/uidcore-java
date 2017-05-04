package com.uniquid.core.connector.mqtt;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class RPCProviderRequestTest {

	@Test
	public void testEmptyBuild() {
		
		RPCProviderRequest rpcProviderRequest = new RPCProviderRequest.Builder()	
				.build();
		
		Assert.assertEquals(rpcProviderRequest.getSender(), null);
		Assert.assertEquals(rpcProviderRequest.getParams(), null);
		Assert.assertEquals(rpcProviderRequest.getFunction(), 0);
		Assert.assertNotEquals(0, rpcProviderRequest.getId());
		
	}
	
	@Test
	public void testBuild() {
		String sender = "bea";
		int method = 4;
		String params = "params";
		
		RPCProviderRequest rpcProviderRequest = new RPCProviderRequest.Builder()
				.set_sender(sender)
				.set_rpcMethod(method)
				.set_params(params)
				.build();
		
		Assert.assertEquals(sender, rpcProviderRequest.getSender());
		Assert.assertEquals(method, rpcProviderRequest.getFunction());
		Assert.assertEquals(params, rpcProviderRequest.getParams());
		Assert.assertNotEquals(0, rpcProviderRequest.getId());
		
	}
	
	@Test
	public void testToJSONString() {
		String sender = "sender";
		int method = 4;
		String params = "params";
		
		RPCProviderRequest rpcProviderRequest = new RPCProviderRequest.Builder()
				.set_sender(sender)
				.set_rpcMethod(method)
				.set_params(params)
				.build();
		
		String jsonString = rpcProviderRequest.toJSONString();
		JSONObject jsonObject = new JSONObject(jsonString);
		Assert.assertEquals(sender, jsonObject.getString("sender"));
		JSONObject jsonBody = jsonObject.getJSONObject("body");
		Assert.assertEquals(method, jsonBody.getInt("method"));
		Assert.assertEquals(params, jsonBody.getString("params"));
		Assert.assertNotEquals(0, jsonBody.getLong("id"));
		
	}
	
	@Test
	public void testFromJSONString() {
		String sender = "sender";
		int method = 4;
		String params = "params";
		long id = 123456789;
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("sender", sender);
		JSONObject body = new JSONObject();
		body.put("method", method);
		body.put("params", params);
		body.put("id", id);
		jsonObject.put("body", body);
		
		String request = jsonObject.toString();
		
		try {
			RPCProviderRequest rpcProviderRequest = RPCProviderRequest.fromJSONString(request);
			Assert.assertEquals(sender, rpcProviderRequest.getSender());
			Assert.assertEquals(method, rpcProviderRequest.getFunction());
			Assert.assertEquals(params, rpcProviderRequest.getParams());
			Assert.assertNotEquals(0, rpcProviderRequest.getId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
 }
