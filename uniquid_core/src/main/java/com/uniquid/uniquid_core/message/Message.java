package com.uniquid.uniquid_core.message;

/**
 * Created by beatriz on 11/17/2016 for Uniquid Inc..
 */

public class Message {
    String sender;
    long msg_id;

    public Message(){
        // empty constructor
    }

    public Message(String sender, long msg_id) {
        this.sender = sender;
        this.msg_id = msg_id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(long msg_id) {
        this.msg_id = msg_id;
    }
}
