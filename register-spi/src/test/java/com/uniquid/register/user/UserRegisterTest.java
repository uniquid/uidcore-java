package com.uniquid.register.user;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.uniquid.register.exception.RegisterException;

public abstract class UserRegisterTest {
	
	protected abstract UserRegister getUserRegister() throws Exception;

	@Test
	public void testUser() throws Exception {

		List<UserChannel> channels = getUserRegister().getAllUserChannels();

		Assert.assertNotNull(channels);
		Assert.assertEquals(channels.size(), 0);

		UserChannel userChannel = new UserChannel();
		userChannel.setProviderAddress("mfuta5iXJNe7yzCaPtmm4W2saiqTbTfxNG");
		userChannel.setUserAddress("mkw5u34vDegrah5GasD5gKCJQ1NhNGG8tJ");
		userChannel.setRevokeAddress("mjgWHUCV86eLp7B8mhHUuBAyCS136hz7SH");
		userChannel.setRevokeTxId("97ab3c1a7bbca566712ab843a65d2e1bf94594b26b2ffe9d3348e4403065c1db");
		userChannel.setBitmask("00000");
		userChannel.setProviderName("Test");
		userChannel.setSince(1528200741000L);		// 06/05/2018 @ 12:12pm (UTC)
		userChannel.setUntil(1591367400000L);		// 06/05/2020 @ 2:30pm (UTC)
		userChannel.setPath("");

		try {

			getUserRegister().insertChannel(null);
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("userchannel is null!", ex.getLocalizedMessage());

		}

		getUserRegister().insertChannel(userChannel);
		
		try {
			getUserRegister().insertChannel(userChannel);
		} catch (Exception ex) {
			Assert.assertTrue(ex instanceof RegisterException);
		}

		channels = getUserRegister().getAllUserChannels();

		Assert.assertEquals(channels.size(), 1);

		Assert.assertEquals(true, userChannel.equals(channels.get(0)));

		try {

			getUserRegister().getChannelByName(null);
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("name is not valid", ex.getLocalizedMessage());
		}

		try {

			getUserRegister().getChannelByName("");
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("name is not valid", ex.getLocalizedMessage());
		}

		Assert.assertNull(getUserRegister().getChannelByName("aaa"));

		UserChannel provider2 = getUserRegister().getChannelByName("Test");

		Assert.assertEquals(true, userChannel.equals(provider2));

		try {

			getUserRegister().getChannelByProviderAddress(null);
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("name is not valid", ex.getLocalizedMessage());
		}

		try {

			getUserRegister().getChannelByProviderAddress("");
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("name is not valid", ex.getLocalizedMessage());
		}

		Assert.assertNull(getUserRegister().getChannelByProviderAddress("aaa"));

		provider2 = getUserRegister().getChannelByProviderAddress("mfuta5iXJNe7yzCaPtmm4W2saiqTbTfxNG");

		Assert.assertEquals(true, userChannel.equals(provider2));

		try {

			getUserRegister().getUserChannelByRevokeTxId(null);
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("revokeTxId is not valid", ex.getLocalizedMessage());
		}

		try {

			getUserRegister().getUserChannelByRevokeTxId("");
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("revokeTxId is not valid", ex.getLocalizedMessage());
		}

		Assert.assertNull(getUserRegister().getUserChannelByRevokeTxId("aaa"));

		provider2 = getUserRegister()
				.getUserChannelByRevokeTxId("97ab3c1a7bbca566712ab843a65d2e1bf94594b26b2ffe9d3348e4403065c1db");

		Assert.assertEquals(true, userChannel.equals(provider2));

		try {

			getUserRegister().getUserChannelByRevokeAddress(null);
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("revokeAddress is not valid", ex.getLocalizedMessage());
		}

		try {

			getUserRegister().getUserChannelByRevokeAddress("");
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("revokeAddress is not valid", ex.getLocalizedMessage());
		}

		Assert.assertNull(getUserRegister().getUserChannelByRevokeAddress("aaa"));

		provider2 = getUserRegister().getUserChannelByRevokeAddress("mjgWHUCV86eLp7B8mhHUuBAyCS136hz7SH");

		Assert.assertEquals(true, userChannel.equals(provider2));

		try {

			getUserRegister().deleteChannel(null);
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("userchannel is null!", ex.getLocalizedMessage());

		}

		getUserRegister().deleteChannel(userChannel);
		
		try {
			getUserRegister().deleteChannel(userChannel);
		} catch (Exception ex) {
			Assert.assertTrue(ex instanceof RegisterException);
		}

		channels = getUserRegister().getAllUserChannels();

		Assert.assertEquals(channels.size(), 0);
		
		userChannel.setUntil(1528194600000L);
		getUserRegister().insertChannel(userChannel);
		
		channels = getUserRegister().getAllUserChannels();
		Assert.assertEquals(0, channels.size());
		
		getUserRegister().deleteChannel(userChannel);

	}
	
}
