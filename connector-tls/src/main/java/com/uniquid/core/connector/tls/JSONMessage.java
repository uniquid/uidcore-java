package com.uniquid.core.connector.tls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
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

	public String toJSONString() {

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

		message.setBody(toMap(jsonBody));

		return message;

	}
	
	private static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap();
        Iterator keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)));
        }
        return map;
    }

	private static List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

	private static Object fromJson(Object json) throws JSONException {
        if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

}