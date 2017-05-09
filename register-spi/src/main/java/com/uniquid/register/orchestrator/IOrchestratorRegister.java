package com.uniquid.register.orchestrator;

import java.util.List;

import com.uniquid.register.exception.RegisterException;

/**
 * Data Access Object pattern for Orchestrator.
 * 
 * Is used to separate low level data accessing API from high level business services.
 */
public interface IOrchestratorRegister {

	/*
	 * Context stuff
	 */
	
	/**
	 * Creates a {@code Context} by persisting its content in the data store.
	 * @param context the Context to persist.
	 * @throws RegisterException in case a problem occurs.
	 */
    public void insertContext(Context context) throws RegisterException;
    
    /**
	 * Updates a {@code Context} by changing its content in the data store.
	 * @param context the Context to change.
	 * @throws RegisterException in case a problem occurs.
	 */
    public void updateContext(Context context) throws RegisterException;
    
    /**
	 * Deletes a {@code Context} from the data store by its name
	 * @param name the context name to delete from the data store.
	 * @throws RegisterException in case a problem occurs.
	 */
    public void deleteContextByName(String name) throws RegisterException;
    
    /**
	 * Deletes a {@code Context} from the data store by its xpub.
	 * @param xpub the context xpub to delete from the data store.
	 * @throws RegisterException in case a problem occurs.
	 */
    public void deleteContextByXpub(String xpub) throws RegisterException;
    
    /**
	 * Returns a List containing all the {@code Context} present in the data store.
	 * In case no {@code Context} is present an empty list is returned.
	 * @return a List containing all the {@code Context} present in the data store or an empty List.
	 * @throws RegisterException in case a problem occurs.
	 */
    public List<Context> getAllContexts();
    
    /**
	 * Return a {@code Context} from its name or null if no element is found.
	 * @param name the name of the context
	 * @return a {@code Context} from its name or null if no element is found.
	 * @throws RegisterException in case a problem occurs.
	 */
    public Context getContextByName(String name) throws RegisterException;
    
    /**
	 * Return a {@code Context} from its xpub or null if no element is found.
	 * @param xpub the xpub of the context
	 * @return a {@code Context} from its xpub or null if no element is found.
	 * @throws RegisterException in case a problem occurs.
	 */
    public Context getContextByXpub(String xpub) throws RegisterException;
    
    /*
     * Node stuff
     */
    
    /**
	 * Creates a {@code Node} by persisting its content in the data store.
	 * @param node the Node to persist.
	 * @throws RegisterException in case a problem occurs.
	 */
    public void insertNode(Node node) throws RegisterException;
    
    /**
   	 * Deletes a {@code Node} from the data store by its name
   	 * @param node the node to delete from the data store.
   	 * @throws RegisterException in case a problem occurs.
   	 */
    public void deleteNode(Node node) throws RegisterException;
    
    /**
	 * Returns a List containing all the {@code Node} present in the data store.
	 * In case no {@code Node} is present an empty list is returned.
	 * @return a List containing all the {@code Node} present in the data store or an empty List.
	 * @throws RegisterException in case a problem occurs.
	 */
    public List<Node> getAllNodes();
    
    /**
	 * Returns a {@code Node} from its xpub or null if no element is found.
	 * @param xpub the xpub of the node
	 * @return a {@code Node} from its xpub or null if no element is found.
	 * @throws RegisterException in case a problem occurs.
	 */
    public Node getNodeByXpub(String xpub) throws RegisterException;
    
    /**
	 * Returns a {@code Node} from its name or null if no element is found.
	 * @param name the name of the node
	 * @return a {@code Node} from its name or null if no element is found.
	 * @throws RegisterException in case a problem occurs.
	 */
    public Node getNodeByName(String name) throws RegisterException;
    
    /**
   	 * Returns a list of {@code Node} from their context name or null if no element is found.
   	 * @param context_name the name of the context 
   	 * @return a {@code Node} from its context name or null if no element is found.
   	 * @throws RegisterException in case a problem occurs.
   	 */
    public List<Node> getNodesByContext(String context_name) throws RegisterException;
    
    /**
	 * Updates a {@code Node} by changing its content in the data store.
	 * @param node the node to change.
	 * @throws RegisterException in case a problem occurs.
	 */
    public void updateNode(Node node) throws RegisterException;
    
    /*
     * Imprinted node stuff
     */

    /**
	 * Returns a List containing all the {@code ImprintedNode} present in the data store.
	 * In case no {@code ImprintedNode} is present an empty list is returned.
	 * @return a List containing all the {@code ImprintedNode} present in the data store or an empty List.
	 * @throws RegisterException in case a problem occurs.
	 */
    public List<ImprintedNode> getAllImprinted();
    
    /**
	 * Returns an {@code ImprintedNode} from its xpub or null if no element is found.
	 * @param xpub the xpub of the imprinted node
	 * @return a {@code ImprintedNode} from its xpub or null if no element is found.
	 * @throws RegisterException in case a problem occurs.
	 */
    public ImprintedNode getImprintedByXpub(String xpub) throws RegisterException;
    
    /**
	 * Creates an {@code ImprintedNode} by persisting its content in the data store.
	 * @param node the Node to persist.
	 * @throws RegisterException in case a problem occurs.
	 */
    public void insertImprinted(ImprintedNode node) throws RegisterException;
    
    /**
   	 * Deletes an {@code ImprintedNode} from the data store by its node
   	 * @param node the imprinted node to delete from the data store.
   	 * @throws RegisterException in case a problem occurs.
   	 */
    public void deleteImprinted(ImprintedNode node) throws RegisterException;
    
    
    /*
     * Contract stuff
     */
    
    /**
	 * Creates a {@code Contract} by persisting its content in the data store.
	 * @param contract the contract to persist.
	 * @throws RegisterException in case a problem occurs.
	 */
    public void insertContract(Contract contract) throws RegisterException;
    
    /**
   	 * Deletes a {@code Contract} from the data store by its value
   	 * @param contract the contract to delete from the data store.
   	 * @throws RegisterException in case a problem occurs.
   	 */
    public void deleteContract(Contract contract) throws RegisterException;
    
    /**
	 * Returns a List containing all the {@code Contract} present in the data store.
	 * In case no {@code Contract} is present an empty list is returned.
	 * @return a List containing all the {@code Contract} present in the data store or an empty List.
	 * @throws RegisterException in case a problem occurs.
	 */
    public List<Contract> getAllContracts();
    
    /**
	 * Returns a list of {@code Contract} from their context name or null if no element is found.
	 * @param context_name the name of the context
	 * @return a list of {@code Contract} from its context name or null if no element is found.
	 * @throws RegisterException in case a problem occurs.
	 */
    public List<Contract> getContractsByContextName(String context_name);
    
    /**
	 * Returns a {@code Contract} from transaction id or null if no element is found.
	 * @param txid the id of the transaction
	 * @return a {@code Contract} from from transaction id or null if no element is found.
	 * @throws RegisterException in case a problem occurs.
	 */
    public Contract getContractByTxid(String txid);
    
    /**
	 * Updates a {@code Contract} by changing its content in the data store.
	 * @param txid the transaction id of the contract to change
	 * @throws RegisterException in case a problem occurs.
	 */
    public void updateContract(String txid) throws RegisterException;
    
}
