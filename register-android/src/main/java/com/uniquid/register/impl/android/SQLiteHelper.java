package com.uniquid.register.impl.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Beatrice Formai on 02/10/17
 *         for Uniquid Inc.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "register.db";
    public static final int DB_VERSION = 1;

    //    USER
    public static final String TABLE_USER = "user_channel";
    public static final String USER_CLM_PROVIDER_NAME = "provider_name";
    public static final String USER_CLM_PROVIDER_ADDRESS = "provider_address";
    public static final String USER_CLM_USER_ADDRESS = "user_address";
    public static final String USER_CLM_BITMASK = "bitmask";
    public static final String USER_CLM_REVOKE_ADDRESS = "revoke_address";
    public static final String USER_CLM_REVOKE_TX_ID = "revoke_tx_id";
    public static final String USER_CREATE = "create table " + TABLE_USER + "(" +
            USER_CLM_PROVIDER_NAME + " text not null unique, " +
            USER_CLM_PROVIDER_ADDRESS + " text not null, " +
            USER_CLM_USER_ADDRESS + " text not null, " +
            USER_CLM_BITMASK + " text not null, " +
            USER_CLM_REVOKE_ADDRESS + " text not null, " +
            USER_CLM_REVOKE_TX_ID + " text not null);";

    //    PROVIDER
    public static final String TABLE_PROVIDER = "provider_channel";
    public static final String PROVIDER_CLM_PROVIDER_ADDRESS = "provider_address";
    public static final String PROVIDER_CLM_USER_ADDRESS = "user_address";
    public static final String PROVIDER_CLM_BITMASK = "bitmask";
    public static final String PROVIDER_CLM_REVOKE_ADDRESS = "revoke_address";
    public static final String PROVIDER_CLM_REVOKE_TX_ID = "revoke_tx_id";
    private static final String PROVIDER_CREATE = "create table " + TABLE_PROVIDER + "(" +
            PROVIDER_CLM_PROVIDER_ADDRESS + " text not null, " +
            PROVIDER_CLM_USER_ADDRESS + " text not null, " +
            PROVIDER_CLM_BITMASK + " text not null, " +
            PROVIDER_CLM_REVOKE_ADDRESS + " text not null, " + 
            PROVIDER_CLM_REVOKE_TX_ID + " text not null primary key (" +
            PROVIDER_CLM_PROVIDER_ADDRESS + ", " +
            PROVIDER_CLM_USER_ADDRESS + "));";

    public SQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(USER_CREATE);
        db.execSQL(PROVIDER_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop current table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROVIDER);
        // create new table
        onCreate(db);
    }
}
