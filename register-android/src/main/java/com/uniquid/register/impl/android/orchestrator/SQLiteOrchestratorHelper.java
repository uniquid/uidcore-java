package com.uniquid.register.impl.android.orchestrator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteOrchestratorHelper extends SQLiteOpenHelper {
	
	public static final String DB_NAME = "register.db";
    public static final int DB_VERSION = 1;
    
    // CONTEXTS TABLE
    public static final String TABLE_CONTEXT = "contexts";
    public static final String CONTEXT_CLM_NAME = "name";
    public static final String CONTEXT_CLM_XPUB = "xpub";
    private static final String CONTEXT_CREATE = "create table " + TABLE_CONTEXT + "(" +
            CONTEXT_CLM_NAME + " text not null unique, " +
            CONTEXT_CLM_XPUB + " text not null primary key);";

    // NODES TABLE
    public static final String TABLE_NODES = "nodes";
    public static final String NODES_CLM_NAME = "name";
    public static final String NODES_CLM_XPUB = "xpub";
    public static final String NODES_CLM_TS = "timestamp";
    public static final String NODES_CLM_RECIPE = "recipe";
    public static final String NODES_CLM_PATH = "path";
    private static final String NODES_CREATE = "create table " + TABLE_NODES + "(" +
            NODES_CLM_NAME + " text not null, " +
            NODES_CLM_XPUB + " text not null primary key, " +
            NODES_CLM_TS + " integer not null, " +
            NODES_CLM_RECIPE + " text not null, " +
            NODES_CLM_PATH + " text not null);";

    // CONTRACTS TABLE
    public static final String TABLE_CONTRACTS = "contracts";
    public static final String CONTRACT_CLM_CONTEXT = "context_id";
    public static final String CONTRACT_CLM_USER = "user_id";
    public static final String CONTRACT_CLM_PROVIDER = "provider_id";
    public static final String CONTRACT_CLM_TS_BORN = "timestamp_born";
    public static final String CONTRACT_CLM_TS_EXPIRATION = "timestamp_expiration";
    public static final String CONTRACT_CLM_RECIPE = "recipe";
    public static final String CONTRACT_CLM_TXID = "txid";
    public static final String CONTRACT_CLM_ANNULMENT = "annulment";
    public static final String CONTRACT_CLM_REVOCATED = "revocated";
    private static final String CONTRACT_CREATE = "create table " + TABLE_CONTRACTS + "(" +
            CONTRACT_CLM_CONTEXT + " text not null, " +
            CONTRACT_CLM_USER + " text not null, " +
            CONTRACT_CLM_PROVIDER + " text not null, " +
            CONTRACT_CLM_TS_BORN + " integer not null, " +
            CONTRACT_CLM_TS_EXPIRATION + " integer not null, " +
            CONTRACT_CLM_RECIPE + " text not null, " +
            CONTRACT_CLM_TXID + " text not null primary key, " +
            CONTRACT_CLM_ANNULMENT + " text not null, " +
            CONTRACT_CLM_REVOCATED + " numeric not null, " +
            "foreign key(" + CONTRACT_CLM_CONTEXT + ") references " + TABLE_CONTEXT + "(" + CONTEXT_CLM_XPUB + "), " +
            "foreign key(" + CONTRACT_CLM_USER + ") references " + TABLE_NODES + "(" + NODES_CLM_XPUB + "), " +
            "foreign key(" + CONTRACT_CLM_PROVIDER + ") references " + TABLE_NODES + "(" + NODES_CLM_XPUB + "));";


    // IMPRINTED NODES
    public static final String TABLE_IMPRINTED = "imprinted_nodes";
    public static final String IMPRINTED_CLM_XPUB = "xpub";
    public static final String IMPRINTED_CLM_NAME = "name";
    public static final String IMPRINTED_CLM_OWNER = "owner";
    public static final String IMPRINTED_CLM_TXID = "txid";
    private static final String IMPRINTED_CREATE = "create table " + TABLE_IMPRINTED + "(" +
            IMPRINTED_CLM_XPUB + " text not null unique, " +
            IMPRINTED_CLM_NAME + " text not null, " +
            IMPRINTED_CLM_OWNER + " text not null, " +
            IMPRINTED_CLM_TXID + " text not null unique);";
    

	public SQLiteOrchestratorHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		// create tables
		sqLiteDatabase.execSQL(NODES_CREATE);
		sqLiteDatabase.execSQL(CONTEXT_CREATE);
		sqLiteDatabase.execSQL(CONTRACT_CREATE);
		sqLiteDatabase.execSQL(IMPRINTED_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
		// drop current tables
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTEXT);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTRACTS);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_IMPRINTED);

        // create new tables
        onCreate(sqLiteDatabase);
	}

}
