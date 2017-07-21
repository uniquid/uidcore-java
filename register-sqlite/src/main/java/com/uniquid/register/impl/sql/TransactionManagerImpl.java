package com.uniquid.register.impl.sql;

import java.sql.Connection;

import org.apache.commons.dbcp2.BasicDataSource;

import com.uniquid.register.transaction.ConnectionGenerator;
import com.uniquid.register.transaction.TransactionException;
import com.uniquid.register.transaction.TransactionManager;

public class TransactionManagerImpl implements TransactionManager, ConnectionGenerator {

	private static final ThreadLocal<Connection> context = new ThreadLocal<Connection>();

	private BasicDataSource dataSource;

	public TransactionManagerImpl(BasicDataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void startTransaction() throws TransactionException {

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

		Connection connection = context.get();

		try {
			
			connection.commit();

			connection.setAutoCommit(true);

		} catch (Exception ex) {

			throw new TransactionException("Exception", ex);

		} finally {
			
			context.remove();
			
		}
		
	}

	@Override
	public void rollbackTransaction() throws TransactionException {

		Connection connection = context.get();

		try {
			
			connection.rollback();

			connection.setAutoCommit(true);


		} catch (Exception ex) {

			throw new TransactionException("Exception", ex);

		} finally {
			
			context.remove();
			
		}

	}

	@Override
	public Connection getConnection() {

		return context.get();

	}

}
