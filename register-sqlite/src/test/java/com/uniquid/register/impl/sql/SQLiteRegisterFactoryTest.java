package com.uniquid.register.impl.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.RegisterFactoryTest;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

public class SQLiteRegisterFactoryTest extends RegisterFactoryTest {

	private static SQLiteRegisterFactory factory;
	private static String url;

	@BeforeClass
	public static void createNewDatabase() throws Exception {

		Class.forName("org.sqlite.JDBC");

		url = "jdbc:sqlite:" + File.createTempFile("test", ".db");

		Connection conn = DriverManager.getConnection(url);

		Statement statement = conn.createStatement();

		statement.executeUpdate(SQLiteRegister.CREATE_PROVIDER_TABLE);

		statement.executeUpdate(SQLiteRegister.CREATE_USER_TABLE);

		statement.close();

		conn.close();

		try {
			
			factory = new SQLiteRegisterFactory(url);

			Assert.assertNotNull(factory);

		} catch (Exception ex) {

			Assert.fail("unexpected");

		}

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

	@Override
	public RegisterFactory getRegisterFactory() {
		return factory;
	}

}
