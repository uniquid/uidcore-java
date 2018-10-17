package com.uniquid.register.impl.sql;

import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.provider.ProviderRegisterTest;
import org.junit.BeforeClass;

public class SQLiteProviderRegisterTest extends ProviderRegisterTest {

    private static SQLiteRegisterFactory factory;

    @BeforeClass
    public static void createNewDatabase() throws Exception {

        factory = UniquidNodeDBUtils.initDB();

    }

    @Override
    protected ProviderRegister getProviderRegister() throws Exception {
        return factory.getProviderRegister();
    }


}
