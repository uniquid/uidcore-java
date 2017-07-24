package com.uniquid.register.impl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.register.transaction.ConnectionGenerator;
import com.uniquid.register.transaction.TransactionException;
import com.uniquid.register.transaction.TransactionManager;

public class TransactionManagerImpl implements TransactionManager, ConnectionGenerator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerImpl.class);

	private static final ThreadLocal<Connection> context = new ThreadLocal<Connection>();

	private BasicDataSource dataSource;

	public TransactionManagerImpl(BasicDataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void startTransaction() throws TransactionException {
		
		LOGGER.info("Starting transaction " + Thread.currentThread().getName());

		try {
			
			Connection connection = context.get();
			
			if (connection != null) {
				
				throw new TransactionException("Transaction already in progress");
				
			}

			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			context.set(connection);

		} catch (Exception ex) {

			throw new TransactionException("Exception", ex);

		}

	}

	@Override
	public void commitTransaction() throws TransactionException {
		
		LOGGER.info("Committing transaction " + Thread.currentThread().getName());

		Connection connection = context.get();

		try {
			
			connection.commit();

			connection.setAutoCommit(true);
			
			context.remove();

		} catch (Exception ex) {

			throw new TransactionException("Exception", ex);

		} finally {
			
			try {
				
				connection.close();
			
			} catch (SQLException e) {
				
				LOGGER.error("Exception closing connection", e);
				
			}
			
		}
		
	}

	@Override
	public void rollbackTransaction() throws TransactionException {
		
		LOGGER.info("Rollbacking transaction " + Thread.currentThread().getName());

		Connection connection = context.get();

		try {
			
			connection.rollback();

			connection.setAutoCommit(true);

			context.remove();

		} catch (Exception ex) {

			throw new TransactionException("Exception", ex);

		} finally {
			
			try {
				
				connection.close();
			
			} catch (SQLException e) {
				
				LOGGER.error("Exception closing connection", e);
				
			}
			
		}

	}

	@Override
	public Connection getConnection() {

		return context.get();

	}

	@Override
	public boolean isInsideTransaction() {
		
		return context.get() != null;
		
	}

}
