package com.uniquid.register.orchestrator;

import org.junit.Assert;
import org.junit.Test;

public class ContractTest {
	
	@Test
	public void testEmptyConstructor() {
		
		Contract contract = new Contract();
		
		Assert.assertEquals(null, contract.getContext());
		Assert.assertEquals(null, contract.getUser());
		Assert.assertEquals(null, contract.getProvider());
		Assert.assertEquals(0, contract.getTimestamp_born());
		Assert.assertEquals(0, contract.getTimestamp_expiration());
		Assert.assertEquals(null, contract.getRecipe());
		Assert.assertEquals(null, contract.getTxid());
		Assert.assertEquals(null, contract.getAnnulment());
		Assert.assertEquals(false, contract.isRevocated());
		
	}
	
	@Test
	public void testConstructor1() {
		
		Context context = new Context();
		
		Node user = new Node();
		
		Node provider = new Node();
		
		long born = System.currentTimeMillis();
		
		long expire = System.currentTimeMillis() + 1000;
		
		String recipe = "recipe";
		
		Contract contract = new Contract(context, user, provider, born, expire, recipe, recipe, recipe);
		
		Assert.assertEquals(context, contract.getContext());
		Assert.assertEquals(user, contract.getUser());
		Assert.assertEquals(provider, contract.getProvider());
		Assert.assertEquals(born, contract.getTimestamp_born());
		Assert.assertEquals(expire, contract.getTimestamp_expiration());
		Assert.assertEquals(recipe, contract.getRecipe());
		Assert.assertEquals(recipe, contract.getTxid());
		Assert.assertEquals(recipe, contract.getAnnulment());
		Assert.assertEquals(false, contract.isRevocated());
		
	}
	
	@Test
	public void testConstructor2() {
		
		Context context = new Context();
		
		Node user = new Node();
		
		Node provider = new Node();
		
		long born = System.currentTimeMillis();
		
		long expire = System.currentTimeMillis() + 1000;
		
		String recipe = "recipe";
		
		Contract contract = new Contract(context, user, provider, born, expire, recipe, recipe, recipe, true);
		
		Assert.assertEquals(context, contract.getContext());
		Assert.assertEquals(user, contract.getUser());
		Assert.assertEquals(provider, contract.getProvider());
		Assert.assertEquals(born, contract.getTimestamp_born());
		Assert.assertEquals(expire, contract.getTimestamp_expiration());
		Assert.assertEquals(recipe, contract.getRecipe());
		Assert.assertEquals(recipe, contract.getTxid());
		Assert.assertEquals(recipe, contract.getAnnulment());
		Assert.assertEquals(true, contract.isRevocated());
		
	}
	
	@Test
	public void testEquals() {
		
		Context context = new Context();
		
		Node user = new Node();
		
		Node provider = new Node();
		
		long born = System.currentTimeMillis();
		
		long expire = System.currentTimeMillis() + 1000;
		
		String recipe = "recipe";
		
		Contract contract = new Contract(context, user, provider, born, expire, recipe, recipe, recipe);

		Assert.assertEquals(true, contract.equals(contract));
		
		Contract contract2 = new Contract(context, user, provider, born, expire, recipe, recipe, recipe);
		
		Assert.assertEquals(true, contract.equals(contract2));
		
		Contract contract3 = new Contract(context, user, provider, born, expire, "other", recipe, recipe);
		
		Assert.assertEquals(false, contract.equals(contract3));
		
		Assert.assertEquals(false, contract.equals(null));
		
	}

}
