package com.uniquid.register.orchestrator;

import java.util.List;

import com.uniquid.register.exception.RegisterException;

public interface IOrchestratorRegister {

	//CONTEXT
    public void insertContext(Context context) throws RegisterException;
    
    public void updateContext(Context context) throws RegisterException;
    
    public void deleteContextByName(String name) throws RegisterException;
    
    public void deleteContextByXpub(String xpub) throws RegisterException;
    
    public List<Context> getAllContexts();
    
    public Context getContextByName(String name) throws RegisterException;
    
    Context getContextByXpub(String xpub) throws RegisterException;
    
    
    //NODE
    public void insertNode(Node node) throws RegisterException;
    
    public void deleteNode(Node node) throws RegisterException;
    
    public List<Node> getAllNodes();
    
    public Node getNodeByXpub(String xpub) throws RegisterException;
    
    public Node getNodeByName(String name) throws RegisterException;
    
    public List<Node> getNodesByContext(String context_name) throws RegisterException;
    
    public void updateNode(Node node) throws RegisterException;
    
    
    //IMPRINTED NODE
    public List<ImprintedNode> getAllImprinted();
    
    public ImprintedNode getImprintedByXpub(String xpub) throws RegisterException;
    
    public void insertImprinted(ImprintedNode node) throws RegisterException;
    
    public void deleteImprinted(ImprintedNode node) throws RegisterException;
    
    
    //CONTRACT
    public void insertContract(Contract contract) throws RegisterException;
    
    public void deleteContract(Contract contract) throws RegisterException;
    
    public List<Contract> getAllContracts();
    
    public List<Contract> getContractsByContextName(String context_name);
    
    public Contract getContractByTxid(String txid);
    
    public void updateContract(String txid) throws RegisterException;
    
}
