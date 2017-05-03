package com.uniquid.register.orchestrator;

import org.junit.Assert;
import org.junit.Test;

public class NodeTest {
	
	@Test
	public void testEmptyContructor() {
		
		Node node = new Node();
		
		Assert.assertEquals(null, node.getName());
		Assert.assertEquals(null, node.getXpub());
		Assert.assertEquals(0, node.getTimestamp());
		Assert.assertEquals(null, node.getRecipe());
		Assert.assertEquals(null, node.getPath());
	
	}
	
	@Test
	public void testConstructor() {
		
		String name = "name";
		String xpub = "xpub";
		String recipe = "recipe";
		String path = "path";
		
		Node node = new Node(name, xpub, 0, recipe, path);
		
		Assert.assertEquals(name, node.getName());
		Assert.assertEquals(xpub, node.getXpub());
		Assert.assertEquals(0, node.getTimestamp());
		Assert.assertEquals(recipe, node.getRecipe());
		Assert.assertEquals(path, node.getPath());
		
	}
	
	@Test
	public void testName() {
		
		Node node = new Node();
		
		Assert.assertEquals(null, node.getName());
		
		String test = "";
		
		node.setName(test);
		
		Assert.assertEquals(test, node.getName());
		
	}
	
	@Test
	public void testXpub() {
		
		Node node = new Node();
		
		Assert.assertEquals(null, node.getXpub());
		
		String test = "";
		
		node.setXpub(test);
		
		Assert.assertEquals(test, node.getXpub());
		
	}
	
	@Test
	public void testTimeStamp() {
		
		Node node = new Node();
		
		Assert.assertEquals(0, node.getTimestamp());
		
		long timestamp = System.currentTimeMillis();
		
		node.setTimestamp(timestamp);
		
		Assert.assertEquals(timestamp, node.getTimestamp());
		
	}
	
	@Test
	public void testRecipe() {
		
		Node node = new Node();
		
		Assert.assertEquals(null, node.getRecipe());
		
		String test = "";
		
		node.setRecipe(test);
		
		Assert.assertEquals(test, node.getRecipe());
		
	}
	
	@Test
	public void testPath() {
		
		Node node = new Node();
		
		Assert.assertEquals(null, node.getPath());
		
		String test = "";
		
		node.setPath(test);
		
		Assert.assertEquals(test, node.getPath());
		
	}
	
	@Test
	public void testEquals() {
		
		String name = "name";
		String xpub = "xpub";
		String recipe = "recipe";
		String path = "path";
		
		Node node = new Node(name, xpub, 0, recipe, path);
		
		Assert.assertEquals(true, node.equals(node));
		
		Assert.assertEquals(false, node.equals(null));
		
		Assert.assertEquals(false, node.equals("String"));
		
		Node node2 = new Node(name, xpub, 0, recipe, path);
		
		Assert.assertEquals(true, node.equals(node2));
		
		Node node3 = new Node("name2", xpub, 0, recipe, path);
		
		Assert.assertEquals(false, node.equals(node3));
		
	}

}
