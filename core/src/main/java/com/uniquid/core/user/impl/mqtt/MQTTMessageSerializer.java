package com.uniquid.core.user.impl.mqtt;

import org.json.JSONObject;

import com.uniquid.core.messages.AnnounceMessage;
import com.uniquid.core.messages.FunctionRequestMessage;
import com.uniquid.core.messages.FunctionResponseMessage;
import com.uniquid.core.messages.MessageSerializer;
import com.uniquid.core.messages.MessageType;
import com.uniquid.core.messages.UniquidMessage;

public class MQTTMessageSerializer implements MessageSerializer {
	
	@Override
	public byte[] serialize(UniquidMessage uniquidMessage) throws Exception {
		
		if (MessageType.ANNOUNCE.equals(uniquidMessage.getMessageType())) {
			
			AnnounceMessage announceMessage = (AnnounceMessage) uniquidMessage;
			
			// Create empty json object
			JSONObject jsonObject = new JSONObject();

			// populate sender
			jsonObject.put("name", announceMessage.getName());
			
			jsonObject.put("xpub", announceMessage.getPublicKey());

			return jsonObject.toString().getBytes();
			
		} else if (MessageType.FUNCTION_REQUEST.equals(uniquidMessage.getMessageType())) {
			
			FunctionRequestMessage functionRequestMessage = (FunctionRequestMessage) uniquidMessage;
			
			// Create empty json object
			JSONObject jsonObject = new JSONObject();

			// populate sender
			jsonObject.put("sender", functionRequestMessage.getUser());

			// Create empty json child
			JSONObject jsonbody = new JSONObject();

			// Put all keys inside body
			jsonbody.put("method", functionRequestMessage.getMethod());
			
			jsonbody.put("params", functionRequestMessage.getParameters());
			
			jsonbody.put("id", functionRequestMessage.getId());

			// Add body
			jsonObject.put("body", jsonbody);

			return jsonObject.toString().getBytes();
			
		} else if (MessageType.FUNCTION_RESPONSE.equals(uniquidMessage.getMessageType())) {
			
			FunctionResponseMessage functionResponseMessage = (FunctionResponseMessage) uniquidMessage;
			
			final JSONObject jsonBody = new JSONObject();
			
			jsonBody.put("result", functionResponseMessage.getResult());
			jsonBody.put("error", functionResponseMessage.getError());
			jsonBody.put("id", functionResponseMessage.getId());

			final JSONObject jsonResponse = new JSONObject();

			jsonResponse.put("sender", functionResponseMessage.getProvider());
			
			jsonResponse.put("body", jsonBody);
			
			return jsonResponse.toString().getBytes();
			
		}
		
		throw new Exception("Unmanaged type!");
		
	}

	@Override
	public UniquidMessage deserialize(byte[] payload) throws Exception {
		
		String jsonString = new String(payload);
		
		final JSONObject jsonMessage = new JSONObject(jsonString);
		
		if (jsonMessage.has("sender")) {

			final String sender = jsonMessage.getString("sender");
	
			final JSONObject jsonBody = jsonMessage.getJSONObject("body");
	
			// IS Request?
			if (jsonBody.has("method")) {
				
				final int method = jsonBody.getInt("method");
				
				final String params = jsonBody.getString("params"); 
				
				final long id = jsonBody.getLong("id");
				
				FunctionRequestMessage requestMessage = new FunctionRequestMessage();
				
				requestMessage.setUser(sender);
				requestMessage.setMethod(method);
				requestMessage.setParameters(params);
				requestMessage.setId(id);
				
				return requestMessage;
				
			} else if (jsonBody.has("result")) {
				
				final String result = jsonBody.getString("result");
				
				final int error =jsonBody.getInt("error"); 
				
				final long id = jsonBody.getLong("id");
				
				FunctionResponseMessage responseMessage = new FunctionResponseMessage();
				
				responseMessage.setProvider(sender);
				responseMessage.setResult(result);
				responseMessage.setError(error);
				responseMessage.setId(id);
				
				return responseMessage;
				
			}
		
		} else if (jsonMessage.has("xpub")) {
			
			final String name = jsonMessage.getString("name");
			
			final String xpub = jsonMessage.getString("xpub");
			
			AnnounceMessage announceMessage = new AnnounceMessage();
			announceMessage.setName(name);
			announceMessage.setPublicKey(xpub);
			
			return announceMessage;
			
		}
		
		throw new Exception("Unknown message received!");
	}
	
	
	private static class AnnouncerSerializer {
		
		public byte[] serialize(AnnounceMessage announceMessage)  {
			
			// Create empty json object
			JSONObject jsonObject = new JSONObject();

			// populate sender
			jsonObject.put("name", announceMessage.getName());
			
			jsonObject.put("xpub", announceMessage.getPublicKey());

			return jsonObject.toString().getBytes();
			
		}
		
	}
	
	private static class FunctionRequestSerializer {
		
		public byte[] serialize(FunctionRequestMessage functionRequestMessage) {
			
			// Create empty json object
			JSONObject jsonObject = new JSONObject();

			// populate sender
			jsonObject.put("sender", functionRequestMessage.getUser());

			// Create empty json child
			JSONObject jsonbody = new JSONObject();

			// Put all keys inside body
			jsonbody.put("method", functionRequestMessage.getMethod());
			
			jsonbody.put("params", functionRequestMessage.getParameters());
			
			jsonbody.put("id", functionRequestMessage.getId());

			// Add body
			jsonObject.put("body", jsonbody);

			return jsonObject.toString().getBytes();
			
		}
		
	}
	
	private static class FunctionResponseSerializer {
		
		public byte[] serialize(FunctionResponseMessage functionResponseMessage) {
			
			final JSONObject jsonBody = new JSONObject();
			
			jsonBody.put("result", functionResponseMessage.getResult());
			jsonBody.put("error", functionResponseMessage.getError());
			jsonBody.put("id", functionResponseMessage.getId());

			final JSONObject jsonResponse = new JSONObject();

			jsonResponse.put("sender", functionResponseMessage.getProvider());
			
			jsonResponse.put("body", jsonBody);
			
			return jsonResponse.toString().getBytes();
			
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		
		String announce = "{ \"name\":\"ciccio\", \"xpub\":\"1234567\"}";
		
		String request = "{\"sender\":\"ciccio\", \"body\": { \"method\":33, \"params\": \"{}\", \"id\":123467 } }";
		
		String response = "{\"sender\":\"ciccio\", \"body\": { \"result\":\"\", \"error\": 0, \"id\":123456 } }";
		
		UniquidMessage announceMessage = new MQTTMessageSerializer().deserialize(announce.getBytes());
		
		UniquidMessage requestMessage = new MQTTMessageSerializer().deserialize(request.getBytes());
		
		UniquidMessage responseMessage = new MQTTMessageSerializer().deserialize(response.getBytes());
		
		
		String announce1 = new String(new MQTTMessageSerializer().serialize(announceMessage));
		String request1 = new String(new MQTTMessageSerializer().serialize(requestMessage));
		String response1 = new String(new MQTTMessageSerializer().serialize(responseMessage));
		
	}
	
}
