package com.uniquid.register.impl.mem;

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

public class MemRegister implements Register {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MemRegister.class.getName());
	
	public MemRegister() {
	
	}
	
	// MANAGE CHANNEL TABLE

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#insertChannel(com.uniquid.register.Channel)
	 */
    @Override
	public boolean insertChannel(Channel channel) throws Exception {

    		return true;

    }

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#deleteChannel(com.uniquid.register.Channel)
	 */
	@Override
	public boolean deleteChannel(Channel channel) throws SQLException {

		return true;
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#getChannel(java.lang.String, com.uniquid.register.Channel)
	 */
	@Override
	public Channel getChannel(String name) throws SQLException {

		return null;
		
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#getChannelByAddress(java.lang.String)
	 */
	@Override
	public Channel getChannelByAddress(String address) throws SQLException {
		
		return null;
	}

	// MANAGE TABLE UCONTEXT

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#insertUContext(com.uniquid.register.UContext)
	 */
	@Override
	public boolean insertUContext(UContext uContext) throws SQLException {

		return true;
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#deleteUContext(com.uniquid.register.UContext)
	 */
	@Override
	public boolean deleteUContext(UContext uContext) throws SQLException {
		
		return true;
		
	}

	/**
	 * Retrieve a list of all the UContext stored in the registry
	 * 
	 * @param uContexts
	 *            list to fill with the contexts
	 * @throws SQLException 
	 */
	private List<UContext> getAllUContext() throws SQLException {
		
		return null;

	}

	// MANAGE TABLE UNODE

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#insertNode(com.uniquid.register.Node)
	 */
	@Override
	public boolean insertNode(Node node) throws SQLException {
		
		return true;
		
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#deleteNode(com.uniquid.register.Node)
	 */
	@Override
	public boolean deleteNode(Node node) throws SQLException {

		return true;
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#getNode(java.lang.String)
	 */
	@Override
	public Node getNode(String xpub) throws SQLException {
		
		return null;

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
		
		return null;
	}

	// MANAGE TABLE CONTRACTS

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#insertContract(com.uniquid.register.Contract)
	 */
	@Override
	public boolean insertContract(Contract contract) throws SQLException {

		return true;
	}

	/* (non-Javadoc)
	 * @see com.uniquid.register.impl.Register#deleteContract(com.uniquid.register.Contract)
	 */
	@Override
	public boolean deleteContract(Contract contract) throws SQLException {
		
		return true;
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
		
		return null;
		
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
		 
		 return new ArrayList<>();
	}
	
}