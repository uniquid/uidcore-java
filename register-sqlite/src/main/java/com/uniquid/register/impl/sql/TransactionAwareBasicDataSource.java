package com.uniquid.register.impl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.register.transaction.TransactionException;
import com.uniquid.register.transaction.TransactionManager;

public class TransactionAwareBasicDataSource extends BasicDataSource implements TransactionManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionAwareBasicDataSource.class);
	
	private static final ThreadLocal<TransactionAwareConnection> context = new ThreadLocal<TransactionAwareConnection>();

	@Override
	public void startTransaction() throws TransactionException {
		
		LOGGER.debug("Starting transaction " + Thread.currentThread().getName());

		try {
			
			Connection connection = context.get();
			
			if (connection != null) {
				
				//try {
				
				//	connection.rollback();
					
					throw new TransactionException("Transaction already in progress");
				
				/*} finally {
					
					connection.close();
					
				}*/
				
			}

			connection = super.getConnection();
			connection.setAutoCommit(false);

			context.set(new TransactionAwareConnection(connection, Thread.currentThread()));

		} catch (Exception ex) {

			throw new TransactionException("Exception", ex);

		}
		
	}

	@Override
	public void commitTransaction() throws TransactionException {

		LOGGER.debug("Committing transaction " + Thread.currentThread().getName());

		TransactionAwareConnection connection = context.get();

		try {
			
			connection.commit();

			connection.setAutoCommit(true);

		} catch (Exception ex) {

			throw new TransactionException("Exception", ex);

		} finally {
			
			try {
				
				connection.close();
			
			} catch (SQLException e) {
				
				LOGGER.error("Exception closing connection", e);
				
			}
		
			// remove wrapper!
			context.remove();

		}
		
	}

	@Override
	public void rollbackTransaction() throws TransactionException {
		
		LOGGER.debug("Rollbacking transaction " + Thread.currentThread().getName());

		TransactionAwareConnection connection = context.get();

		try {
			
			connection.rollback();

			connection.setAutoCommit(true);

		} catch (Exception ex) {

			throw new TransactionException("Exception", ex);

		} finally {
			
			try {
				
				connection.close();
			
			} catch (SQLException e) {
				
				LOGGER.error("Exception closing connection", e);
				
			}
		
			// remove wrapper!
			context.remove();

		}
		
	}

	@Override
    public Connection getConnection() throws SQLException {
		
		Connection connection = context.get();
		
		if (connection == null) {
			
			connection = super.getConnection();
			
		}
		
		return connection;
		
	}
	
}
