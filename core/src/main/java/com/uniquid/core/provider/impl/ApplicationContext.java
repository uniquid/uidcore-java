package com.uniquid.core.provider.impl;

import com.uniquid.core.provider.FunctionContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link FunctionContext}
 */
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
        if (name == null) return null;

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

        // save attribute
        attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        Object value;

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
