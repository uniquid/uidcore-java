package com.uniquid.register.impl.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Wrapper around Connection implementation. This will avoid to close explicitly a connection
 * if we are in the middle of a transaction
 */
public class TransactionAwareConnection implements Connection {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionAwareConnection.class.getName());
	
	private final Object sync;
	
	private Connection wrapped;
	private volatile boolean inTransaction;
	private long threadId;
	private String threadName;
	
	public TransactionAwareConnection(Connection wrapped, Thread runnerThread) {
		this.sync = new Object();
		this.wrapped = wrapped;
		this.inTransaction = true;
		this.threadId = runnerThread.getId();
		this.threadName = runnerThread.getName();
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		
		return wrapped.unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		
		return wrapped.isWrapperFor(iface);
	}

	public Statement createStatement() throws SQLException {
		
		return wrapped.createStatement();
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		
		return wrapped.prepareStatement(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		
		return wrapped.prepareCall(sql);
	}

	public String nativeSQL(String sql) throws SQLException {
		
		return wrapped.nativeSQL(sql);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		
		wrapped.setAutoCommit(autoCommit);
	}

	public boolean getAutoCommit() throws SQLException {
		
		return wrapped.getAutoCommit();
	}

	public void commit() throws SQLException {
		
		synchronized (sync) {
			
			try {
				//checkCurrentThread();
				wrapped.commit();
			} finally {
				inTransaction = false;
			}

		}
		
	}

	public void rollback() throws SQLException {
		
		synchronized (sync) {

			try {
				//checkCurrentThread();
				wrapped.rollback();
			} finally {
				inTransaction = false;
			}
			
		}
		
	}

	public void close() throws SQLException {
		
		wrapped.close();
		
	}

	public boolean isClosed() throws SQLException {
		
		return wrapped.isClosed();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		
		return wrapped.getMetaData();
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		
		wrapped.setReadOnly(readOnly);
	}

	public boolean isReadOnly() throws SQLException {
		
		return wrapped.isReadOnly();
	}

	public void setCatalog(String catalog) throws SQLException {
		
		wrapped.setCatalog(catalog);
	}

	public String getCatalog() throws SQLException {

		return wrapped.getCatalog();
	}

	public void setTransactionIsolation(int level) throws SQLException {
		
		wrapped.setTransactionIsolation(level);
	}

	public int getTransactionIsolation() throws SQLException {
		
		return wrapped.getTransactionIsolation();
	}

	public SQLWarning getWarnings() throws SQLException {
		
		return wrapped.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		
		wrapped.clearWarnings();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		
		return wrapped.createStatement(resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		
		return wrapped.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		
		return wrapped.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		
		return wrapped.getTypeMap();
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		
		wrapped.setTypeMap(map);
	}

	public void setHoldability(int holdability) throws SQLException {
		
		wrapped.setHoldability(holdability);
	}

	public int getHoldability() throws SQLException {
		
		return wrapped.getHoldability();
	}

	public Savepoint setSavepoint() throws SQLException {
		
		return wrapped.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		
		return wrapped.setSavepoint(name);
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		
		wrapped.rollback(savepoint);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		
		wrapped.releaseSavepoint(savepoint);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		
		return wrapped.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		
		return wrapped.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		
		return wrapped.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		
		return wrapped.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		
		return wrapped.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		
		return wrapped.prepareStatement(sql, columnNames);
	}

	public Clob createClob() throws SQLException {
		
		return wrapped.createClob();
	}

	public Blob createBlob() throws SQLException {
		
		return wrapped.createBlob();
	}

	public NClob createNClob() throws SQLException {
		
		return wrapped.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		
		return wrapped.createSQLXML();
	}

	public boolean isValid(int timeout) throws SQLException {
		
		return wrapped.isValid(timeout);
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		
		wrapped.setClientInfo(name, value);
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		
		wrapped.setClientInfo(properties);
	}

	public String getClientInfo(String name) throws SQLException {
		
		return wrapped.getClientInfo(name);
	}

	public Properties getClientInfo() throws SQLException {
		
		return wrapped.getClientInfo();
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		
		return wrapped.createArrayOf(typeName, elements);
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		
		return wrapped.createStruct(typeName, attributes);
	}

	public void setSchema(String schema) throws SQLException {
		
		wrapped.setSchema(schema);
	}

	public String getSchema() throws SQLException {
		
		return wrapped.getSchema();
	}

	public void abort(Executor executor) throws SQLException {
		
		wrapped.abort(executor);
	}

	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		
		wrapped.setNetworkTimeout(executor, milliseconds);
	}

	public int getNetworkTimeout() throws SQLException {
		
		return wrapped.getNetworkTimeout();
	}
	
	public boolean inTransaction() {
		
		synchronized (sync) {
			
			return inTransaction;
			
		}
		
	}
	
	private void checkCurrentThread() {
		
		Thread runner = Thread.currentThread();
		
		if (runner.getId() != threadId ||
				!runner.getName().equals(threadName)) {
			
			LOGGER.error("The thread executing transaction is different");
			
			throw new IllegalStateException("The thread executing transaction is different");
			
		}
		
	}
}
