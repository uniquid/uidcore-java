package com.uniquid.register.impl.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.BeforeClass;

import com.uniquid.register.user.UserRegister;
import com.uniquid.register.user.UserRegisterTest;

public class SQLiteUserRegisterTest extends UserRegisterTest {

	private static SQLiteRegisterFactory factory;

	@BeforeClass
	public static void createNewDatabase() throws Exception {

		Class.forName("org.sqlite.JDBC");

		String url = "jdbc:sqlite:" + File.createTempFile("user", ".db");

		Connection conn = DriverManager.getConnection(url);

		Statement statement = conn.createStatement();

		statement.executeUpdate(SQLiteRegister.CREATE_USER_TABLE);

		factory = new SQLiteRegisterFactory(url);

	}

	@Override
	protected UserRegister getUserRegister() throws Exception {
		return factory.getUserRegister();
	}

}
