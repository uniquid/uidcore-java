package com.uniquid.register.impl.sql;

import java.sql.Connection;
import java.sql.DriverManager;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

/**
 * Concrete class implementation of {@code RegisterFactory} that uses SQLite as data store.
 */
public class SQLiteRegisterFactory implements RegisterFactory {

	private SQLiteRegister instance;

	/**
	 * Creates an instance from the connection string
	 * 
	 * @param connectionString the database connection string
	 * @throws RegisterException in case a problem occurs.
	 */
	public SQLiteRegisterFactory(final String connectionString) throws RegisterException {

		if (connectionString == null) throw new RegisterException("connectionString is null!");
		
		try {
			
			Class.forName("org.sqlite.JDBC");

			// create a database connection
			Connection connection = DriverManager.getConnection(connectionString);
			
			// Disable auto commit
			if (!connection.getAutoCommit()) {
				connection.setAutoCommit(true);
			}

			instance = new SQLiteRegister(connection);
		
		} catch (Exception ex) {
			
			throw new RegisterException("Exception while creating register", ex);
			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProviderRegister getProviderRegister() throws RegisterException {
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserRegister getUserRegister() throws RegisterException {
		return instance;
	}

}
