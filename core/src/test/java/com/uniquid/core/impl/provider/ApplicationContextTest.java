package com.uniquid.core.impl.provider;

import org.junit.Assert;
import org.junit.Test;

import com.uniquid.core.provider.impl.ApplicationContext;

public class ApplicationContextTest {

	@Test
	public void testConstructor() {
		
		ApplicationContext context = new ApplicationContext();
		
		Assert.assertNotNull(context);
		Assert.assertEquals("Uniquid Library", context.getServerInfo());
		Assert.assertEquals(null, context.getAttribute(null));
		Assert.assertNotNull(context.getAttributeNames());
		
	}
	
	@Test
	public void testAttribute() {
		
		ApplicationContext context = new ApplicationContext();
		
		try {
			
			context.setAttribute(null, null);
			Assert.fail();
			
		} catch (IllegalArgumentException ex) {

			Assert.assertEquals("name attribute is null", ex.getMessage());
			
		}

		Object o = new Object();

		context.setAttribute("test", o);
		
		Assert.assertEquals(o, context.getAttribute("test"));
		
		context.setAttribute("test", null);
		
		Assert.assertEquals(null, context.getAttribute("test"));
		
		context.setAttribute("test", o);
		
		Assert.assertEquals(o, context.getAttribute("test"));
		
		context.removeAttribute("test");
		
		Assert.assertEquals(null, context.getAttribute("test"));
		
		context.setAttribute("test", o);
		
		Assert.assertEquals(o, context.getAttribute("test"));
		
		context.setAttributeReadOnly("test");
		
		Assert.assertEquals(o, context.getAttribute("test"));
		
		context.removeAttribute("test");
		
		Assert.assertEquals(o, context.getAttribute("test"));
		
	}
	
}
