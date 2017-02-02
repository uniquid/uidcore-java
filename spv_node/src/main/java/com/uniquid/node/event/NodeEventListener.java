package com.uniquid.node.event;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.node.state.NodeStateContext;

/**
 * This class redirects events from BitcoinJ wallet to our current node state.
 * 
 * @author giuseppe
 *
 */
public class NodeEventListener implements WalletCoinsSentEventListener, WalletCoinsReceivedEventListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventListener.class.getName());
	
	private NodeStateContext nodeStateContext;
	
	/**
	 * Construct a new NodeEventListener
	 * 
	 * @param nodeStateContext
	 */
	public NodeEventListener(NodeStateContext nodeStateContext) {
		this.nodeStateContext = nodeStateContext;
	}

	@Override
	public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		LOGGER.info("onCoinsReceived() " + tx.getHashAsString());
		nodeStateContext.onCoinsReceived(wallet, tx, prevBalance, newBalance);
	}

	@Override
	public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		LOGGER.info("onCoinsSent() " + tx.getHashAsString());
		nodeStateContext.onCoinsSent(wallet, tx, prevBalance, newBalance);
	}

}
