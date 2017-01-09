package com.uniquid.register.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.register.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;


public class SQLiteRegister implements ProviderRegister {
	
	public static final String TABLE_PROVIDER = "provider_channel";
    public static final String PROVIDER_CLM_PROVIDER_ADDRESS = "provider_address";
    public static final String PROVIDER_CLM_USER_ADDRESS = "user_address";
    public static final String PROVIDER_CLM_BITMASK = "bitmask";
	private static final String PROVIDER_CREATE = "create table " + TABLE_PROVIDER + "(" + PROVIDER_CLM_PROVIDER_ADDRESS +
            " text not null, " + PROVIDER_CLM_USER_ADDRESS + " text not null, " +
            PROVIDER_CLM_BITMASK + " text not null);";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteRegister.class.getName());
	
	private Connection connection;
	
	public SQLiteRegister(Connection connection) {
	
		this.connection = connection;
	
	}
	
	/**
     * Retrieve Channel information
     * */
    public ProviderChannel getChannelByUserAddress(String address) throws RegisterException {
//        ProviderChannel providerChannel = new ProviderChannel();
//        db = dbHelper.getReadableDatabase();
//        Cursor cursor = db.rawQuery("select * from " + SQLiteHelperProvider.TABLE_PROVIDER +
//                " where " + SQLiteHelperProvider.PROVIDER_CLM_USER_ADDRESS + " = ?",
//                new String[]{address});
//        if(cursor.moveToFirst()){
//            providerChannel.setProviderAddress(cursor.getString(0));
//            providerChannel.setUserAddress(cursor.getString(1));
//            providerChannel.setBitmask(cursor.getString(2));
//            cursor.close();
//        } else {
//            throw new RegisterException("Doesn't exist any record with specified name");
//        }
//        return providerChannel;
    	
    		return null;
    }

    public void insertChannel(ProviderChannel providerChannel) throws RegisterException{
//        db = dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(SQLiteHelperProvider.PROVIDER_CLM_PROVIDER_ADDRESS, providerChannel.getProviderAddress());
//        values.put(SQLiteHelperProvider.PROVIDER_CLM_USER_ADDRESS, providerChannel.getUserAddress());
//        values.put(SQLiteHelperProvider.PROVIDER_CLM_BITMASK, providerChannel.fromBitset());
//        long db_index = db.insert(SQLiteHelperProvider.TABLE_PROVIDER, null, values);
//        if(db_index < 0)
//            throw new RegisterException("Error inserting new channel");
    }

    public void deleteChannel(ProviderChannel providerChannel) throws RegisterException{
//        db = dbHelper.getWritableDatabase();
//        int d = db.delete(SQLiteHelperProvider.TABLE_PROVIDER,
//                SQLiteHelperProvider.PROVIDER_CLM_PROVIDER_ADDRESS + " = ?",
//                new String[]{providerChannel.getProviderAddress()});
//        if(d == 0)
//            throw new RegisterException("Channel not present");
    }
	
//	private List<Contract> getContracts(String filter) throws SQLException {
//		
//		PreparedStatement statement = connection.prepareStatement(CONTRACT_SEARCH);
//		
//		try {
//			
//			statement.setString(1, filter);
//			
//			ResultSet rs = statement.executeQuery();
//			
//			List<Contract> contracts = new ArrayList<Contract>();
//			
//			while (rs.next()) {
//	
//				Contract contract = new Contract();
//				
//				contract.setContext_name(rs.getString("name"));
//				contract.setUser_name(rs.getString("user_name"));
//				contract.setMachine_name(rs.getString("machine_name"));
//				contract.setTimestamp_born(rs.getLong("timestamp_born"));
//				contract.setTimestamp_expiration(rs.getLong("timestamp_expiration"));
//				contract.setRecipe(rs.getString("recipe"));
//				contract.setTxid(rs.getString("txid"));
//	
//				contracts.add(contract);
//	
//			}
//			
//			return contracts;
//		
//		} finally {
//			
//			statement.close();
//			
//		}
//		
//	}

	protected void finalize() throws Throwable {

		try {

			connection.close();

		} catch (Throwable ex) {/* do nothing */}
		
		super.finalize();

	}

}
