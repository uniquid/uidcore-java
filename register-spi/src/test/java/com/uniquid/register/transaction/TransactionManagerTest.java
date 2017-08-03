package com.uniquid.register.transaction;

import org.junit.Test;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;

public abstract class TransactionManagerTest {
	
	public abstract RegisterFactory getRegisterFactory() throws RegisterException;
	
	@Test
	public void testTransactionTest() throws Exception {
		
		TransactionManager transactionManager = getRegisterFactory().getTransactionManager();
		
		transactionManager.startTransaction();
		
		transactionManager.rollbackTransaction();
		
		
	}

}
