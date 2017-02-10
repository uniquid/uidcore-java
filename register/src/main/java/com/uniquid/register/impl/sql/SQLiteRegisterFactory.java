package com.uniquid.register.impl.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

public class SQLiteRegisterFactory extends RegisterFactory {

	private static final String PREFIX = "SQLiteRegisterFactory";
	public static final String JDBC_DRIVER = PREFIX + ".jdbc.driver";
	public static final String JDBC_CONNECTION = PREFIX + ".jdbc.connection";
	
	private SQLiteRegister instance;

	public SQLiteRegisterFactory(Map<String, Object> configuration) throws RegisterException {

		// delegate to superclass
		super(configuration);
		
		try {
			
			Class.forName((String) factoryConfiguration.get(JDBC_DRIVER));

			// create a database connection
			Connection connection = DriverManager.getConnection((String) factoryConfiguration.get(JDBC_CONNECTION));
			
			// Disable auto commit
			if (!connection.getAutoCommit()) {
				connection.setAutoCommit(true);
			}

			instance = new SQLiteRegister(connection);
		
		} catch (Exception ex) {
			
			throw new RegisterException("Exception while creating register", ex);
			
		}
	}

	@Override
	public ProviderRegister getProviderRegister() throws RegisterException {
		return instance;
	}

	@Override
	public UserRegister getUserRegister() throws RegisterException {
		return instance;
	}

}
