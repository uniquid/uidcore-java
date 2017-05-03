package com.uniquid.register.orchestrator;

import org.junit.Assert;
import org.junit.Test;

public class ContextText {
	
	@Test
	public void testEmptyConstructor() {
		
		Context context = new Context();
		
		Assert.assertEquals(context.getName(), null);
		Assert.assertEquals(context.getXpub(), null);
		
	}
	
	@Test
	public void testConstructor() {
		
		String name = "name";
		String xpub = "xpub";
		
		Context context = new Context(name, xpub);
		
		Assert.assertEquals(name, context.getName());
		Assert.assertEquals(xpub, context.getXpub());
		
	}
	
	@Test
	public void testName() {
		
		String name = "name";
		
		Context context = new Context();
		
		Assert.assertEquals(null, context.getName());
		
		context.setName(name);
		
		Assert.assertEquals(name, context.getName());
		
	}

	@Test
	public void testXpub() {
		
		String xpub = "xpub";
		
		Context context = new Context();
		
		Assert.assertEquals(null, context.getXpub());
		
		context.setXpub(xpub);
		
		Assert.assertEquals(xpub, context.getXpub());
		
	}
	
	@Test
	public void testEquals() {
		
		String name = "name";
		String xpub = "xpub";
		
		Context context = new Context(name, xpub);
		
		Assert.assertEquals(true, context.equals(context));
		
		Context context2 = new Context(name, xpub);
		
		Assert.assertEquals(true, context.equals(context2));
		
		Context context3 = new Context("other", xpub);
		
		Assert.assertEquals(false, context.equals(context3));
		
	}

}
