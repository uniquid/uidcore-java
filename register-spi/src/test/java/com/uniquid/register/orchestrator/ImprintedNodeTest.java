package com.uniquid.register.orchestrator;

import org.junit.Assert;
import org.junit.Test;

public class ImprintedNodeTest {
	
	@Test
	public void testEmptyConstructor() {
		
		ImprintedNode imprintedNode = new ImprintedNode();
		
		Assert.assertEquals(null, imprintedNode.getXpub());
		Assert.assertEquals(null, imprintedNode.getName());
		Assert.assertEquals(null, imprintedNode.getOwner());
		Assert.assertEquals(null, imprintedNode.getTxid());
		
	}
	
	@Test
	public void testConstructor() {
		
		String test = "test";
		
		ImprintedNode imprintedNode = new ImprintedNode(test, test, test, test);
		
		Assert.assertEquals(test, imprintedNode.getXpub());
		Assert.assertEquals(test, imprintedNode.getName());
		Assert.assertEquals(test, imprintedNode.getOwner());
		Assert.assertEquals(test, imprintedNode.getTxid());
		
	}
	
	@Test
	public void testXpub() {
		
		ImprintedNode imprintedNode = new ImprintedNode();
		
		Assert.assertEquals(null, imprintedNode.getXpub());
		
		String test = "test";
		
		imprintedNode.setXpub(test);
		
		Assert.assertEquals(test, imprintedNode.getXpub());
		
	}
	
	@Test
	public void testName() {
		
		ImprintedNode imprintedNode = new ImprintedNode();
		
		Assert.assertEquals(null, imprintedNode.getName());
		
		String test = "test";
		
		imprintedNode.setName(test);
		
		Assert.assertEquals(test, imprintedNode.getName());
		
	}
	
	@Test
	public void testOwner() {
		
		ImprintedNode imprintedNode = new ImprintedNode();
		
		Assert.assertEquals(null, imprintedNode.getOwner());
		
		String test = "test";
		
		imprintedNode.setOwner(test);
		
		Assert.assertEquals(test, imprintedNode.getOwner());
		
	}
	
	@Test
	public void testTxId() {
		
		ImprintedNode imprintedNode = new ImprintedNode();
		
		Assert.assertEquals(null, imprintedNode.getTxid());
		
		String test = "test";
		
		imprintedNode.setTxid(test);
		
		Assert.assertEquals(test, imprintedNode.getTxid());
		
	}

}
