package com.uniquid.core.impl.test;

import com.uniquid.node.UniquidNode;
import com.uniquid.node.UniquidNodeState;
import com.uniquid.node.exception.NodeException;
import com.uniquid.node.listeners.UniquidNodeEventListener;

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
	public String getHexSeed() {
		// TODO Auto-generated method stub
		return null;
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
	public String signTransaction(String s_tx, String path) throws NodeException {
		
		return s_tx;
		
	}

}
