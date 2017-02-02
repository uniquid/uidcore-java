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
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

public class SQLiteRegister implements ProviderRegister, UserRegister {

	public static final String TABLE_PROVIDER = "provider_channel";
	public static final String PROVIDER_CLM_PROVIDER_ADDRESS = "provider_address";
	public static final String PROVIDER_CLM_USER_ADDRESS = "user_address";
	public static final String PROVIDER_CLM_BITMASK = "bitmask";
	
	private static final String PROVIDER_CREATE_TABLE = "create table provider_channel (provider_address text not null, user_address text not null, bitmask text not null, revoke_address text not null, revoke_tx_id text not null primary key (provider_address, user_address));";

	private static final String PROVIDER_CHANNEL_BY_USER = "select provider_address, user_address, bitmask, revoke_address, revoke_tx_id from provider_channel where user_address = ?";
	
	private static final String PROVIDER_INSERT = "insert into provider_channel (provider_address, user_address, bitmask, revoke_address, revoke_tx_id) values (?, ?, ?, ?, ?);";
	
	private static final String PROVIDER_DELETE = "delete from provider_channel where provider_address = ? and user_address = ?;";
	
	public static final String PROVIDER_ALL_CHANNEL = "select provider_address, user_address, bitmask, revoke_address , revoke_tx_id from provider_channel;";
	
	public static final String TABLE_USER = "user_channel";
	
	public static final String USER_CREATE_TABLE = "create table user_channel (provider_name text not null, provider_address text not null, user_address text not null, bitmask text not null, revoke_address text not null, revoke_tx_id text not null, primary key (provider_name, provider_address, user_address));";
	
	public static final String USER_ALL_CHANNEL = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id from user_channel";
	
	public static final String USER_CHANNEL_BY_NAME = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id from user_channel where provider_name = ?;";
	
	public static final String USER_CHANNEL_BY_ADDRESS = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id from user_channel where provider_address = ?;";
	
	public static final String INSERT_USER_CHANNEL = "insert into user_channel (provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id) values (?, ?, ?, ?, ?, ?);";
	
	public static final String USER_CHANNEL_DELETE = "delete from user_channel where provider_name = ? and provider_address = ? and user_address = ?";

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
					providerChannel.setRevokeTxId(rs.getString("revoke_tx_id"));

					return providerChannel;
				}

			} finally {

				statement.close();

			}
		} catch (SQLException ex) {
			throw new RegisterException("Exception while getChannelByUserAddress()", ex);
		}
		
		return null;

	}

	public void insertChannel(ProviderChannel providerChannel) throws RegisterException {
		
		try {
			PreparedStatement statement = connection.prepareStatement(PROVIDER_INSERT);

			try {

				statement.setString(1, providerChannel.getProviderAddress());
				statement.setString(2, providerChannel.getUserAddress());
				statement.setString(3, providerChannel.getBitmask());
				statement.setString(4, providerChannel.getRevokeAddress());
				statement.setString(5, providerChannel.getRevokeTxId());

				int rs = statement.executeUpdate();

			} finally {

				statement.close();

			}
			
		} catch (SQLException ex) {
			throw new RegisterException("Exception while insertChannel()", ex);
		}
		
	}

	public void deleteChannel(ProviderChannel providerChannel) throws RegisterException {
		
		try {
			PreparedStatement statement = connection.prepareStatement(PROVIDER_DELETE);

			try {

				statement.setString(1, providerChannel.getProviderAddress());
				statement.setString(2, providerChannel.getUserAddress());

				int rs = statement.executeUpdate();

			} finally {

				statement.close();

			}
			
		} catch (SQLException ex) {
			throw new RegisterException("Exception while deleteChannel()", ex);
		}
	}

	protected void finalize() throws Throwable {

		try {

			connection.close();

		} catch (Throwable ex) {
			/* do nothing */}

		super.finalize();

	}

	@Override
	public List<UserChannel> getAllUserChannels() throws RegisterException {
		
		List<UserChannel> userChannels = new ArrayList<UserChannel>();
		
		try {
			PreparedStatement statement = connection.prepareStatement(USER_ALL_CHANNEL);

			try {

				ResultSet rs = statement.executeQuery();

				while (rs.next()) {

					UserChannel userChannel = new UserChannel();

					userChannel.setProviderName(rs.getString("provider_name"));
					userChannel.setProviderAddress(rs.getString("provider_address"));
					userChannel.setUserAddress(rs.getString("user_address"));
					userChannel.setBitmask(rs.getString("bitmask"));
					userChannel.setRevokeAddress(rs.getString("revoke_address"));
					userChannel.setRevokeTxId(rs.getString("revoke_tx_id"));

					userChannels.add(userChannel);
				}

			} finally {

				statement.close();

			}
		} catch (SQLException ex) {
			throw new RegisterException("Exception while getChannelByUserAddress()", ex);
		}
		
		return userChannels;
	}

	@Override
	public UserChannel getChannelByName(String name) throws RegisterException {
		
		try {
			PreparedStatement statement = connection.prepareStatement(USER_CHANNEL_BY_NAME);

			try {
				
				statement.setString(1, name);

				ResultSet rs = statement.executeQuery();

				if (rs.next()) {

					UserChannel userChannel = new UserChannel();

					userChannel.setProviderName(rs.getString("provider_name"));
					userChannel.setProviderAddress(rs.getString("provider_address"));
					userChannel.setUserAddress(rs.getString("user_address"));
					userChannel.setBitmask(rs.getString("bitmask"));
					userChannel.setRevokeAddress(rs.getString("revoke_address"));
					userChannel.setRevokeTxId(rs.getString("revoke_tx_id"));

					return userChannel;
					
				}

			} finally {

				statement.close();

			}
		} catch (SQLException ex) {
			throw new RegisterException("Exception while getChannelByName()", ex);
		}
		
		return null;
	}
	
	@Override
	public UserChannel getChannelByProviderAddress(String name) throws RegisterException {
		
		try {
			PreparedStatement statement = connection.prepareStatement(USER_CHANNEL_BY_ADDRESS);

			try {
				
				statement.setString(1, name);

				ResultSet rs = statement.executeQuery();

				if (rs.next()) {

					UserChannel userChannel = new UserChannel();

					userChannel.setProviderName(rs.getString("provider_name"));
					userChannel.setProviderAddress(rs.getString("provider_address"));
					userChannel.setUserAddress(rs.getString("user_address"));
					userChannel.setBitmask(rs.getString("bitmask"));
					userChannel.setRevokeAddress(rs.getString("revoke_address"));
					userChannel.setRevokeTxId(rs.getString("revoke_tx_id"));

					return userChannel;
					
				}

			} finally {

				statement.close();

			}
		} catch (SQLException ex) {
			throw new RegisterException("Exception while getChannelByName()", ex);
		}
		
		return null;
	}

	@Override
	public void insertChannel(UserChannel userChannel) throws RegisterException {
		
		try {
			PreparedStatement statement = connection.prepareStatement(INSERT_USER_CHANNEL);

			try {

				statement.setString(1, userChannel.getProviderName());
				statement.setString(2, userChannel.getProviderAddress());
				statement.setString(3, userChannel.getUserAddress());
				statement.setString(4, userChannel.getBitmask());
				statement.setString(5, userChannel.getRevokeAddress());
				statement.setString(6, userChannel.getRevokeTxId());

				int rs = statement.executeUpdate();

			} finally {

				statement.close();

			}
			
		} catch (SQLException ex) {
			throw new RegisterException("Exception while insertChannel()", ex);
		}
		
	}

	@Override
	public void deleteChannel(UserChannel userChannel) throws RegisterException {
		
		try {
			PreparedStatement statement = connection.prepareStatement(USER_CHANNEL_DELETE);

			try {

				statement.setString(1, userChannel.getProviderName());
				statement.setString(2, userChannel.getProviderAddress());
				statement.setString(3, userChannel.getUserAddress());

				int rs = statement.executeUpdate();

			} finally {

				statement.close();

			}
			
		} catch (SQLException ex) {
			throw new RegisterException("Exception while deleteChannel()", ex);
		}
		
	}

	@Override
	public List<ProviderChannel> getAllChannels() throws RegisterException {
		
		List<ProviderChannel> providerChannels = new ArrayList<ProviderChannel>();
		
		try {
			PreparedStatement statement = connection.prepareStatement(PROVIDER_ALL_CHANNEL);

			try {

				ResultSet rs = statement.executeQuery();

				while (rs.next()) {

					ProviderChannel providerChannel = new ProviderChannel();

					providerChannel.setProviderAddress(rs.getString("provider_address"));
					providerChannel.setRevokeAddress(rs.getString("revoke_address"));
					providerChannel.setUserAddress(rs.getString("user_address"));
					providerChannel.setBitmask(rs.getString("bitmask"));
					providerChannel.setRevokeTxId(rs.getString("revoke_tx_id"));

					providerChannels.add(providerChannel);
				}

			} finally {

				statement.close();

			}
		} catch (SQLException ex) {
			throw new RegisterException("Exception while getAllChannels()", ex);
		}
		
		return providerChannels;
	}

}
