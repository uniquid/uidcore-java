package com.uniquid.register.impl.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderRegister;

public class SQLiteRegisterFactory extends RegisterFactory {

	private static final String PREFIX = "SQLiteRegisterFactory";
	public static final String JDBC_DRIVER = PREFIX + ".jdbc.driver";
	public static final String JDBC_CONNECTION = PREFIX + ".jdbc.connection";
	
	private static SQLiteRegister INSTANCE;

	public SQLiteRegisterFactory(Map<String, Object> configuration) {
		super(configuration);
	}

	private synchronized SQLiteRegister createRegister() throws Exception {
		
		if (INSTANCE == null) {
			
			Class.forName((String) factoryConfiguration.get(JDBC_DRIVER));

			// create a database connection
			Connection connection = DriverManager.getConnection((String) factoryConfiguration.get(JDBC_CONNECTION));
			
			// Disable auto commit
			if (!connection.getAutoCommit()) {
				connection.setAutoCommit(true);
			}

			INSTANCE = new SQLiteRegister(connection);
			
		}
		
		return INSTANCE;

	}

	@Override
	public ProviderRegister createProviderRegister() throws Exception {
		return createRegister();
	}

}
