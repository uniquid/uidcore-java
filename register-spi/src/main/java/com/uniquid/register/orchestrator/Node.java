package com.uniquid.register.orchestrator;

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
    public boolean equals(Object object){
        if(object == null)
                return false;
        if(object == this)
            return true;
        if(!(object instanceof Node))
            return false;
        Node n = (Node) object;
        return n.getXpub().equals(this.xpub);
    }

    @Override
    public int hashCode() {
        return xpub.hashCode();
    }

    @Override
    public int compareTo(Node o) {
        return this.name.compareToIgnoreCase(o.getName());
    }

}
