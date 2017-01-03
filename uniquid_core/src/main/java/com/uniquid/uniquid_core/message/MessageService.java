package com.uniquid.uniquid_core.message;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class.getName());
	
//	protected int parseReqMessage(String payload, MessageRequest messageRequest) {
//		
////		return parseReqMessage(payload.getBytes(), messageRequest);
//		return 0;
//		
//	}

	/**
	 * Parse a message into a MessageRequest
	 * 
	 * @param message
	 *            message to parse
	 * @param messageRequest
	 *            message parsed
	 */
//	protected int parseReqMessage(byte[] message, MessageRequest messageRequest) {
//
//		String m = new String(message, StandardCharsets.UTF_8);
//
//		try {
//
//			JSONObject jMessage = new JSONObject(m);
//			String address = jMessage.getString("sender");
//			messageRequest.setSender(address);
//			JSONObject body = jMessage.getJSONObject("body");
//
//			if (isRpcReq(body)) {
//
//				messageRequest.setMethod(body.getInt("method"));
//				String params = body.getString("params");
//				messageRequest.setParams(params);
//				messageRequest.setMsg_id(body.getLong("id"));
//
//			} else {
//
//				return -1;
//
//			}
//
//		} catch (JSONException ex) {
//
//			LOGGER.error("Exception during JSON manipulation", ex);
//			return -1;
//
//		}
//
//		return 0;
//	}
	
	/**
	 * Check if {@param json} is a JSON-RPC request
	 * 
	 * @return true if the string is a rpc request, false otherwise
	 */
	private boolean isRpcReq(JSONObject json) {
		try {
			return json.has("method") && json.has("params") && json.has("id");
		} catch (JSONException e) {
			return false;
		}
	}
	
	public abstract MessageRequest receiveRequest();
	
	public abstract void sendResponse(MessageResponse messageResponse);

}
