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
		
		String xpub = "xpub";
		String name = "name";
		String owner = "owner";
		String txid = "txid";
		
		ImprintedNode imprintedNode = new ImprintedNode(xpub, name, owner, txid);
		
		Assert.assertEquals(xpub, imprintedNode.getXpub());
		Assert.assertEquals(name, imprintedNode.getName());
		Assert.assertEquals(owner, imprintedNode.getOwner());
		Assert.assertEquals(txid, imprintedNode.getTxid());
		
	}
	
	@Test
	public void testXpub() {
		
		ImprintedNode imprintedNode = new ImprintedNode();
		
		Assert.assertEquals(null, imprintedNode.getXpub());
		
		String xpub = "xpub";
		
		imprintedNode.setXpub(xpub);
		
		Assert.assertEquals(xpub, imprintedNode.getXpub());
		
	}
	
	@Test
	public void testName() {
		
		ImprintedNode imprintedNode = new ImprintedNode();
		
		Assert.assertEquals(null, imprintedNode.getName());
		
		String name = "name";
		
		imprintedNode.setName(name);
		
		Assert.assertEquals(name, imprintedNode.getName());
		
	}
	
	@Test
	public void testOwner() {
		
		ImprintedNode imprintedNode = new ImprintedNode();
		
		Assert.assertEquals(null, imprintedNode.getOwner());
		
		String owner = "owner";
		
		imprintedNode.setOwner(owner);
		
		Assert.assertEquals(owner, imprintedNode.getOwner());
		
	}
	
	@Test
	public void testTxId() {
		
		ImprintedNode imprintedNode = new ImprintedNode();
		
		Assert.assertEquals(null, imprintedNode.getTxid());
		
		String txid = "txid";
		
		imprintedNode.setTxid(txid);
		
		Assert.assertEquals(txid, imprintedNode.getTxid());
		
	}

	@Test
	public void testEquals() {
		
		String xpub = "xpub";
		String name = "name";
		String owner = "owner";
		String txid = "txid";
		
		ImprintedNode imprintedNode = new ImprintedNode(xpub, name, owner, txid);
		
		Assert.assertEquals(true, imprintedNode.equals(imprintedNode));
		
		ImprintedNode imprintedNode2 = new ImprintedNode(xpub, name, owner, txid);
		
		Assert.assertEquals(true, imprintedNode.equals(imprintedNode2));
		
		ImprintedNode imprintedNode3 = new ImprintedNode(xpub, "other", owner, txid);
		
		Assert.assertEquals(false, imprintedNode.equals(imprintedNode3));
		
		Assert.assertEquals(false, imprintedNode.equals(null));
		
		Assert.assertEquals(391503859, imprintedNode.hashCode());
		
	}
	
	@Test
	public void testCompareTo() {
		
		String xpub = "xpub";
		String name = "name0";
		String owner = "owner";
		String txid = "txid";
		
		ImprintedNode imprintedNode = new ImprintedNode(xpub, name, owner, txid);
		
		String xpub2 = "xpub";
		String name2 = "name2";
		String owner2 = "owner";
		String txid2 = "txid";
		
		ImprintedNode imprintedNode2 = new ImprintedNode(xpub2, name2, owner2, txid2);
		
		Assert.assertTrue(imprintedNode.compareTo(imprintedNode2) < 0);
		
		Assert.assertTrue(imprintedNode.compareTo(imprintedNode) == 0);
		
		Assert.assertTrue(imprintedNode2.compareTo(imprintedNode) > 0);
	}

}
