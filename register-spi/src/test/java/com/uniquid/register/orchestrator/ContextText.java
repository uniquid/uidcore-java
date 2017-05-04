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
		
		Assert.assertEquals(false, context.equals(null));
	}
	
	@Test
	public void testCompareTo() {
		
		String name0 = "name0";
		String xpub0 = "xpub0";
		
		Context context = new Context(name0, xpub0);
		
		String name1 = "name1";
		String xpub1 = "xpub1";
		
		Context context2 = new Context(name1, xpub1);
		
		Assert.assertTrue(context.compareTo(context2) < 0);
		
		Assert.assertTrue(context.compareTo(context) == 0);
		
		Assert.assertTrue(context2.compareTo(context) > 0);
		
	}

}
