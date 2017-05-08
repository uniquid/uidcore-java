package com.uniquid.register.orchestrator;

import java.util.Objects;

/**
 * Represents a Node: an entity capable to open a connection to the peer to peer network of the BlockChain and:
 * <ul>
 * <li>synchronize/update the local BlockChain files</li>
 * <li>listen, verify and re-broadcast valid Transactions inside the peer to peer network</li>
 * </ul>
 */
public class Node implements Comparable<Node> {

	private String name;
    private String xpub;
    private long timestamp;
    private String recipe;
    private String path;

    /**
     * Creates an empty instance
     */
    public Node() {
        // empty constructor
    }

    /**
     * Creates an instance from name, xpub, timestamp, recipe and path
     * @param machine_name the name
     * @param machine_xpub the xpub
     * @param timestamp the timestamp
     * @param recipe the recipe
     * @param path the path
     */
    public Node(String machine_name, String machine_xpub, long timestamp, String recipe, String path) {
        this.name = machine_name;
        this.xpub = machine_xpub;
        this.timestamp = timestamp;
        this.recipe = recipe;
        this.path = path;
    }

    /**
     * Returns the name
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the xpub
     * @return the xpub
     */
    public String getXpub() {
        return xpub;
    }

    /**
     * Set the xpub
     * @param xpub the xpub
     */
    public void setXpub(String xpub) {
        this.xpub = xpub;
    }

    /**
     * Returns the timestamp
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp
     * @param timestamp the timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the recipe
     * @return the recipe
     */
    public String getRecipe() {
        return recipe;
    }

    /**
     * Set the recipe
     * @param recipe the recipe
     */
    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    /**
     * Set the path
     * @param path the path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the path
     * @return the path
     */
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
