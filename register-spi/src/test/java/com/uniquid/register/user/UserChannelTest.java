package com.uniquid.register.user;

import org.junit.Assert;
import org.junit.Test;

public class UserChannelTest {

	@Test
	public void testEmptyConstructor() {
		
		UserChannel userChannel = new UserChannel();
		
		Assert.assertEquals(null, userChannel.getProviderName());
		Assert.assertEquals(null, userChannel.getProviderAddress());
		Assert.assertEquals(null, userChannel.getUserAddress());
		Assert.assertEquals(null, userChannel.getBitmask());
		Assert.assertEquals(null, userChannel.getRevokeAddress());
		Assert.assertEquals(null, userChannel.getRevokeTxId());
		
	}

	@Test
	public void testConstructor() {
		
		String providerName = "providerName";
		String providerAddress = "providerAddress";
		String userAddress = "userAddress";
		String bitmask = "bitmask";
		
		UserChannel userChannel = new UserChannel(providerName, providerAddress, userAddress, bitmask);
		
		Assert.assertEquals(providerName, userChannel.getProviderName());
		Assert.assertEquals(providerAddress, userChannel.getProviderAddress());
		Assert.assertEquals(userAddress, userChannel.getUserAddress());
		Assert.assertEquals(bitmask, userChannel.getBitmask());
		Assert.assertEquals(null, userChannel.getRevokeAddress());
		Assert.assertEquals(null, userChannel.getRevokeTxId());
		
		Assert.assertEquals("provider address: providerAddress; user address: userAddress; bitmask: bitmask; revoke address: null; revokeTxId: null", userChannel.toString());
		Assert.assertEquals(-763790120, userChannel.hashCode());
		
	}
	
	@Test
	public void testProviderName() {
		
		UserChannel userChannel = new UserChannel();
		
		Assert.assertEquals(null, userChannel.getProviderName());
		
		String providerName = "providerName";
		
		userChannel.setProviderName(providerName);
		
		Assert.assertEquals(providerName, userChannel.getProviderName());
		
	}
	
	@Test
	public void testProviderAddress() {
		
		UserChannel userChannel = new UserChannel();
		
		Assert.assertEquals(null, userChannel.getProviderAddress());
		
		String providerAddress = "providerAddress";
		
		userChannel.setProviderAddress(providerAddress);
		
		Assert.assertEquals(providerAddress, userChannel.getProviderAddress());
		
	}
	
	@Test
	public void testUserAddress() {
		
		UserChannel userChannel = new UserChannel();
		
		Assert.assertEquals(null, userChannel.getUserAddress());
		
		String userAddress = "userAddress";
		
		userChannel.setUserAddress(userAddress);
		
		Assert.assertEquals(userAddress, userChannel.getUserAddress());
		
	}
	
	@Test
	public void testBitmask() {
		
		UserChannel userChannel = new UserChannel();
		
		Assert.assertEquals(null, userChannel.getBitmask());
		
		String bitmask = "bitmask";
		
		userChannel.setBitmask(bitmask);
		
		Assert.assertEquals(bitmask, userChannel.getBitmask());
		
	}
	
	@Test
	public void testRevokeAddress() {
		
		UserChannel userChannel = new UserChannel();
		
		Assert.assertEquals(null, userChannel.getRevokeAddress());
		
		String revokeAddress = "revokeAddress";
		
		userChannel.setRevokeAddress(revokeAddress);
		
		Assert.assertEquals(revokeAddress, userChannel.getRevokeAddress());
		
	}
	
	@Test
	public void testRevokeTxId() {
		
		UserChannel userChannel = new UserChannel();
		
		Assert.assertEquals(null, userChannel.getRevokeTxId());
		
		String revokeTxid = "revokeTxid";
		
		userChannel.setRevokeTxId(revokeTxid);
		
		Assert.assertEquals(revokeTxid, userChannel.getRevokeTxId());
		
	}
	
	@Test
	public void testEquals() {
		
		UserChannel userChannel1 = new UserChannel();
		
		UserChannel userChannel2 = new UserChannel();
		
		Assert.assertEquals(true, userChannel1.equals(userChannel2));
		
		userChannel2.setProviderName("other");
		
		Assert.assertEquals(false, userChannel1.equals(userChannel2));
		
		Assert.assertEquals(false, userChannel1.equals(null));
		
		Assert.assertEquals(true, userChannel1.equals(userChannel1));
		
	}
	
	@Test
	public void testCompareTo() {
		
		UserChannel userChannel1 = new UserChannel();
		
		UserChannel userChannel2 = new UserChannel();
		
		Assert.assertEquals(0, userChannel1.compareTo(userChannel1));
		
		Assert.assertEquals(0, userChannel1.compareTo(userChannel2));
		
	}
	
}
