package com.uniquid.register.impl.sql;

import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Before;

public class TestSQLiteRegisterFactory {

	@Before
	public void createNewDatabase() throws Exception {
		
		Class.forName("org.sqlite.JDBC");

		String url = "jdbc:sqlite:" + File.createTempFile("test", ".db");

		try (Connection conn = DriverManager.getConnection(url)) {
			if (conn != null) {
				DatabaseMetaData meta = conn.getMetaData();
				System.out.println("The driver name is " + meta.getDriverName());
				System.out.println("A new database has been created.");
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testSQLiteRegisterFactory() {

		try {

			SQLiteRegisterFactory factory = new SQLiteRegisterFactory(
					"jdbc:sqlite:/home/giuseppe/dev/oauthserver/oauth.db");

			Assert.assertNotNull(factory);

		} catch (Exception ex) {

			Assert.fail("unexpected");

		}
	}

}
