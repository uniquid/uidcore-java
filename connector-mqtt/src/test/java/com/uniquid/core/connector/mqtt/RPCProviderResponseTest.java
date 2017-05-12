package com.uniquid.core.connector.mqtt;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class RPCProviderResponseTest {

	@Test
	public void testEmptyBuild() {
		RPCProviderResponse rpcProviderResponse = new RPCProviderResponse.Builder().buildFromId(0);
		Assert.assertEquals(null, rpcProviderResponse.getSender());
		Assert.assertEquals(null, rpcProviderResponse.getResult());
		Assert.assertEquals(0, rpcProviderResponse.getId());
		Assert.assertEquals(0, rpcProviderResponse.getError());
	}
	
	@Test
	public void testBuild() {
		String sender = "sender";
		String result = "result";
		long id = 123456789;
		int error = 1;
		
		RPCProviderResponse rpcProviderResponse = new RPCProviderResponse.Builder()
				.set_sender(sender)
				.set_result(result)
				.set_error(error)
				.buildFromId(id);
		Assert.assertEquals(sender, rpcProviderResponse.getSender());
		Assert.assertEquals(result, rpcProviderResponse.getResult());
		Assert.assertEquals(id, rpcProviderResponse.getId());
		Assert.assertEquals(error, rpcProviderResponse.getError());
	}
	
	@Test
	public void testSender() {
		String sender = "sender";
		long id = 123456789;

		RPCProviderResponse rpcProviderResponse = new RPCProviderResponse.Builder().buildFromId(id);
		rpcProviderResponse.setSender(sender);
		
		Assert.assertEquals(sender, rpcProviderResponse.getSender());
	}
	
	@Test
	public void testResult() {
		String result = "result";
		long id = 123456789;

		RPCProviderResponse rpcProviderResponse = new RPCProviderResponse.Builder().buildFromId(id);
		rpcProviderResponse.setResult(result);
		
		Assert.assertEquals(result, rpcProviderResponse.getResult());
	}
	
	@Test
	public void testError() {
		int error = 1;
		long id = 123456789;

		RPCProviderResponse rpcProviderResponse = new RPCProviderResponse.Builder().buildFromId(id);
		rpcProviderResponse.setError(error);
		
		Assert.assertEquals(error, rpcProviderResponse.getError());

	}

	@Test
	public void testToJSONString() {
		String sender = "sender";
		String result = "result";
		long id = 123456789;
		int error = 1;
		
		RPCProviderResponse rpcProviderResponse = new RPCProviderResponse.Builder()
				.set_sender(sender)
				.set_result(result)
				.set_error(error)
				.buildFromId(id);
		
		String json = rpcProviderResponse.toJSONString();
		JSONObject response = new JSONObject(json);
		JSONObject body = response.getJSONObject("body");
		
		Assert.assertEquals(sender, response.getString("sender"));
		Assert.assertEquals(result, body.getString("result"));
		Assert.assertEquals(error, body.getInt("error"));
		Assert.assertEquals(id, body.getLong("id"));
	}

	@Test
	public void testFromJSONString() {
		String sender = "sender";
		String result = "result";
		long id = 123456789;
		int error = 1;
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("sender", sender);
		JSONObject body = new JSONObject();
		body.put("result", result);
		body.put("id", id);
		body.put("error", error);
		
		jsonObject.put("body", body);
		
		String response = jsonObject.toString();
		
		try {
			RPCProviderResponse rpcProviderResponse = RPCProviderResponse.fromJSONString(response.toString());
			Assert.assertEquals(sender, rpcProviderResponse.getSender());
			Assert.assertEquals(result, rpcProviderResponse.getResult());
			Assert.assertEquals(id, rpcProviderResponse.getId());
			Assert.assertEquals(error, rpcProviderResponse.getError());
			
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test(expected = Exception.class)
	public void testFromJSONStringException() throws Exception {
		String sender = "sender";
		String result = "result";
		long id = 0;
		int error = 1;
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("sender", sender);
		JSONObject body = new JSONObject();
		body.put("result", result);
		body.put("id", id);
		body.put("error", error);
		
		jsonObject.put("body", body);
		
		String response = jsonObject.toString();
		
		RPCProviderResponse rpcProviderResponse = RPCProviderResponse.fromJSONString(response.toString());
		Assert.assertEquals(sender, rpcProviderResponse.getSender());
		Assert.assertEquals(result, rpcProviderResponse.getResult());
		Assert.assertEquals(id, rpcProviderResponse.getId());
		Assert.assertEquals(error, rpcProviderResponse.getError());
			
	}
}
