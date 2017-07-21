package com.uniquid.register.transaction;

public interface TransactionManager {

	public void startTransaction() throws TransactionException;

	public void commitTransaction() throws TransactionException;
	
	public void rollbackTransaction() throws TransactionException;
	
	public boolean isInsideTransaction();
	
}
