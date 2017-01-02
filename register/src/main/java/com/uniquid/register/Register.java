package com.uniquid.register;

import java.sql.SQLException;
import java.util.List;

public interface Register {

	/**
	 * Insert a new Channel in table
	 * @param channel the channel to insert
	 * @return true if channel has been insered, false if an error occurred
	 * */
	boolean insertChannel(Channel channel) throws Exception;

	/**
	 * Delete a Channel from the table
	 * 
	 * @param channel
	 *            the channel to delete
	 * @return true if the channel has been deleted, false if an error occurred
	 * @throws SQLException 
	 */
	boolean deleteChannel(Channel channel) throws Exception;

	/**
	 * Retrieve a Channel from its name
	 * 
	 * @param name
	 *            the name of the channel (machine)
	 * @param channel
	 *            the object to fill with the informations
	 * @return 0 if the channel (machine) exist, -1 if an error occurred
	 * @throws SQLException 
	 */
	Channel getChannel(String name) throws Exception;

	/**
	 * Retrieve a Channel from its bitcoin address
	 * 
	 * @param address
	 *            the address of the channel (machine)
	 * @return 0 if the channel (machine) exist, -1 if an error occurred
	 * @throws SQLException 
	 */
	Channel getChannelByAddress(String address) throws Exception;

	/**
	 * Insert a new UContext in table
	 * 
	 * @param uContext
	 *            context to add in the table
	 * @return true if new record has been added, false otherwise
	 * @throws SQLException 
	 */
	boolean insertUContext(UContext uContext) throws Exception;

	/**
	 * Delete a UContext
	 * 
	 * @param uContext
	 *            to delete from table
	 * @return true if new record has been deleted, false otherwise
	 * @throws SQLException 
	 */
	boolean deleteUContext(UContext uContext) throws Exception;

	/**
	 * Insert a new Node in the table
	 * 
	 * @param node
	 *            the record to insert
	 * @return true if new record has been added, false otherwise
	 * @throws SQLException 
	 */
	boolean insertNode(Node node) throws Exception;

	/**
	 * Delete a Node
	 * 
	 * @param node
	 *            to delete from table
	 * @return true if new record has been deleted, false otherwise
	 * @throws SQLException 
	 */
	boolean deleteNode(Node node) throws Exception;

	/**
	 * Retrieve a node from his xpub
	 * 
	 * @param xpub
	 *            filter condition
	 * @return retrieved node, can be empty if table doesn't contain the record
	 * @throws SQLException 
	 */
	Node getNode(String xpub) throws Exception;

	/**
	 * Insert a new Contract in the registry
	 * 
	 * @param contract
	 *            the contract to add
	 * @return true if new contract has been added, false otherwise
	 * @throws SQLException 
	 */
	boolean insertContract(Contract contract) throws Exception;

	/**
	 * Delete a Contract
	 * 
	 * @param contract
	 *            the contract to delete from registry
	 * @return true if the contract has been deleted, false otherwise
	 * @throws SQLException 
	 */
	boolean deleteContract(Contract contract) throws Exception;

	/**
	 * Get a registry
	 * 
	 * @param registry
	 *            the name of the table
	 * @param filter
	 *            the filter to apply as condition
	 * @param list
	 *            the list to fill with retrieved records
	 * @throws SQLException 
	 */
	List<?> getRegistry(Class className, String filter) throws Exception;

}