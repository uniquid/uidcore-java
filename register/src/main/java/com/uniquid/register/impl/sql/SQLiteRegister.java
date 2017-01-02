package com.uniquid.register.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.register.Channel;
import com.uniquid.register.Contract;
import com.uniquid.register.Node;
import com.uniquid.register.Register;
import com.uniquid.register.UContext;

public class SQLiteRegister implements Register {
	
	// M2M TABLE
    private static final String MACHINE_CREATE = "CREATE TABLE machine (name text not null unique, " +
    																		"p_address  text not null, c_address text not null);";
    private static final String MACHINE_INSERT = "INSERT INTO machine (name, p_address, c_address) VALUES (?, ?, ?)";
    private static final String MACHINE_DELETE = "DELETE FROM machine WHERE name = ?";
    private static final String MACHINE_SELECT = "SELECT name, p_address, c_address FROM machine WHERE name = ?";
    private static final String MACHINE_SELECT_BY_ADDRESS = "SELECT name, p_address, c_address FROM machine WHERE c_address = ?";

    // CONTEXTS TABLE
    private static final String TABLE_UCONTEXT = UContext.class.getName();
    private static final String UCONTEXT_CREATE = "CREATE TABLE context (name text not null unique, xpub text not null unique);";
    private static final String UCONTEXT_INSERT = "INSERT INTO context (name, xpub) VALUES (?, ?)";
    private static final String UCONTEXT_DELETE = "DELETE FROM context WHERE name = ?";
    private static final String UCONTEXT_SELECT_ALL = "SELECT name, xpub FROM context";

    // CONTRACTS TABLE
    private static final String TABLE_CONTRACTS = Contract.class.getName();
    private static final String CONTRACT_CREATE = "CREATE TABLE contracts (name text not null, user_name text not null, machine_name text not null, timestamp_born integer not null, timestamp_expiration integer not null, recipe text not null, txid text not null unique);";
    private static final String CONTRACT_INSERT = "INSERT INTO contracts (name, user_name, machine_name, timestamp_born, timestamp_expiration, recipe, txid) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String CONTRACT_DELETE = "DELETE FROM contracts WHERE txid = ?";
    private static final String CONTRACT_SEARCH = "SELECT name, user_name, machine_name, timestamp_born, timestamp_expiration, recipe, txid from contracts WHERE name = ?";

    // NODES TABLE
    private static final String TABLE_NODES = Node.class.getName();
    private static final String NODES_CREATE = "CREATE TABLE nodes (context_name text not null, machine_name text not null, machine_xpub text not null unique, timestamp integer not null, recipe text not null);";
    private static final String NODES_INSERT = "INSERT INTO nodes (context_name, machine_name, machine_xpub, timestamp, recipe) VALUES (?, ?, ?, ?, ?)";
    private static final String NODES_DELETE = "DELETE FROM nodes WHERE machine_name = ?";
    private static final String NODES_SELECT = "SELECT context_name, machine_name, machine_xpub, timestamp, recipe FROM nodes WHERE machine_xpub = ?";
    private static final String NODES_SELECT_BY_NAME = "SELECT context_name, machine_name, machine_xpub, timestamp, recipe FROM nodes WHERE context_name = ?";

	private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteRegister.class.getName());
	
	private Connection connection;
	
	public SQLiteRegister(Connection connection) {
	
		this.connection = connection;
	
	}
	
	// MANAGE CHANNEL TABLE

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#insertChannel(com.uniquid.register.Channel)
	 */
    @Override
	public boolean insertChannel(Channel channel) throws Exception {

    		PreparedStatement statement = connection.prepareStatement(MACHINE_INSERT);
    		
    		try {
    			
	    		statement.setString(1, channel.getName());
	    		statement.setString(2, channel.getProviderAddress());
	    		statement.setString(3, channel.getClientAddress());
	    		
	    		statement.executeUpdate();

	    		return true;
    		
    		} finally {
    			
    			statement.close();
    		
    			connection.commit();
    		
    		}

    }

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#deleteChannel(com.uniquid.register.Channel)
	 */
	@Override
	public boolean deleteChannel(Channel channel) throws SQLException {

		PreparedStatement statement = connection.prepareStatement(MACHINE_DELETE);
		
		try {

			statement.setString(1, channel.getName());
			
			statement.executeUpdate();
			
			return true;

		} finally {
			
			statement.close();
			
			connection.commit();

		}
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#getChannel(java.lang.String, com.uniquid.register.Channel)
	 */
	@Override
	public Channel getChannel(String name) throws SQLException {

		PreparedStatement statement = connection.prepareStatement(MACHINE_SELECT);
		
		try {
			statement.setString(1, name);

			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
	
				Channel channel = new Channel();
				
				channel.setName(rs.getString("name"));
				channel.setProviderAddress(rs.getString("p_address"));
				channel.setClientAddress(rs.getString("c_address"));
				
				return channel;
	
			} else {
				
				return null;
			
			}

		} finally {
			
			statement.close();
			
		}
		
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#getChannelByAddress(java.lang.String)
	 */
	@Override
	public Channel getChannelByAddress(String address) throws SQLException {
		
		PreparedStatement statement = connection.prepareStatement(MACHINE_SELECT_BY_ADDRESS);
		
		try {

			statement.setString(1, address);
			
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
	
				Channel channel = new Channel();
				
				channel.setName(rs.getString("name"));
				channel.setProviderAddress(rs.getString("p_address"));
				channel.setClientAddress(rs.getString("c_address"));
				
				return channel;
	
			} else {
				
				return null;
			
			}

		} finally {
			
			statement.close();
		}
	}

	// MANAGE TABLE UCONTEXT

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#insertUContext(com.uniquid.register.UContext)
	 */
	@Override
	public boolean insertUContext(UContext uContext) throws SQLException {

		PreparedStatement statement = connection.prepareStatement(UCONTEXT_INSERT);
		
		try {

			statement.setString(1, uContext.getName());
			statement.setString(2, uContext.getXpub());
			
			statement.executeUpdate();
	
			return true;
			
		} finally {
			
			statement.close();
			
			connection.commit();
			
		}
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#deleteUContext(com.uniquid.register.UContext)
	 */
	@Override
	public boolean deleteUContext(UContext uContext) throws SQLException {
		
		PreparedStatement statement = connection.prepareStatement(UCONTEXT_DELETE);
		
		try {

			statement.setString(1, uContext.getName());
			
			statement.executeUpdate();
			
			return true;

		} finally {
			
			statement.close();
			
			connection.commit();
		
		}
	}

	/**
	 * Retrieve a list of all the UContext stored in the registry
	 * 
	 * @param uContexts
	 *            list to fill with the contexts
	 * @throws SQLException 
	 */
	private List<UContext> getAllUContext() throws SQLException {
		
		PreparedStatement statement = connection.prepareStatement(UCONTEXT_SELECT_ALL);
		
		List<UContext> uContexts = new ArrayList<UContext>();
		
		try {

			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
				
				UContext uContext = new UContext();
				uContext.setName(rs.getString("name"));
				uContext.setXpub(rs.getString("xpub"));
				
				uContexts.add(uContext);
				
			}
			
			return uContexts;

		} finally {
			
			statement.close();
		
		}

	}

	// MANAGE TABLE UNODE

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#insertNode(com.uniquid.register.Node)
	 */
	@Override
	public boolean insertNode(Node node) throws SQLException {
		
		PreparedStatement statement = connection.prepareStatement(NODES_INSERT);
		
		try {

			statement.setString(1, node.getContext_name());
			statement.setString(2, node.getMachine_name());
			statement.setString(3, node.getMachine_xpub());
			statement.setLong(4, node.getTimestamp());
			statement.setString(5, node.getRecipe());
			
			statement.executeUpdate();
	
			return true;
		
		} finally {
			
			statement.close();
			
			connection.commit();
			
		}
		
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#deleteNode(com.uniquid.register.Node)
	 */
	@Override
	public boolean deleteNode(Node node) throws SQLException {

		PreparedStatement statement = connection.prepareStatement(NODES_DELETE);
		
		try {

			statement.setString(1, node.getMachine_name());
			
			statement.executeUpdate();
			
			return true;
		
		} finally {
			
			statement.close();
			
			connection.commit();
			
		}
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#getNode(java.lang.String)
	 */
	@Override
	public Node getNode(String xpub) throws SQLException {
		
		PreparedStatement statement = connection.prepareStatement(NODES_SELECT);
		
		try {
			statement.setString(1, xpub);
			
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
	
				Node node = new Node();
				
				node.setContext_name(rs.getString("context_name"));
				node.setMachine_name(rs.getString("machine_name"));
				node.setMachine_xpub(rs.getString("machine_xpub"));
				node.setTimestamp(rs.getLong("timestamp"));
				node.setRecipe(rs.getString("recipe"));
				
				return node;
	
			} else {
				
				return null;
			
			}
		} finally {

			statement.close();
		
		}

	}

	/**
	 * Retrieve all nodes filtered by filter
	 * 
	 * @param filter
	 *            the filter to apply
	 * @param nodes
	 *            the list to fill with filtered nodes
	 * @return 0 if no errors, != 0 otherwise
	 * @throws SQLException 
	 */
	private List<Node> getNodes(String filter) throws SQLException {
		
		PreparedStatement statement = connection.prepareStatement(NODES_SELECT_BY_NAME);
		
		List<Node> nodes = new ArrayList<Node>();
		
		try {
			
			statement.setString(1, filter);
			
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
	
				Node node = new Node();
				
				node.setContext_name(rs.getString("context_name"));
				node.setMachine_name(rs.getString("machine_name"));
				node.setMachine_xpub(rs.getString("machine_xpub"));
				node.setTimestamp(rs.getLong("timestamp"));
				node.setRecipe(rs.getString("recipe"));
				
				nodes.add(node);
	
			}
			
			return nodes;

		} finally {
			
			statement.close();
		
		}
		
	}

	// MANAGE TABLE CONTRACTS

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#insertContract(com.uniquid.register.Contract)
	 */
	@Override
	public boolean insertContract(Contract contract) throws SQLException {

		PreparedStatement statement = connection.prepareStatement(CONTRACT_INSERT);
		
		try {
		
			statement.setString(1, contract.getContext_name());
			statement.setString(2, contract.getUser_name());
			statement.setString(3, contract.getMachine_name());
			statement.setLong(4, contract.getTimestamp_born());
			statement.setLong(5, contract.getTimestamp_expiration());
			statement.setString(6, contract.getRecipe());
			statement.setString(7, contract.getTxid());
			
			statement.executeUpdate();
	
			return true;
			
		} finally {
			
			statement.close();
			
			connection.commit();
			
		}
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#deleteContract(com.uniquid.register.Contract)
	 */
	@Override
	public boolean deleteContract(Contract contract) throws SQLException {
		
		PreparedStatement statement = connection.prepareStatement(CONTRACT_DELETE);
		
		try {
			
			statement.setString(1, contract.getTxid());
			
			statement.executeUpdate();
			
			return true;
		
		} finally {
			
			statement.close();
			
			connection.commit();
		
		}
	}

	/**
	 * Retrieve a list of filtered Contract
	 * 
	 * @param filter
	 *            the filter to apply in the condition statement of the sql
	 *            query
	 * @return 0 if no errors, != 0 otherwise
	 * @throws SQLException 
	 */
	private List<Contract> getContracts(String filter) throws SQLException {
		
		PreparedStatement statement = connection.prepareStatement(CONTRACT_SEARCH);
		
		try {
			
			statement.setString(1, filter);
			
			ResultSet rs = statement.executeQuery();
			
			List<Contract> contracts = new ArrayList<Contract>();
			
			while (rs.next()) {
	
				Contract contract = new Contract();
				
				contract.setContext_name(rs.getString("name"));
				contract.setUser_name(rs.getString("user_name"));
				contract.setMachine_name(rs.getString("machine_name"));
				contract.setTimestamp_born(rs.getLong("timestamp_born"));
				contract.setTimestamp_expiration(rs.getLong("timestamp_expiration"));
				contract.setRecipe(rs.getString("recipe"));
				contract.setTxid(rs.getString("txid"));
	
				contracts.add(contract);
	
			}
			
			return contracts;
		
		} finally {
			
			statement.close();
			
		}
		
	}

	// GENERIC METHODS

	/**
	 * Update record in a registry
	 * 
	 * @param registry
	 *            name of the table to update
	 * @param record
	 *            the record to update
	 */
	private int updateRecord(String registry, String record) {
		// LOGGER.info("UPDATE", record);
		// db = dbHelper.getWritableDatabase();
		// ContentValues values = new ContentValues();
		// Gson gson = new Gson();
		// int result = -1;
		// switch (registry){
		// case TABLE_NODES: // update node
		// Node node = gson.fromJson(record, Node.class);
		// values.put(NODES_CLM_CONTEXT, node.getContext_name());
		// values.put(NODES_CLM_MACHINE_NAME, node.getMachine_name());
		// values.put(NODES_CLM_XPUB_MACHINE, node.getMachine_xpub());
		// values.put(NODES_CLM_TS, node.getTimestamp());
		// values.put(NODES_CLM_RECIPE, node.getRecipe());
		// result = db.update(TABLE_NODES, values, NODES_CLM_XPUB_MACHINE + " =
		// ?", new String[]{node.getMachine_xpub()});
		// break;
		//
		// case TABLE_UCONTEXT: // update context
		// UContext uContext = gson.fromJson(record, UContext.class);
		// values.put(UCONTEXT_CLM_NAME, uContext.getName());
		// values.put(UCONTEXT_CLM_XPUB, uContext.getXpub());
		// result = db.update(TABLE_UCONTEXT, values, UCONTEXT_CLM_XPUB + " =
		// ?", new String[]{uContext.getXpub()});
		// break;
		//
		// case TABLE_CONTRACTS: // update contract
		// Contract contract = gson.fromJson(record, Contract.class);
		// values.put(CONTRACT_CLM_CONTEXT, contract.getContext_name());
		// values.put(CONTRACT_CLM_USER, contract.getUser_name());
		// values.put(CONTRACT_CLM_MACHINE, contract.getMachine_name());
		// values.put(CONTRACT_CLM_TS_BORN, contract.getTimestamp_born());
		// values.put(CONTRACT_CLM_TS_EXPIRATION,
		// contract.getTimestamp_expiration());
		// values.put(CONTRACT_CLM_RECIPE, contract.getRecipe());
		// values.put(CONTRACT_CLM_TXID, contract.getTxid());
		// result = db.update(TABLE_CONTRACTS, values, CONTRACT_CLM_TXID + " =
		// ?", new String[]{contract.getTxid()});
		// break;
		// }
		// return result;

		return 0;
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#getRegistry(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	public List<?> getRegistry(Class className, String filter) throws SQLException {

		 LOGGER.info("REGISTRY", "getRegistry");
		 
		 if (TABLE_NODES.equals(className.getName())) {
			// tutti i nodi del contesto 'filter'
			 LOGGER.info("REGISTRY", filter);
			 return getNodes(filter);
		 } else if (TABLE_UCONTEXT.equals(className.getName())) {
			// tutti i contesti
			 return getAllUContext();
			 //LOGGER.info("CONTEXTS_REGISTRY", "" + list.size());
		 } else if (TABLE_CONTRACTS.equals(className.getName())) {
			// tutti i contratti relativi al contesto 'filter'
			 return getContracts(filter);
		 }
		 
		 return new ArrayList<>();
	}
	
	protected void finalize() throws Throwable {

		try {

			connection.close();

		} catch (Throwable ex) {/* do nothing */}
		
		super.finalize();

	}
}
