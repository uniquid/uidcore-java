package com.uniquid.register.provider;

import org.junit.Assert;
import org.junit.Test;

public class ProviderChannelTest {
	
	@Test
	public void testEmptyConstructor() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		Assert.assertEquals(null, providerChannel.getProviderAddress());
		Assert.assertEquals(null, providerChannel.getUserAddress());
		Assert.assertEquals(null, providerChannel.getBitmask());
		Assert.assertEquals(null, providerChannel.getRevokeAddress());
		Assert.assertEquals(null, providerChannel.getRevokeTxId());
		
	}
	
	@Test
	public void testContructor() {
		
		String providerAddress = "providerAddress";
		String userAddress = "userAddress";
		String bitmask = "bitmask";
		
		ProviderChannel providerChannel = new ProviderChannel(providerAddress, userAddress, bitmask);
		
		Assert.assertEquals(providerAddress, providerChannel.getProviderAddress());
		Assert.assertEquals(userAddress, providerChannel.getUserAddress());
		Assert.assertEquals(bitmask, providerChannel.getBitmask());
		Assert.assertEquals(null, providerChannel.getRevokeAddress());
		Assert.assertEquals(null, providerChannel.getRevokeTxId());
		
		Assert.assertEquals("provider address: providerAddress; user address: userAddress; bitmask: bitmask; revoke address: null; revokeTxId: null; creationTime: 0", providerChannel.toString());
		
		Assert.assertEquals(-974312256, providerChannel.hashCode());
	}
	
	@Test
	public void testProviderAddress() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		Assert.assertEquals(null, providerChannel.getProviderAddress());
		
		String providerAddress = "providerAddress";
		
		providerChannel.setProviderAddress(providerAddress);
		
		Assert.assertEquals(providerAddress, providerChannel.getProviderAddress());
		
	}
	
	@Test
	public void testUserAddress() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		Assert.assertEquals(null, providerChannel.getUserAddress());
		
		String userAddress = "userAddress";
		
		providerChannel.setUserAddress(userAddress);
		
		Assert.assertEquals(userAddress, providerChannel.getUserAddress());
		
	}
	
	@Test
	public void testBitmask() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		Assert.assertEquals(null, providerChannel.getBitmask());
		
		String bitmask = "bitmask";
		
		providerChannel.setBitmask(bitmask);
		
		Assert.assertEquals(bitmask, providerChannel.getBitmask());
		
	}
	
	@Test
	public void testRevokeAddress() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		Assert.assertEquals(null, providerChannel.getRevokeAddress());
		
		String revokeAddress = "revokeAddress";
		
		providerChannel.setRevokeAddress(revokeAddress);
		
		Assert.assertEquals(revokeAddress, providerChannel.getRevokeAddress());
		
	}
	
	@Test
	public void testRevokeTxId() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		Assert.assertEquals(null, providerChannel.getRevokeTxId());
		
		String revokeTxid = "revokeTxid";
		
		providerChannel.setRevokeTxId(revokeTxid);
		
		Assert.assertEquals(revokeTxid, providerChannel.getRevokeTxId());
		
	}
	
	@Test
	public void testCreationTime() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		long creationTime = System.currentTimeMillis();
		
		Assert.assertEquals(0, providerChannel.getCreationTime());
		
		providerChannel.setCreationTime(creationTime);
		
		Assert.assertEquals(creationTime, providerChannel.getCreationTime());
		
	}
	
	@Test
	public void testEquals() {
		
		ProviderChannel providerChannel1 = new ProviderChannel();
		
		ProviderChannel providerChannel2 = new ProviderChannel();
		
		Assert.assertEquals(true, providerChannel1.equals(providerChannel1));
		
		Assert.assertEquals(true, providerChannel1.equals(providerChannel2));
		
		providerChannel2.setUserAddress("123");
		
		Assert.assertEquals(false, providerChannel1.equals(providerChannel2));
		
		Assert.assertEquals(false, providerChannel1.equals(null));
		
	}

}
