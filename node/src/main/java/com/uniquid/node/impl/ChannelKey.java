package com.uniquid.node.impl;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;

public class ChannelKey {
    private DeterministicKey key;
    private NetworkParameters networkParameters;

    public ChannelKey(DeterministicKey key, NetworkParameters networkParameters) {
        this.key = key;
        this.networkParameters = networkParameters;
    }

    public String getPublicKey() {
        return key.serializePubB58(networkParameters);
    }

    public String getPrivateKey() {
        return key.serializePrivB58(networkParameters);
    }
}
