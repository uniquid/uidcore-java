package com.uniquid.register.impl.sql;

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

	@BeforeClass
	public static void createNewDatabase() throws Exception {

		factory = UniquidNodeDBUtils.initDB();

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
