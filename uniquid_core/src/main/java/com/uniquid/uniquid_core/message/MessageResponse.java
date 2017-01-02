package com.uniquid.uniquid_core.message;

import com.google.gson.JsonObject;

public class MessageResponse extends Message {
    String result;
    int error;

    String receiver;

    /*{
        "sender":"base58",
        "body":{
            "result":"",
            "error"; ,
            "id":
        }

    }*/

    public MessageResponse(){

    }

    public MessageResponse(String sender, String result, int error, long id){
        super(sender, id);
        this.result = result;
        this.error = error;
    }

    public String createRegistryMsgResponse(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("sender", super.getSender());
        JsonObject body = new JsonObject();
        body.addProperty("result", result);
        body.addProperty("error", error);
        body.addProperty("id", super.getMsg_id());
        jsonObject.add("body", body);

        return jsonObject.toString();
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setError(int error){
        this.error = error;
    }

    public String getSender() {
        return super.getSender();
    }

    public long getId() {
        return super.getMsg_id();
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiver() {
        return receiver;
    }

}
