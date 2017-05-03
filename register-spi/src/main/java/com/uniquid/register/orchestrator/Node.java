package com.uniquid.register.orchestrator;

import java.util.Objects;

public class Node implements Comparable<Node>{

	private String name;
    private String xpub;
    private long timestamp;
    private String recipe;
    private String path;

    public Node(){
        // empty constructor
    }

    public Node(String machine_name, String machine_xpub, long timestamp, String recipe, String path) {
        this.name = machine_name;
        this.xpub = machine_xpub;
        this.timestamp = timestamp;
        this.recipe = recipe;
        this.path = path;
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

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object object) {
    
    	if (!(object instanceof Node))
    		return false;

    	if (this == object)
    		return true;
    	
    	Node node = (Node) object;
    	
    	return Objects.equals(name, node.name) &&
    			Objects.equals(xpub, node.xpub) &&
    			timestamp == node.timestamp &&
    			Objects.equals(recipe, node.recipe) &&
    			Objects.equals(path, node.path);
    }

    @Override
    public int hashCode() {
    	
        return Objects.hash(name, xpub, timestamp, recipe, path);
    
    }

    @Override
    public int compareTo(Node o) {
        return this.name.compareToIgnoreCase(o.getName());
    }

}
