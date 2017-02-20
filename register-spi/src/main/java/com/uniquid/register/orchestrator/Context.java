package com.uniquid.register.orchestrator;

import org.json.JSONException;
import org.json.JSONObject;

public class Context implements Comparable<Context> {
	
	private String name;
    private String xpub;

    public Context(){
        // Empty constructor
    }

    public Context(String name, String xpub){
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

    public String getContextAsString(){
        try {
            return getContextAsJson().toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getContextAsJson() throws JSONException {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("name", name);
            jobj.put("xpub", xpub);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jobj;
    }

    @Override
    public int compareTo(Context o) {
        return this.name.compareToIgnoreCase(o.getName());
    }

}
