package com.uniquid.register.transaction;

public interface TransactionManager {

	void startTransaction() throws TransactionException;

	void commitTransaction() throws TransactionException;

	void rollbackTransaction() throws TransactionException;

	boolean insideTransaction();

}
