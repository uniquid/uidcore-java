package com.uniquid.register.orchestrator;

import java.util.Objects;

/**
 * Represents a Smart Contract: functional relationship between two or more entities, compatible with the BlockChain
 * and its cryptographic rules
 */
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

    /**
     * Creates an empty instance
     */
    public Contract() {
        // empty constructor
    }

    /**
     * Creates an instance from context, user, provider, timestamp born, timestamp expiration, recipe, transaction id
     * and revoker address
     * @param context the context
     * @param user the user
     * @param provider the provider
     * @param timestamp_born the timestamp of creation
     * @param timestamp_expiration the expiration
     * @param recipe the recipe
     * @param txid the transaction id
     * @param annulment the revoker
     */
    public Contract(Context context, Node user, Node provider, long timestamp_born,
                    long timestamp_expiration, String recipe, String txid, String annulment) {
    	this(context, user, provider, timestamp_born, timestamp_expiration, recipe, txid, annulment, false);
    }

    /**
     * Creates an instance from context, user, provider, timestamp born, timestamp expiration, recipe, transaction id,
     * revoker address and revocated state
     * @param context the context
     * @param user the user
     * @param provider the provider
     * @param timestamp_born the timestamp of creation
     * @param timestamp_expiration the expiration
     * @param recipe the recipe
     * @param txid the transaction id
     * @param annulment the revoker
     * @param revocated if is revocated
     */
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

    /**
     * Return the context
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Set the context
     * @param context the context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Return the user
     * @return the user
     */
    public Node getUser() {
        return user;
    }

    /**
     * Set the user
     * @param user the user
     */
    public void setUser(Node user) {
        this.user = user;
    }

    /**
     * Returns the provider
     * @return the provider
     */
    public Node getProvider() {
        return provider;
    }

    /**
     * Set the provider
     * @param provider the provider
     */
    public void setProvider(Node provider) {
        this.provider = provider;
    }

    /**
     * Return the timestamp of born
     * @return the timestamp of born
     */
    public long getTimestamp_born() {
        return timestamp_born;
    }

    /**
     * Set the timestamp of born
     * @param timestamp_born the timestamp of born
     */
    public void setTimestamp_born(long timestamp_born) {
        this.timestamp_born = timestamp_born;
    }

    /**
     * Returns the timestamp of expiration
     * @return timestamp of expiration
     */
    public long getTimestamp_expiration() {
        return timestamp_expiration;
    }

    /**
     * Set the timestamp of expiration
     * @param timestamp_expiration the timestamp of expiration
     */
    public void setTimestamp_expiration(long timestamp_expiration) {
        this.timestamp_expiration = timestamp_expiration;
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
     * Returns the transaction id
     * @return the transaction id
     */
    public String getTxid() {
        return txid;
    }

    /**
     * Set the transaction id
     * @param txid transaction id
     */
    public void setTxid(String txid) {
        this.txid = txid;
    }

    /**
     * Set the revoker address
     * @param annulment the revoker address
     */
    public void setAnnulment(String annulment) {
        this.annulment = annulment;
    }

    /**
     * Returns the revoker address
     * @return the revoker address
     */
    public String getAnnulment() {
        return annulment;
    }

    /**
     * Set the revocation status
     * @param revocated the revocation status
     */
    public void setRevocated(boolean revocated){
        this.revocated = revocated;
    }

    /**
     * Return the revocation status
     * @return the revocation status
     */
    public boolean isRevocated(){
        return revocated;
    }

    @Override
    public String toString(){
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
