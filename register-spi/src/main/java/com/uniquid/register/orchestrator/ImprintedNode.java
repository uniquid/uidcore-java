package com.uniquid.register.orchestrator;

import java.util.Objects;

public class ImprintedNode implements Comparable<ImprintedNode> {
	
    private String xpub;
    private String name;
    private String owner;   // imprinter name
    private String txid;

    public ImprintedNode(){

    }

    public ImprintedNode(String xpub, String name, String owner, String txid) {
        this.xpub = xpub;
        this.name = name;
        this.owner = owner;
        this.txid = txid;
    }

    public String getXpub() {
        return xpub;
    }

    public void setXpub(String xpub) {
        this.xpub = xpub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTxid() {
        return txid;
    }

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
