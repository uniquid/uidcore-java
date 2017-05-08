package com.uniquid.register.orchestrator;

import java.util.Objects;

/**
 * Represents an imprinted node: a node just enrolled in the system
 */
public class ImprintedNode implements Comparable<ImprintedNode> {
	
    private String xpub;
    private String name;
    private String owner;   // imprinter name
    private String txid;

    /**
     * Creates an empty instance
     */
    public ImprintedNode() {
    	// NOTHING TO DO
    }

    /**
     * Creates an instance from xpub, name, owner and transaction id
     * 
     * @param xpub the xpub
     * @param name the name
     * @param owner the owner
     * @param txid the transaction id
     */
    public ImprintedNode(String xpub, String name, String owner, String txid) {
        this.xpub = xpub;
        this.name = name;
        this.owner = owner;
        this.txid = txid;
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
     * @param xpub the xub
     */
    public void setXpub(String xpub) {
        this.xpub = xpub;
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
     * Returns the owner
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Set the owner
     * @param owner the owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Returns the transaction id
     * @return the transaction id
     */
    public String getTxid() {
        return txid;
    }

    /**
     * Set the transaction id
     * @param txid the transaction id
     */
    public void setTxid(String txid) {
        this.txid = txid;
    }

    @Override
    public int compareTo(ImprintedNode o) {
        return this.name.compareToIgnoreCase(o.getName());
    }
    
    @Override
    public boolean equals(Object object) {
    
    	if (!(object instanceof ImprintedNode))
    		return false;

    	if (this == object)
    		return true;
    	
    	ImprintedNode imprintedNode = (ImprintedNode) object;
    	
    	return Objects.equals(xpub, imprintedNode.xpub) &&
    			Objects.equals(name, imprintedNode.name) &&
    			Objects.equals(owner, imprintedNode.owner) &&
    			Objects.equals(txid, imprintedNode.txid);
    	
    }
    
    @Override
    public int hashCode() {
    	
    	return Objects.hash(xpub, name, owner, txid);
    	
    }

}
