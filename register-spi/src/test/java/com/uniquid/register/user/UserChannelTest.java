package com.uniquid.register.user;

import org.junit.Assert;

import org.junit.Test;

public class UserChannelTest {

	@Test
	public void testEmptyConstructor() {
		
		UserChannel userChannel = new UserChannel();
		
		Assert.assertEquals(userChannel.getProviderName(), null);
		Assert.assertEquals(userChannel.getProviderAddress(), null);
		Assert.assertEquals(userChannel.getUserAddress(), null);
		Assert.assertEquals(userChannel.getBitmask(), null);
		Assert.assertEquals(userChannel.getRevokeAddress(), null);
		Assert.assertEquals(userChannel.getRevokeTxId(), null);
		
	}
	
	@Test
	public void testProviderName() {
		
		UserChannel userChannel = new UserChannel();
		
		String test = "test";
		
		userChannel.setProviderName(test);
		
		Assert.assertEquals(test, userChannel.getProviderName());
		
	}
	
	@Test
	public void testProviderAddress() {
		
		UserChannel userChannel = new UserChannel();
		
		String test = "test";
		
		userChannel.setProviderAddress(test);
		
		Assert.assertEquals(test, userChannel.getProviderAddress());
		
	}
	
	@Test
	public void testUserAddress() {
		
		UserChannel userChannel = new UserChannel();
		
		String test = "test";
		
		userChannel.setUserAddress(test);
		
		Assert.assertEquals(test, userChannel.getUserAddress());
		
	}
	
	@Test
	public void testBitmask() {
		
		UserChannel userChannel = new UserChannel();
		
		String test = "test";
		
		userChannel.setBitmask(test);
		
		Assert.assertEquals(test, userChannel.getBitmask());
		
	}
	
	@Test
	public void testRevokeAddress() {
		
		UserChannel userChannel = new UserChannel();
		
		String test = "test";
		
		userChannel.setRevokeAddress(test);
		
		Assert.assertEquals(test, userChannel.getRevokeAddress());
		
	}
	
	@Test
	public void testRevokeTxId() {
		
		UserChannel userChannel = new UserChannel();
		
		String test = "test";
		
		userChannel.setRevokeTxId(test);
		
		Assert.assertEquals(test, userChannel.getRevokeTxId());
		
	}
	
	public void testEquals() {
		
		UserChannel userChannel1 = new UserChannel();
		
		UserChannel userChannel2 = new UserChannel();
		
		Assert.assertEquals(userChannel1, userChannel2);
		
	}

}
