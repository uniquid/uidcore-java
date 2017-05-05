package com.uniquid.register.impl.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

public class SQLiteRegisterFactoryTest {
	
	public static String CREATE_PROVIDER_TABLE = "create table provider_channel (provider_address text not null, user_address text not null, bitmask text not null, revoke_address text not null, revoke_tx_id text not null, creation_time integer not null, primary key (provider_address, user_address));";
	
	public static String CREATE_USER_TABLE = "create table user_channel (provider_name text not null, provider_address text not null, user_address text not null, bitmask text not null, revoke_address text not null, revoke_tx_id text not null, primary key (provider_name, provider_address, user_address));";

	private static SQLiteRegisterFactory factory;
	private static String url;
	
	@BeforeClass
	public static void createNewDatabase() throws Exception {
		
		Class.forName("org.sqlite.JDBC");

		url = "jdbc:sqlite:" + File.createTempFile("test", ".db");

		Connection conn = DriverManager.getConnection(url);
			
		Statement statement = conn.createStatement();
			
		statement.executeUpdate(CREATE_PROVIDER_TABLE);
			
		statement.executeUpdate(CREATE_USER_TABLE);
		
	}

	@Test
	public void testSQLiteRegisterFactory() {
		
		try {
		
			new SQLiteRegisterFactory(null);
			
			Assert.fail();
		
		} catch (RegisterException ex) {
			
			//
			
		}

		try {
			
			factory = new SQLiteRegisterFactory(url);

			Assert.assertNotNull(factory);

		} catch (Exception ex) {

			Assert.fail("unexpected");

		}
		
	}
	
	@Test
	public void testGetProviderRegister() throws Exception {
		
		ProviderRegister providerRegister = factory.getProviderRegister();
		
		Assert.assertNotNull(providerRegister);
	
	}
	
	@Test
	public void testGetUserRegister() throws Exception {
		
		UserRegister userRegister = factory.getUserRegister();
		
		Assert.assertNotNull(userRegister);
		
	}
	
	public void testInstance() throws Exception {
		
		ProviderRegister providerRegister = factory.getProviderRegister();
		
		UserRegister userRegister = factory.getUserRegister();
		
		Assert.assertEquals(true, providerRegister == userRegister);
		
	}

}
