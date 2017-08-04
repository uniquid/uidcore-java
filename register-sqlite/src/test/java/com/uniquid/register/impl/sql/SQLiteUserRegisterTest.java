package com.uniquid.register.impl.sql;

import org.junit.BeforeClass;

import com.uniquid.register.user.UserRegister;
import com.uniquid.register.user.UserRegisterTest;

public class SQLiteUserRegisterTest extends UserRegisterTest {

	private static SQLiteRegisterFactory factory;

	@BeforeClass
	public static void createNewDatabase() throws Exception {

		factory = UniquidNodeDBUtils.initDB();

	}

	@Override
	protected UserRegister getUserRegister() throws Exception {
		return factory.getUserRegister();
	}

}
