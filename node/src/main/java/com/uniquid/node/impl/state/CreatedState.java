package com.uniquid.node.impl.state;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;

/**
 * Dummy state to be used when new node is created.
 */
public class CreatedState implements UniquidNodeState {

    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx) {
        throw new IllegalStateException();
    }

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx) {
        throw new IllegalStateException();
    }

    @Override
    public com.uniquid.node.UniquidNodeState getNodeState() {
        return com.uniquid.node.UniquidNodeState.CREATED;
    }

}
