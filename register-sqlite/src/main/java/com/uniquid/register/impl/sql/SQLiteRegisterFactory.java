package com.uniquid.register.impl.sql;

import org.apache.commons.dbcp2.BasicDataSource;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

/**
 * Concrete class implementation of {@code RegisterFactory} that uses SQLite as data store.
 */
public class SQLiteRegisterFactory implements RegisterFactory {

	protected BasicDataSource dataSource;

	/**
	 * Creates an instance from the connection string
	 * 
	 * @param connectionString the database connection string
	 * @throws RegisterException in case a problem occurs.
	 */
	public SQLiteRegisterFactory(final String connectionString) throws RegisterException {

		if (connectionString == null) throw new RegisterException("connectionString is null!");
		
		try {

			dataSource = new BasicDataSource();

			dataSource.setDriverClassName("org.sqlite.JDBC");
			
			dataSource.addConnectionProperty("foreign_keys", "true");

			dataSource.setUrl(connectionString);

			dataSource.getConnection();

		} catch (Exception ex) {
			
			throw new RegisterException("Exception while creating register", ex);
			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProviderRegister getProviderRegister() throws RegisterException {
		
		if (dataSource == null) throw new RegisterException("Datasource is null");
		
		return new SQLiteRegister(dataSource);
	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserRegister getUserRegister() throws RegisterException {
		
		if (dataSource == null) throw new RegisterException("Datasource is null");
		
		return new SQLiteRegister(dataSource);
		
	}
	
	/**
	 * Destroy the data source
	 */
	public void destroy() throws RegisterException {
		
		try {
		
			dataSource.close();
			
			dataSource = null;
		
		} catch (Exception ex) {
			
			throw new RegisterException("Exception while closing dataSource", ex);
			
		}
		
	}

}
