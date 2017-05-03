package com.uniquid.register.provider;

import org.junit.Assert;
import org.junit.Test;

public class ProviderChannelTest {
	
	@Test
	public void testEmptyConstructor() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		Assert.assertEquals(providerChannel.getProviderAddress(), null);
		Assert.assertEquals(providerChannel.getUserAddress(), null);
		Assert.assertEquals(providerChannel.getBitmask(), null);
		Assert.assertEquals(providerChannel.getRevokeAddress(), null);
		Assert.assertEquals(providerChannel.getRevokeTxId(), null);
		
	}
	
	@Test
	public void testProviderAddress() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		String test = "test";
		
		providerChannel.setProviderAddress(test);
		
		Assert.assertEquals(test, providerChannel.getProviderAddress());
		
	}
	
	@Test
	public void testUserAddress() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		String test = "test";
		
		providerChannel.setUserAddress(test);
		
		Assert.assertEquals(test, providerChannel.getUserAddress());
		
	}
	
	@Test
	public void testBitmask() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		String test = "test";
		
		providerChannel.setBitmask(test);
		
		Assert.assertEquals(test, providerChannel.getBitmask());
		
	}
	
	@Test
	public void testRevokeAddress() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		String test = "test";
		
		providerChannel.setRevokeAddress(test);
		
		Assert.assertEquals(test, providerChannel.getRevokeAddress());
		
	}
	
	@Test
	public void testRevokeTxId() {
		
		ProviderChannel providerChannel = new ProviderChannel();
		
		String test = "test";
		
		providerChannel.setRevokeTxId(test);
		
		Assert.assertEquals(test, providerChannel.getRevokeTxId());
		
	}
	
	public void testEquals() {
		
		ProviderChannel providerChannel1 = new ProviderChannel();
		
		ProviderChannel providerChannel2 = new ProviderChannel();
		
		Assert.assertEquals(providerChannel1, providerChannel2);
		
	}

}
