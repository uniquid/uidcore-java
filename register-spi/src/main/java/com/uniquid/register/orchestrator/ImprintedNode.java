package com.uniquid.register.orchestrator;

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

}
