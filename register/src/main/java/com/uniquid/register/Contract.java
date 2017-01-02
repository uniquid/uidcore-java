package com.uniquid.register;

import org.json.JSONException;
import org.json.JSONObject;

public class Contract {

    private String context_name;
    private String user_name;
    private String machine_name;
    private long timestamp_born;
    private long timestamp_expiration;
    private String recipe;
    private String txid;

    public Contract(){
        // empty constructor
    }

    public Contract(String context_name, String user_name, String machine_name, long timestamp_born,
                    long timestamp_expiration, String recipe, String txid) {
        this.context_name = context_name;
        this.user_name = user_name;
        this.machine_name = machine_name;
        this.timestamp_born = timestamp_born;
        this.timestamp_expiration = timestamp_expiration;
        this.recipe = recipe;
        this.txid = txid;
    }

    public String getContext_name() {
        return context_name;
    }

    public void setContext_name(String context_name) {
        this.context_name = context_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getMachine_name() {
        return machine_name;
    }

    public void setMachine_name(String machine_name) {
        this.machine_name = machine_name;
    }

    public long getTimestamp_born() {
        return timestamp_born;
    }

    public void setTimestamp_born(long timestamp_born) {
        this.timestamp_born = timestamp_born;
    }

    public long getTimestamp_expiration() {
        return timestamp_expiration;
    }

    public void setTimestamp_expiration(long timestamp_expiration) {
        this.timestamp_expiration = timestamp_expiration;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getContractAsString(){
        return "context_name:" + context_name + ",user_name:" + user_name + "machine_name:" + machine_name +
                ",timestamp_born:" + timestamp_born + ",timestamp_expiration:" + timestamp_expiration +
                ",recipe:" + recipe + ",txid:" + txid;
    }

    public JSONObject getContractAsJson() throws JSONException {
        return new JSONObject(getContractAsString());
    }
}
