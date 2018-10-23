package com.uniquid.register.impl.sql;

import java.io.File;

public class UniquidNodeDBUtils {

    public static SQLiteRegisterFactory initDB() throws Exception {

        Class.forName("org.sqlite.JDBC");

        String url = "jdbc:sqlite:" + File.createTempFile("node", ".db");

        return new SQLiteRegisterFactory(url);

    }

}
