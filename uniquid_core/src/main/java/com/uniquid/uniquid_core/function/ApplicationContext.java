package com.uniquid.uniquid_core.function;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext implements FunctionContext {

	/**
     * The context attributes for this context.
     */
    protected Map<String,Object> attributes = new ConcurrentHashMap<>();

    /**
     * List of read only attributes for this context.
     */
    private final Map<String,String> readOnlyAttributes = new ConcurrentHashMap<>();
	
	@Override
	public String getServerInfo() {
		return "Uniquid Library";
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		Set<String> names = new HashSet<>();
        names.addAll(attributes.keySet());
        return Collections.enumeration(names);
	}

	@Override
	public void setAttribute(String name, Object value) {
		if (name == null)
            throw new IllegalArgumentException
                ("name attribute is null");
		
		// Null value is the same as removeAttribute()
        if (value == null) {
            removeAttribute(name);
            return;
        }
        
	    // Add or replace the specified attribute
	    // Check for read only attribute
	    if (readOnlyAttributes.containsKey(name))
	        return;
        
	}

	@Override
	public void removeAttribute(String name) {
		Object value = null;

        // Remove the specified attribute
        // Check for read only attribute
        if (readOnlyAttributes.containsKey(name)){
            return;
        }
        value = attributes.remove(name);
        if (value == null) {
            return;
        }
	}
	
	/**
     * Set an attribute as read only.
     */
    public void setAttributeReadOnly(String name) {

        if (attributes.containsKey(name))
            readOnlyAttributes.put(name, name);

    }

}
