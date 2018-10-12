package com.uniquid.register.impl.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

/**
 * Data Access Object concrete class implementation of {@code ProviderRegister} and {@code UserRegister} that uses
 * SQLite as data store.
 */
public class SQLiteRegister implements ProviderRegister, UserRegister {
	
	public static final String CREATE_PROVIDER_TABLE = "create table provider_channel (provider_address text not null, user_address text not null, bitmask text not null, revoke_address text not null, revoke_tx_id text not null, creation_time integer not null, since integer, until integer, path text not null, primary key (provider_address, user_address));";

	public static final String CREATE_USER_TABLE = "create table user_channel (provider_name text not null, provider_address text not null, user_address text not null, bitmask text not null, revoke_address text not null, revoke_tx_id text not null, since integer, until integer, path text not null, primary key (provider_name, provider_address, user_address));";

	private static final String PROVIDER_CHANNEL_BY_USER = "select provider_address, user_address, bitmask, revoke_address, revoke_tx_id, creation_time, since, until, path from provider_channel where user_address = ?";
	
	private static final String PROVIDER_CHANNEL_BY_REVOKE_ADDRESS = "select provider_address, user_address, bitmask, revoke_address, revoke_tx_id, creation_time, since, until, path from provider_channel where revoke_address = ?";
	
	private static final String PROVIDER_CHANNEL_BY_REVOKE_TXID = "select provider_address, user_address, bitmask, revoke_address, revoke_tx_id, creation_time, since, until, path from provider_channel where revoke_tx_id = ?";
	
	private static final String PROVIDER_INSERT = "insert into provider_channel (provider_address, user_address, bitmask, revoke_address, revoke_tx_id, creation_time, since, until, path) values (?, ?, ?, ?, ?, ?, ?, ?, ?);";
	
	private static final String PROVIDER_DELETE = "delete from provider_channel where provider_address = ? and user_address = ?;";
	
	private static final String PROVIDER_ALL_CHANNEL = "select provider_address, user_address, bitmask, revoke_address, revoke_tx_id, creation_time, since, until, path from provider_channel order by creation_time desc;";
	
	
	private static final String USER_ALL_CHANNEL = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id, since, until, path from user_channel";
	
	private static final String USER_CHANNEL_BY_NAME = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id, since, until, path from user_channel where provider_name = ?;";

	private static final String USER_CHANNEL_BY_ADDRESS = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id, since, until, path from user_channel where provider_address = ?;";

	private static final String USER_CHANNEL_BY_REVOKE_TXID = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id, since, until, path from user_channel where revoke_tx_id = ?;";

	private static final String USER_CHANNEL_BY_REVOKE_ADDRESS = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id, since, until, path from user_channel where revoke_address = ?;";

	private static final String INSERT_USER_CHANNEL = "insert into user_channel (provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id, since, until, path) values (?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private static final String USER_CHANNEL_DELETE = "delete from user_channel where provider_name = ? and provider_address = ? and user_address = ?";

	private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteRegister.class.getName());

	protected BasicDataSource dataSource;
	
	/**
	 * Creates an instance from the connection
	 * @param dataSource the connection to use
	 */
	SQLiteRegister(BasicDataSource dataSource) {
		
		Validate.notNull(dataSource);
		
		this.dataSource = dataSource;
		
	}
	
	/**
	 * Helper method
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private ProviderChannel providerChannelFromResultSet(ResultSet rs) throws SQLException {
		
		ProviderChannel providerChannel = new ProviderChannel();

		providerChannel.setProviderAddress(rs.getString("provider_address"));
		providerChannel.setUserAddress(rs.getString("user_address"));
		providerChannel.setBitmask(rs.getString("bitmask"));
		providerChannel.setRevokeAddress(rs.getString("revoke_address"));
		providerChannel.setRevokeTxId(rs.getString("revoke_tx_id"));
		providerChannel.setCreationTime(rs.getLong("creation_time"));
		providerChannel.setSince(rs.getLong("since"));
		providerChannel.setUntil(rs.getLong("until"));
		providerChannel.setPath(rs.getString("path"));

		return providerChannel;
		
	}
	
	/*
	 * Helper method
	 */
	private ResultSetHandler<ProviderChannel> createProviderResultSetHandler() {
		
		return new ResultSetHandler<ProviderChannel>() {
			
			@Override
			public ProviderChannel handle(ResultSet rs) throws SQLException {
				
				if (rs.next()) {

					return providerChannelFromResultSet(rs);
					
				}
				
				return null;
			}
		};
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ProviderChannel> getAllChannels() throws RegisterException {
		
		ResultSetHandler<List<ProviderChannel>> handler = new ResultSetHandler<List<ProviderChannel>>() {
			
			@Override
			public List<ProviderChannel> handle(ResultSet rs) throws SQLException {
				List<ProviderChannel> providerChannels = new ArrayList<>();
				
				while (rs.next()) {

					ProviderChannel providerChannel = providerChannelFromResultSet(rs);

					providerChannels.add(providerChannel);

				}
				
				return providerChannels;
			}
		};
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
		
			return run.query(PROVIDER_ALL_CHANNEL, handler);
		
		} catch (SQLException ex) {
			
			throw new RegisterException("Exception while getAllChannels()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProviderChannel getChannelByUserAddress(String address) throws RegisterException {
		
		if (!StringUtils.isNotBlank(address)) {
			
			throw new RegisterException("address is not valid");
		
		}
		
		ResultSetHandler<ProviderChannel> handler = createProviderResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(PROVIDER_CHANNEL_BY_USER, handler, address);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getChannelByUserAddress()", ex);
			
		}

	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProviderChannel getChannelByRevokeAddress(String revokeAddress) throws RegisterException {
		
		if (!StringUtils.isNotBlank(revokeAddress)) {
			
			throw new RegisterException("revokeAddress is not valid");
		
		}
		
		ResultSetHandler<ProviderChannel> handler = createProviderResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(PROVIDER_CHANNEL_BY_REVOKE_ADDRESS, handler, revokeAddress);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getChannelByRevokeAddress()", ex);
			
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProviderChannel getChannelByRevokeTxId(String revokeTxId) throws RegisterException {
		
		if (!StringUtils.isNotBlank(revokeTxId)) {
			
			throw new RegisterException("revokeTxId is not valid");
		
		}
		
		ResultSetHandler<ProviderChannel> handler = createProviderResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(PROVIDER_CHANNEL_BY_REVOKE_TXID, handler, revokeTxId);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getChannelByRevokeTxId()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insertChannel(ProviderChannel providerChannel) throws RegisterException {
		
		if (providerChannel == null) throw new RegisterException("providerChannel is null!");
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			run.update(PROVIDER_INSERT, providerChannel.getProviderAddress(),
					providerChannel.getUserAddress(), providerChannel.getBitmask(), providerChannel.getRevokeAddress(),
					providerChannel.getRevokeTxId(), providerChannel.getCreationTime(), providerChannel.getSince(), providerChannel.getUntil(),
					providerChannel.getPath());
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while insertChannel()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteChannel(ProviderChannel providerChannel) throws RegisterException {
		
		if (providerChannel == null) throw new RegisterException("providerChannel is null!");
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			run.update(PROVIDER_DELETE, providerChannel.getProviderAddress(),
					providerChannel.getUserAddress());
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while deleteChannel()", ex);
			
		}
		
	}
	
	/**
	 * Helper method
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private UserChannel userChannelFromResultSet(ResultSet rs) throws SQLException {
		
		UserChannel userChannel = new UserChannel();

		userChannel.setProviderName(rs.getString("provider_name"));
		userChannel.setProviderAddress(rs.getString("provider_address"));
		userChannel.setUserAddress(rs.getString("user_address"));
		userChannel.setBitmask(rs.getString("bitmask"));
		userChannel.setRevokeAddress(rs.getString("revoke_address"));
		userChannel.setRevokeTxId(rs.getString("revoke_tx_id"));
		userChannel.setSince(rs.getLong("since"));
		userChannel.setUntil(rs.getLong("until"));
		userChannel.setPath(rs.getString("path"));

		return userChannel;
		
	}
	
	/*
	 * Helper method
	 */
	private ResultSetHandler<UserChannel> createUserResultSetHandler() {
		
		return new ResultSetHandler<UserChannel>() {
			
			@Override
			public UserChannel handle(ResultSet rs) throws SQLException {
				
				if (rs.next()) {

					return userChannelFromResultSet(rs);
					
				}
				
				return null;
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UserChannel> getAllUserChannels() throws RegisterException {
		
		ResultSetHandler<List<UserChannel>> handler = new ResultSetHandler<List<UserChannel>>() {
			
			List<UserChannel> userChannels = new ArrayList<>();
			
			@Override
			public List<UserChannel> handle(ResultSet rs) throws SQLException {
				
				while (rs.next()) {

					UserChannel userChannel = userChannelFromResultSet(rs);
					
					if(userChannel.isValid()) {
						userChannels.add(userChannel);	
					}

				}
				
				return userChannels;
				
			}
		};
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(USER_ALL_CHANNEL, handler);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getAllUserChannels()", ex);
			
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserChannel getChannelByName(String name) throws RegisterException {
		
		if (!StringUtils.isNotBlank(name)) {
			
			throw new RegisterException("name is not valid");
		
		}
		
		ResultSetHandler<UserChannel> handler = createUserResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(USER_CHANNEL_BY_NAME, handler, name);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getChannelByName()", ex);
			
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserChannel getChannelByProviderAddress(String name) throws RegisterException {
		
		if (!StringUtils.isNotBlank(name)) {
			
			throw new RegisterException("name is not valid");
		
		}
		
		ResultSetHandler<UserChannel> handler = createUserResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(USER_CHANNEL_BY_ADDRESS, handler, name);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getChannelByProviderAddress()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insertChannel(UserChannel userChannel) throws RegisterException {
		
		if (userChannel == null) throw new RegisterException("userchannel is null!");
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			run.update(INSERT_USER_CHANNEL, userChannel.getProviderName(), userChannel.getProviderAddress(),
					userChannel.getUserAddress(), userChannel.getBitmask(), userChannel.getRevokeAddress(),
					userChannel.getRevokeTxId(), userChannel.getSince(), userChannel.getUntil(), userChannel.getPath() );
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while insertChannel()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteChannel(UserChannel userChannel) throws RegisterException {
		
		if (userChannel == null) throw new RegisterException("userchannel is null!");
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			run.update(USER_CHANNEL_DELETE, userChannel.getProviderName(),
					userChannel.getProviderAddress(), userChannel.getUserAddress() );
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while deleteChannel()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserChannel getUserChannelByRevokeTxId(String revokeTxId) throws RegisterException {
		
		if (!StringUtils.isNotBlank(revokeTxId)) {
			
			throw new RegisterException("revokeTxId is not valid");
		
		}
		
		ResultSetHandler<UserChannel> handler = createUserResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(USER_CHANNEL_BY_REVOKE_TXID, handler, revokeTxId);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getUserChannelByRevokeTxId()", ex);
			
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserChannel getUserChannelByRevokeAddress(String revokeAddress) throws RegisterException {
		
		if (!StringUtils.isNotBlank(revokeAddress)) {
			
			throw new RegisterException("revokeAddress is not valid");
		
		}
		
		ResultSetHandler<UserChannel> handler = createUserResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(USER_CHANNEL_BY_REVOKE_ADDRESS, handler, revokeAddress);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getUserChannelByRevokeAddress()", ex);
			
		}

	}
	
	/**
	 * Returns QueryRunner to use for interact with DB
	 */
	protected TransactionAwareQueryRunner getQueryRunner() {
		
		return new TransactionAwareQueryRunner(dataSource);
		
	}

}
