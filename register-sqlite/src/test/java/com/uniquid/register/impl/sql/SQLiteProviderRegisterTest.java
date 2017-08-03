package com.uniquid.register.impl.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.BeforeClass;

import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.provider.ProviderRegisterTest;

public class SQLiteProviderRegisterTest extends ProviderRegisterTest {

	private static SQLiteRegisterFactory factory;

	@BeforeClass
	public static void createNewDatabase() throws Exception {

		Class.forName("org.sqlite.JDBC");

		String url = "jdbc:sqlite:" + File.createTempFile("provider", ".db");

		Connection conn = DriverManager.getConnection(url);

		Statement statement = conn.createStatement();

		statement.executeUpdate(SQLiteRegister.CREATE_PROVIDER_TABLE);

		factory = new SQLiteRegisterFactory(url);

	}

	@Override
	protected ProviderRegister getProviderRegister() throws Exception {
		return factory.getProviderRegister();
	}


}
