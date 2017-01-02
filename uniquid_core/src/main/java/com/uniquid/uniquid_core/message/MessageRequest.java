package com.uniquid.uniquid_core.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by beatriz on 11/17/2016 for Uniquid Inc..
 */

public class MessageRequest {

    private String sender;
    private int method;
    private String params;
    private long msg_id;

    public MessageRequest(){
        // empty constructor
    }

    public MessageRequest(String sender, int method, String params, long msg_id){
        this.sender = sender;
        this.method = method;
        this.params = params;
        this.msg_id = msg_id;
    }

    public MessageRequest(String message){
        try {
            JSONObject jMessage = new JSONObject(message);
            this.sender = jMessage.getString("sender");
            JSONObject jobj = new JSONObject(jMessage.getString("body"));
            this.method = jobj.getInt("method");
            this.msg_id = jobj.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public long getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(long msg_id) {
        this.msg_id = msg_id;
    }

    public String messageToString(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sender", sender);
            JSONObject body = new JSONObject();
            body.put("method", method);
            body.put("params", params);
            body.put("id", msg_id);
            jsonObject.put("body", body);
        } catch (JSONException e) {
            return "";
        }
        return jsonObject.toString().replace("\\", "");
    }

}
