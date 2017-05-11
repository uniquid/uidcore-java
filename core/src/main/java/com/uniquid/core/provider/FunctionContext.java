package com.uniquid.core.provider;

import java.util.Enumeration;

/**
 * Defines a set of methods that a function uses to communicate with its function container, for example,
 * to get the MIME type of a file, dispatch requests, or write to a log file.
 */
public interface FunctionContext {

	/**
	 * Returns information about the Function container
	 * @return information about the Function container
	 */
	public String getServerInfo();
	
	/**
     * Returns the function container attribute with the given name, or
     * <code>null</code> if there is no attribute by that name. An attribute
     * allows a function container to give the function additional information not
     * already provided by this interface. See your server documentation for
     * information about its attributes. A list of supported attributes can be
     * retrieved using <code>getAttributeNames</code>.
     * <p>
     */
	public Object getAttribute(String name);
	
	/**
     * Returns an <code>Enumeration</code> containing the attribute names
     * available within this servlet context. Use the {@link #getAttribute}
     * method with an attribute name to get the value of an attribute.
     *
     * @return an <code>Enumeration</code> of attribute names
     * @see #getAttribute
     */
    public Enumeration<String> getAttributeNames();
    
    /**
    * Binds an object to a given attribute name in this servlet context. If the
    * name specified is already used for an attribute, this method will replace
    * the attribute with the new to the new attribute.
    * <p>
    * If listeners are configured on the <code>ServletContext</code> the
    * container notifies them accordingly.
    * <p>
    * If a null value is passed, the effect is the same as calling
    * <code>removeAttribute()</code>.
    * <p>
    * Attribute names should follow the same convention as package names. The
    * Java Servlet API specification reserves names matching
    * <code>java.*</code>, <code>javax.*</code>, and <code>sun.*</code>.
    *
    * @param name
    *            a <code>String</code> specifying the name of the attribute
    * @param object
    *            an <code>Object</code> representing the attribute to be bound
    * @throws NullPointerException If the provided attribute name is
    *         <code>null</code>
    */
   public void setAttribute(String name, Object object);
   
   /**
    * Removes the attribute with the given name from the servlet context. After
    * removal, subsequent calls to {@link #getAttribute} to retrieve the
    * attribute's value will return <code>null</code>.
    * <p>
    * If listeners are configured on the <code>ServletContext</code> the
    * container notifies them accordingly.
    *
    * @param name
    *            a <code>String</code> specifying the name of the attribute to
    *            be removed
    */
   public void removeAttribute(String name);
	
}
