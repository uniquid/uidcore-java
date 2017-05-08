package com.uniquid.register.orchestrator;

import java.util.Objects;

/**
 * Represents a subset of entities that need to be grouped to reduce complexity of the Smart Contract Manager.
 */
public class Context implements Comparable<Context> {
	
	private String name;
    private String xpub;

    /**
     * Creates an empty instance
     */
    public Context() {
        // Empty constructor
    }

    /**
     * Creates an instance from name and xpub
     * 
     * @param name the context's name
     * @param xpub the context's xpub
     */
    public Context(String name, String xpub){
        this.name = name;
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

//    public String getContextAsString(){
//        try {
//            return getContextAsJson().toString();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public JSONObject getContextAsJson() throws JSONException {
//        JSONObject jobj = new JSONObject();
//        try {
//            jobj.put("name", name);
//            jobj.put("xpub", xpub);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return jobj;
//    }

    @Override
    public int compareTo(Context o) {
        return this.name.compareToIgnoreCase(o.getName());
    }
    
    @Override
    public boolean equals(Object object) {

    	if (!(object instanceof Context))
    		return false;
    	
    	if (this == object)
    		return true;
    	
    	Context context = (Context) object;
    	
    	return Objects.equals(name, context.name) &&
    			Objects.equals(xpub, context.xpub);
    }
    
    @Override
    public int hashCode() {
    	
    	return Objects.hash(name, xpub);
    
    }
    
}
