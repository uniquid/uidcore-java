package com.uniquid.register.impl.sql;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.transaction.TransactionManager;
import com.uniquid.register.user.UserRegister;

/**
 * Concrete class implementation of {@code RegisterFactory} that uses SQLite as data store.
 */
public class SQLiteRegisterFactory implements RegisterFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteRegisterFactory.class);

	protected TransactionAwareBasicDataSource dataSource;

	/**
	 * Creates an instance from the connection string
	 * 
	 * @param connectionString the database connection string
	 * @throws RegisterException in case a problem occurs.
	 */
	public SQLiteRegisterFactory(final String connectionString) throws RegisterException {

		if (connectionString == null) throw new RegisterException("connectionString is null!");
		
		try {

			dataSource = new TransactionAwareBasicDataSource();

			dataSource.setDriverClassName("org.sqlite.JDBC");
			
			dataSource.addConnectionProperty("foreign_keys", "ON");
			
			dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			
			dataSource.setMaxIdle(1);
			
			dataSource.setMinIdle(1);
			
			dataSource.setMaxTotal(1);
			
			dataSource.setInitialSize(1);
			
			dataSource.setDefaultAutoCommit(true);

			dataSource.setUrl(connectionString);

			Connection c = dataSource.getConnection();
			
			c.close();
			
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

	@Override
	public TransactionManager getTransactionManager() throws RegisterException {
		
		return dataSource;
		
	}

}
