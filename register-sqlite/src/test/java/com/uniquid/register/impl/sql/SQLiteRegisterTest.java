package com.uniquid.register.impl.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.user.UserChannel;

public class SQLiteRegisterTest {
	
public static String CREATE_PROVIDER_TABLE = "create table provider_channel (provider_address text not null, user_address text not null, bitmask text not null, revoke_address text not null, revoke_tx_id text not null, creation_time integer not null, primary key (provider_address, user_address));";
	
	public static String CREATE_USER_TABLE = "create table user_channel (provider_name text not null, provider_address text not null, user_address text not null, bitmask text not null, revoke_address text not null, revoke_tx_id text not null, primary key (provider_name, provider_address, user_address));";
	
	private static SQLiteRegisterFactory factory;
	
	@BeforeClass
	public static void createNewDatabase() throws Exception {
		
		Class.forName("org.sqlite.JDBC");

		String url = "jdbc:sqlite:" + File.createTempFile("test", ".db");

		Connection conn = DriverManager.getConnection(url);
			
		Statement statement = conn.createStatement();
			
		statement.executeUpdate(CREATE_PROVIDER_TABLE);
			
		statement.executeUpdate(CREATE_USER_TABLE);
		
		factory = new SQLiteRegisterFactory(url);
		
	}
	
	@Test
	public void testProvider() throws Exception {
		
		List<ProviderChannel> channels = factory.getProviderRegister().getAllChannels();
		
		Assert.assertEquals(channels.size(), 0);
		
		ProviderChannel providerChannel = new ProviderChannel();
		providerChannel.setProviderAddress("mfuta5iXJNe7yzCaPtmm4W2saiqTbTfxNG");
		providerChannel.setUserAddress("mkw5u34vDegrah5GasD5gKCJQ1NhNGG8tJ");
		providerChannel.setRevokeAddress("mjgWHUCV86eLp7B8mhHUuBAyCS136hz7SH");
		providerChannel.setRevokeTxId("97ab3c1a7bbca566712ab843a65d2e1bf94594b26b2ffe9d3348e4403065c1db");
		providerChannel.setBitmask("00000");
		
		factory.getProviderRegister().insertChannel(providerChannel);
		
		channels = factory.getProviderRegister().getAllChannels();
			
		Assert.assertEquals(channels.size(), 1);
		
		Assert.assertEquals(true, providerChannel.equals(channels.get(0)));
		
		ProviderChannel provider2 = factory.getProviderRegister().getChannelByUserAddress("mkw5u34vDegrah5GasD5gKCJQ1NhNGG8tJ");
		
		Assert.assertEquals(true, providerChannel.equals(provider2));
		
		provider2 = factory.getProviderRegister().getChannelByRevokeAddress("mjgWHUCV86eLp7B8mhHUuBAyCS136hz7SH");
		
		Assert.assertEquals(true, providerChannel.equals(provider2));
		
		provider2 = factory.getProviderRegister().getChannelByRevokeTxId("97ab3c1a7bbca566712ab843a65d2e1bf94594b26b2ffe9d3348e4403065c1db");
		
		Assert.assertEquals(true, providerChannel.equals(provider2));
		
		factory.getProviderRegister().deleteChannel(providerChannel);
		
		channels = factory.getProviderRegister().getAllChannels();
		
		Assert.assertEquals(channels.size(), 0);
		
	}
	
	@Test
	public void testUser() throws Exception {
		
		List<UserChannel> channels = factory.getUserRegister().getAllUserChannels();
		
		Assert.assertEquals(channels.size(), 0);
		
		UserChannel userChannel = new UserChannel();
		userChannel.setProviderAddress("mfuta5iXJNe7yzCaPtmm4W2saiqTbTfxNG");
		userChannel.setUserAddress("mkw5u34vDegrah5GasD5gKCJQ1NhNGG8tJ");
		userChannel.setRevokeAddress("mjgWHUCV86eLp7B8mhHUuBAyCS136hz7SH");
		userChannel.setRevokeTxId("97ab3c1a7bbca566712ab843a65d2e1bf94594b26b2ffe9d3348e4403065c1db");
		userChannel.setBitmask("00000");
		userChannel.setProviderName("Test");
		
		factory.getUserRegister().insertChannel(userChannel);
		
		channels = factory.getUserRegister().getAllUserChannels();
			
		Assert.assertEquals(channels.size(), 1);
		
		Assert.assertEquals(true, userChannel.equals(channels.get(0)));
		
		UserChannel provider2 = factory.getUserRegister().getChannelByName("Test");
		
		Assert.assertEquals(true, userChannel.equals(provider2));
		
		provider2 = factory.getUserRegister().getChannelByProviderAddress("mfuta5iXJNe7yzCaPtmm4W2saiqTbTfxNG");
		
		Assert.assertEquals(true, userChannel.equals(provider2));
		
		provider2 = factory.getUserRegister().getUserChannelByRevokeTxId("97ab3c1a7bbca566712ab843a65d2e1bf94594b26b2ffe9d3348e4403065c1db");
		
		Assert.assertEquals(true, userChannel.equals(provider2));
		
		provider2 = factory.getUserRegister().getUserChannelByRevokeAddress("mjgWHUCV86eLp7B8mhHUuBAyCS136hz7SH");
		
		Assert.assertEquals(true, userChannel.equals(provider2));
		
		factory.getUserRegister().deleteChannel(userChannel);
		
		channels = factory.getUserRegister().getAllUserChannels();
		
		Assert.assertEquals(channels.size(), 0);
		
	}

}
