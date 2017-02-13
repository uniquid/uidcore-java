package com.uniquid.core.connector.mqtt;

import java.util.HashMap;
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
public class JSONMessage {

	private String sender;
	private Map<String, Object> body;

	public JSONMessage() {
		sender = "";
		body = new HashMap<String, Object>();
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
	
	public static JSONMessage fromJsonString(String string) throws JSONException {

		JSONMessage message = new JSONMessage();

		JSONObject jsonMessage = new JSONObject(string);

		message.setSender(jsonMessage.getString("sender"));

		JSONObject jsonBody = jsonMessage.getJSONObject("body");

		message.setBody(jsonBody.toMap());

		return message;

	}

}
