package com.uniquid.register.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	
	private static final String PROVIDER_CREATE_TABLE = "create table provider_channel (provider_address text not null, user_address text not null, bitmask text not null);";

	private static final String PROVIDER_CHANNEL_BY_USER = "select provider_address, user_address, bitmask from provider_channel where user_address = ?";
	
	private static final String PROVIDER_INSERT = "insert into provider_channel (provider_address, user_address, bitmask) values (?, ?, ?);";

	private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteRegister.class.getName());

	private Connection connection;

	public SQLiteRegister(Connection connection) {

		this.connection = connection;

	}

	/**
	 * Retrieve Channel information
	 */
	public ProviderChannel getChannelByUserAddress(String address) throws RegisterException {

		try {
			PreparedStatement statement = connection.prepareStatement(PROVIDER_CHANNEL_BY_USER);

			try {

				statement.setString(1, address);

				ResultSet rs = statement.executeQuery();

				if (rs.next()) {

					ProviderChannel providerChannel = new ProviderChannel();

					providerChannel.setProviderAddress(rs.getString("provider_address"));
					providerChannel.setUserAddress(rs.getString("user_address"));
					providerChannel.setBitmask(rs.getString("bitmask"));

					return providerChannel;
				}

			} finally {

				statement.close();

			}
		} catch (SQLException ex) {
			throw new RegisterException("Error");
		}
		
		return null;

	}

	public void insertChannel(ProviderChannel providerChannel) throws RegisterException {
		
		try {
			PreparedStatement statement = connection.prepareStatement(PROVIDER_INSERT);

			try {

				statement.setString(1, providerChannel.getProviderAddress());
				statement.setString(1, providerChannel.getUserAddress());
				statement.setString(1, "123");

				int rs = statement.executeUpdate();

			} finally {

				statement.close();

			}
			
		} catch (SQLException ex) {
			throw new RegisterException("Error");
		}
		
	}

	public void deleteChannel(ProviderChannel providerChannel) throws RegisterException {
		// db = dbHelper.getWritableDatabase();
		// int d = db.delete(SQLiteHelperProvider.TABLE_PROVIDER,
		// SQLiteHelperProvider.PROVIDER_CLM_PROVIDER_ADDRESS + " = ?",
		// new String[]{providerChannel.getProviderAddress()});
		// if(d == 0)
		// throw new RegisterException("Channel not present");
	}

	// private List<Contract> getContracts(String filter) throws SQLException {
	//
	// PreparedStatement statement =
	// connection.prepareStatement(CONTRACT_SEARCH);
	//
	// try {
	//
	// statement.setString(1, filter);
	//
	// ResultSet rs = statement.executeQuery();
	//
	// List<Contract> contracts = new ArrayList<Contract>();
	//
	// while (rs.next()) {
	//
	// Contract contract = new Contract();
	//
	// contract.setContext_name(rs.getString("name"));
	// contract.setUser_name(rs.getString("user_name"));
	// contract.setMachine_name(rs.getString("machine_name"));
	// contract.setTimestamp_born(rs.getLong("timestamp_born"));
	// contract.setTimestamp_expiration(rs.getLong("timestamp_expiration"));
	// contract.setRecipe(rs.getString("recipe"));
	// contract.setTxid(rs.getString("txid"));
	//
	// contracts.add(contract);
	//
	// }
	//
	// return contracts;
	//
	// } finally {
	//
	// statement.close();
	//
	// }
	//
	// }

	protected void finalize() throws Throwable {

		try {

			connection.close();

		} catch (Throwable ex) {
			/* do nothing */}

		super.finalize();

	}

}
