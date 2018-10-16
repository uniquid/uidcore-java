package com.uniquid.register.provider;

import com.uniquid.register.exception.RegisterException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public abstract class ProviderRegisterTest {
	
	protected abstract ProviderRegister getProviderRegister() throws Exception;

	@Test
	public void testProvider() throws Exception {

		List<ProviderChannel> channels = getProviderRegister().getAllChannels();

		Assert.assertNotNull(channels);
		Assert.assertEquals(0, channels.size());

		ProviderChannel providerChannel = new ProviderChannel();
		providerChannel.setProviderAddress("mfuta5iXJNe7yzCaPtmm4W2saiqTbTfxNG");
		providerChannel.setUserAddress("mkw5u34vDegrah5GasD5gKCJQ1NhNGG8tJ");
		providerChannel.setRevokeAddress("mjgWHUCV86eLp7B8mhHUuBAyCS136hz7SH");
		providerChannel.setRevokeTxId("97ab3c1a7bbca566712ab843a65d2e1bf94594b26b2ffe9d3348e4403065c1db");
		providerChannel.setBitmask("00000");
		providerChannel.setPath("path");

		try {

			getProviderRegister().insertChannel(null);
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("providerChannel is null!", ex.getLocalizedMessage());

		}

		getProviderRegister().insertChannel(providerChannel);
		
		try {

            getProviderRegister().insertChannel(providerChannel);
            Assert.fail();

        } catch (RegisterException ex) {

            Assert.assertEquals("Exception while insertChannel()", ex.getLocalizedMessage());

        }

		channels = getProviderRegister().getAllChannels();

		Assert.assertEquals(channels.size(), 1);

		Assert.assertEquals(providerChannel, channels.get(0));

		Assert.assertNull(getProviderRegister().getChannelByUserAddress("none"));

		try {

			getProviderRegister().getChannelByUserAddress(null);
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("address is not valid", ex.getLocalizedMessage());
		}

		try {

			getProviderRegister().getChannelByUserAddress("");
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("address is not valid", ex.getLocalizedMessage());
		}
		

		ProviderChannel provider2 = getProviderRegister()
				.getChannelByUserAddress("mkw5u34vDegrah5GasD5gKCJQ1NhNGG8tJ");

		Assert.assertEquals(providerChannel, provider2);

		Assert.assertNull(getProviderRegister().getChannelByRevokeAddress("none"));

		try {

			getProviderRegister().getChannelByRevokeAddress(null);
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("revokeAddress is not valid", ex.getLocalizedMessage());
		}

		try {

			getProviderRegister().getChannelByRevokeAddress("");
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("revokeAddress is not valid", ex.getLocalizedMessage());
		}

		provider2 = getProviderRegister().getChannelByRevokeAddress("mjgWHUCV86eLp7B8mhHUuBAyCS136hz7SH");

		Assert.assertEquals(providerChannel, provider2);

		try {

			getProviderRegister().getChannelByRevokeTxId(null);
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("revokeTxId is not valid", ex.getLocalizedMessage());
		}

		try {

			getProviderRegister().getChannelByRevokeTxId("");
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("revokeTxId is not valid", ex.getLocalizedMessage());
		}

		Assert.assertNull(getProviderRegister().getChannelByRevokeTxId("none"));

		provider2 = getProviderRegister()
				.getChannelByRevokeTxId("97ab3c1a7bbca566712ab843a65d2e1bf94594b26b2ffe9d3348e4403065c1db");

		Assert.assertEquals(providerChannel, provider2);

		try {

			getProviderRegister().deleteChannel(null);
			Assert.fail();

		} catch (RegisterException ex) {

			Assert.assertEquals("providerChannel is null!", ex.getLocalizedMessage());
		}

		getProviderRegister().deleteChannel(providerChannel);
		

		channels = getProviderRegister().getAllChannels();

		Assert.assertEquals(channels.size(), 0);

	}

}
