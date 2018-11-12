package com.uniquid.core.impl.test;

import com.uniquid.node.UniquidCapability;
import com.uniquid.node.UniquidNode;
import com.uniquid.node.UniquidNodeState;
import com.uniquid.node.exception.NodeException;
import com.uniquid.node.listeners.UniquidNodeEventListener;
import org.bitcoinj.core.Transaction;

import java.util.List;

public class DummyNode implements UniquidNode {

    @Override
    public String getImprintingAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPublicKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNodeName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getCreationTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getSpendableBalance() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void initNode() throws NodeException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNode() throws NodeException {
        // TODO Auto-generated method stub

    }

    @Override
    public UniquidNodeState getNodeState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeUniquidNodeEventListener(UniquidNodeEventListener uniquidNodeEventListener) {
        // TODO Auto-generated method stub

    }

    @Override
    public Transaction createTransaction(final String serializedTx) throws NodeException {
        throw new NodeException("Exception");
    }

    @Override
    public Transaction signTransaction(Transaction tx, List<String> paths) throws NodeException {
        return tx;
    }

    @Override
    public String broadCastTransaction(String serializedTx) throws NodeException {
        return "1234";
    }

    @Override
    public String signMessage(String message, String path) throws NodeException {
        throw new NodeException("Exception");
    }

    @Override
    public UniquidCapability createCapability(String providerName, String userPublicKey, byte[] rights,
                                              long since, long until) throws NodeException {
        throw new NodeException("Exception");
    }

    @Override
    public void receiveProviderCapability(UniquidCapability uniquidCapability) throws NodeException {
        throw new NodeException("Exception");
    }

    @Override
    public void receiveUserCapability(UniquidCapability uniquidCapability, String providerName, String path) throws NodeException {
        throw new NodeException("Exception");

    }

    @Override
    public boolean isNodeReady() {
        return true;
    }

    @Override
    public String getAddressAtPath(String path) throws NodeException {
        throw new NodeException("Exception");
    }

    @Override
    public void recoverUnspent(Transaction tx, List<String> paths) throws NodeException {
        // TODO Auto-generated method stub

    }

}
