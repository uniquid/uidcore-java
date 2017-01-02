package com.uniquid.register;

import org.json.JSONException;
import org.json.JSONObject;

public class UContext {
    String name;
    String xpub;

    public UContext(){
        // Empty constructor
    }

    public UContext(String name, String xpub){
        this.name = name;
        this.xpub = xpub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXpub() {
        return xpub;
    }

    public void setXpub(String xpub) {
        this.xpub = xpub;
    }

    public String getUContextAsString(){
        try {
            return getUContextAsJson().toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getUContextAsJson() throws JSONException {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("name", name);
            jobj.put("xpub", xpub);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jobj;
    }
}
