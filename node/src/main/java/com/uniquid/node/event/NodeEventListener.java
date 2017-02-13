package com.uniquid.node.event;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.node.impl.UniquidNodeImpl;

/**
 * This class redirects events from BitcoinJ wallet to our current node state.
 * 
 * @author giuseppe
 *
 */
public class NodeEventListener implements WalletCoinsSentEventListener, WalletCoinsReceivedEventListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventListener.class.getName());
	
	private UniquidNodeImpl uniquidNode;
	
	/**
	 * Construct a new NodeEventListener
	 * 
	 * @param nodeStateContext
	 */
	public NodeEventListener(UniquidNodeImpl uniquidNode) {
		this.uniquidNode = uniquidNode;
	}

	@Override
	public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		LOGGER.info("onCoinsReceived() " + tx.getHashAsString());
		uniquidNode.onCoinsReceived(wallet, tx);
	}

	@Override
	public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
		
		LOGGER.info("onCoinsSent() " + tx.getHashAsString());
		uniquidNode.onCoinsSent(wallet, tx);
	}

}
