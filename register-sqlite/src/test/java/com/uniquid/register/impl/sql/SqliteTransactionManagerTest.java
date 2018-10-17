package com.uniquid.register.impl.sql;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.transaction.TransactionManagerTest;
import org.junit.BeforeClass;

public class SqliteTransactionManagerTest extends TransactionManagerTest {

    private static SQLiteRegisterFactory factory;

    @BeforeClass
    public static void createNewDatabase() throws Exception {

        factory = UniquidNodeDBUtils.initDB();

    }

    @Override
    public RegisterFactory getRegisterFactory() throws RegisterException {

        return factory;

    }

}
