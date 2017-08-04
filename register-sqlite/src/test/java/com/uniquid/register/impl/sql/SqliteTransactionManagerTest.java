package com.uniquid.register.impl.sql;

import org.junit.BeforeClass;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.transaction.TransactionManagerTest;

public class SqliteTransactionManagerTest extends TransactionManagerTest {

	private static SQLiteRegisterFactory factory;

	@BeforeClass
	public static void createNewDatabase() throws Exception {

		factory = UniquidNodeDBUtils.initDB();

	}
	
	@Override
	public RegisterFactory getRegisterFactory() throws RegisterException {
		
		return factory;
		
	}
	
}
