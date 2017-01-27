package com.uniquid.spv_node;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeEventListener implements WalletCoinsSentEventListener, WalletCoinsReceivedEventListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventListener.class.getName());
	
	private NodeStateContext nodeStateContext;
	
	public NodeEventListener(NodeStateContext nodeStateContext) {
		this.nodeStateContext = nodeStateContext;
	}

	@Override
	public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		nodeStateContext.getNodeState().onCoinsReceived(wallet, tx, prevBalance, newBalance);
	}

	@Override
	public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		nodeStateContext.getNodeState().onCoinsSent(wallet, tx, prevBalance, newBalance);
	}

}
