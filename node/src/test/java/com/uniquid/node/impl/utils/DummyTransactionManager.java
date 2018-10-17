package com.uniquid.node.impl.utils;

import com.uniquid.register.transaction.TransactionException;
import com.uniquid.register.transaction.TransactionManager;

public class DummyTransactionManager implements TransactionManager {

    @Override
    public void startTransaction() throws TransactionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollbackTransaction() throws TransactionException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean insideTransaction() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void commitTransaction() throws TransactionException {
        // TODO Auto-generated method stub

    }

}
