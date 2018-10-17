package com.uniquid.register.impl.sql;

import com.uniquid.register.user.UserRegister;
import com.uniquid.register.user.UserRegisterTest;
import org.junit.BeforeClass;

public class SQLiteUserRegisterTest extends UserRegisterTest {

    private static SQLiteRegisterFactory factory;

    @BeforeClass
    public static void createNewDatabase() throws Exception {

        factory = UniquidNodeDBUtils.initDB();

    }

    @Override
    protected UserRegister getUserRegister() throws Exception {
        return factory.getUserRegister();
    }

}
