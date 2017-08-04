package com.uniquid.register.impl.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UniquidNodeDBUtils {

public static SQLiteRegisterFactory initDB() throws Exception {
		
		Class.forName("org.sqlite.JDBC");

		String url = "jdbc:sqlite:" + File.createTempFile("node", ".db");

		Connection conn = DriverManager.getConnection(url);

		Statement statement = conn.createStatement();
		
		statement.execute("PRAGMA foreign_keys = ON;");

		statement.executeUpdate(SQLiteRegister.CREATE_PROVIDER_TABLE);
		statement.executeUpdate(SQLiteRegister.CREATE_USER_TABLE);

		return new SQLiteRegisterFactory(url);
		
	}
	
}
