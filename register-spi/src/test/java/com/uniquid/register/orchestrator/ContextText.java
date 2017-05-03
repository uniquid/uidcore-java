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
		
		String test = "";
		
		Context context = new Context(test, test);
		
		Assert.assertEquals(context.getName(), test);
		Assert.assertEquals(context.getXpub(), test);
		
	}
	
	public void testName() {
		
		String test = "";
		
		Context context = new Context();
		
		context.setName(test);
		
		Assert.assertEquals(context.getName(), test);
		
	}

	public void testXpub() {
		
		String test = "";
		
		Context context = new Context();
		
		context.setXpub(test);
		
		Assert.assertEquals(context.getXpub(), test);
		
	}

}
