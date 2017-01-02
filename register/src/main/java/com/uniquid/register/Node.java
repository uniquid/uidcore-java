package com.uniquid.register;

public class Node {
    private String context_name;
    private String machine_name;
    private String machine_xpub;
    private long timestamp;
    private String recipe;

    public Node(){
        // empty contrusctor
    }

    public Node(String context_name, String machine_name, String machine_xpub, long timestamp, String recipe) {
        this.context_name = context_name;
        this.machine_name = machine_name;
        this.machine_xpub = machine_xpub;
        this.timestamp = timestamp;
        this.recipe = recipe;
    }

    public String getContext_name() {
        return context_name;
    }

    public void setContext_name(String context_name) {
        this.context_name = context_name;
    }

    public String getMachine_name() {
        return machine_name;
    }

    public void setMachine_name(String machine_name) {
        this.machine_name = machine_name;
    }

    public String getMachine_xpub() {
        return machine_xpub;
    }

    public void setMachine_xpub(String machine_xpub) {
        this.machine_xpub = machine_xpub;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }
}
