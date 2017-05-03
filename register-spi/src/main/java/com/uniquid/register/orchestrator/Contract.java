package com.uniquid.register.orchestrator;

import java.util.Objects;

public class Contract {
	
	private Context context;
    private Node user;
    private Node provider;
    private long timestamp_born;
    private long timestamp_expiration;
    private String recipe;
    private String txid;
    private String annulment;       // revoke address
    private boolean revocated;

    public Contract(){
        // empty constructor
    }

    public Contract(Context context, Node user, Node provider, long timestamp_born,
                    long timestamp_expiration, String recipe, String txid, String annulment) {
        this.context = context;
        this.user = user;
        this.provider = provider;
        this.timestamp_born = timestamp_born;
        this.timestamp_expiration = timestamp_expiration;
        this.recipe = recipe;
        this.txid = txid;
        this.annulment = annulment;
        this.revocated = false;
    }

    public Contract(Context context, Node user, Node provider, long timestamp_born,
                    long timestamp_expiration, String recipe, String txid, String annulment,
                    boolean revocated) {
        this.context = context;
        this.user = user;
        this.provider = provider;
        this.timestamp_born = timestamp_born;
        this.timestamp_expiration = timestamp_expiration;
        this.recipe = recipe;
        this.txid = txid;
        this.annulment = annulment;
        this.revocated = revocated;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Node getUser() {
        return user;
    }

    public void setUser(Node user) {
        this.user = user;
    }

    public Node getProvider() {
        return provider;
    }

    public void setProvider(Node provider) {
        this.provider = provider;
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

    public void setAnnulment(String annulment) {
        this.annulment = annulment;
    }

    public String getAnnulment() {
        return annulment;
    }

    public void setRevocated(boolean revocated){
        this.revocated = revocated;
    }

    public boolean isRevocated(){
        return revocated;
    }

    public String getContractAsString(){
        return "context_name:" + context.getName() + ",user_name:" + user.getName() +
                "machine_name:" + provider.getName() +
                ",timestamp_born:" + timestamp_born +
                ",timestamp_expiration:" + timestamp_expiration +
                ",recipe:" + recipe + ",txid:" + txid;
    }
//
//    public JSONObject getContractAsJson() throws JSONException {
//        return new JSONObject(getContractAsString());
//    }
    
    @Override
    public boolean equals(Object object) {
    
    	if (!(object instanceof Contract))
    		return false;

    	if (this == object)
    		return true;
    	
    	Contract contract = (Contract) object;
    	
    	return java.util.Objects.equals(context, contract.context) &&
    			java.util.Objects.equals(user, contract.user) &&
    			java.util.Objects.equals(provider, contract.provider) &&
    			timestamp_born == contract.timestamp_born &&
    			timestamp_expiration == contract.timestamp_expiration &&
    			java.util.Objects.equals(recipe, contract.recipe) &&
    			java.util.Objects.equals(txid, contract.txid) &&
    			java.util.Objects.equals(annulment, contract.annulment) &&
    			java.util.Objects.equals(revocated, contract.revocated);
    }
    
    @Override
    public int hashCode() {
    	
    	return Objects.hash(context, user, provider, timestamp_born, timestamp_expiration, recipe, txid, annulment, revocated);
    	
    }
    
}
