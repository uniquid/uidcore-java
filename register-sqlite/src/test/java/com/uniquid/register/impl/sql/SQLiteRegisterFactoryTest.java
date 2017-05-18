package com.uniquid.register.impl.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

public class SQLiteRegisterFactoryTest {
	
	private static SQLiteRegisterFactory factory;
	private static String url;
	
	@BeforeClass
	public static void createNewDatabase() throws Exception {
		
		try {
			
			new SQLiteRegisterFactory("notNullString");
			Assert.fail();
			
		} catch (RegisterException ex) {
			
			//
			
		}
		
		Class.forName("org.sqlite.JDBC");

		url = "jdbc:sqlite:" + File.createTempFile("test", ".db");

		Connection conn = DriverManager.getConnection(url);
			
		Statement statement = conn.createStatement();
			
		statement.executeUpdate(SQLiteRegister.CREATE_PROVIDER_TABLE);
			
		statement.executeUpdate(SQLiteRegister.CREATE_USER_TABLE);
		
		statement.close();
		
		conn.close();
		
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
	
	@Test
	public void testInstance() throws Exception {
		
		ProviderRegister providerRegister = factory.getProviderRegister();
		
		Assert.assertNotNull(providerRegister);
		
		UserRegister userRegister = factory.getUserRegister();
		
		Assert.assertNotNull(userRegister);
		
	}
	
	@AfterClass
	public static void testDestroy() throws Exception {
		
		factory.destroy();
		
		try {
			ProviderRegister providerRegister = factory.getProviderRegister();
		} catch (RegisterException ex) {
			Assert.assertEquals("Datasource is null", ex.getLocalizedMessage());
		}
		
		try {
			UserRegister userRegister = factory.getUserRegister();
		} catch (RegisterException ex) {
			Assert.assertEquals("Datasource is null", ex.getLocalizedMessage());
		}
		
		try {
			factory.destroy();
		} catch (RegisterException ex) {
			Assert.assertEquals("Exception while closing dataSource", ex.getLocalizedMessage());
		}
		
	}

}
