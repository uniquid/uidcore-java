package com.uniquid.uniquid_core.message;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

//Request:
// {
//  "sender":"id",
//  "body": {
//     "method":22,
//     "params":"{}",
//     "id":12344
//  }
// }
// Response
//{
// "sender":"id",
// "body": {
//     "result":"",
//     "error":0,
//     "id":1234
// }
//}
//
//
class Message {

	private String sender;
	private Map<String, Object> body;

	Message() {
		// empty constructor
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public Map<String, Object> getBody() {
		return body;
	}

	public void setBody(Map<String, Object> body) {
		this.body = body;
	}

	public String toJSON() {

		// Create empty json object
		JSONObject jsonObject = new JSONObject();

		// populate sender
		jsonObject.put("sender", sender);

		// Create empty json child
		JSONObject jsonbody = new JSONObject();

		// Put all keys inside body
		for (Iterator<String> iterator = body.keySet().iterator(); iterator.hasNext() ; ) {

			String key = (String) iterator.next();

			jsonbody.put(key, body.get(key));

		}

		// Add body
		jsonObject.put("body", jsonbody);

		return jsonObject.toString();
	}
	
	public static Message fromJsonString(String string) throws JSONException {

		Message message = new Message();

		JSONObject jsonMessage = new JSONObject(string);

		message.setSender(jsonMessage.getString("sender"));

		JSONObject jsonBody = jsonMessage.getJSONObject("body");

		message.setBody(jsonBody.toMap());

		return message;

	}

}
