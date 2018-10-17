package com.uniquid.node.impl.utils;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.transaction.TransactionManager;
import com.uniquid.register.user.UserRegister;

public class DummyRegisterFactory implements RegisterFactory {

    private UserRegister userRegister;
    private ProviderRegister providerRegister;
    private TransactionManager transactionManager;

    public DummyRegisterFactory(UserRegister userRegister, ProviderRegister providerRegister, TransactionManager transactionManager) {
        this.userRegister = userRegister;
        this.providerRegister = providerRegister;
        this.transactionManager = transactionManager;
    }

    @Override
    public UserRegister getUserRegister() throws RegisterException {
        return userRegister;
    }

    @Override
    public ProviderRegister getProviderRegister() throws RegisterException {
        return providerRegister;
    }

    @Override
    public TransactionManager getTransactionManager() throws RegisterException {
        return transactionManager;
    }

}
