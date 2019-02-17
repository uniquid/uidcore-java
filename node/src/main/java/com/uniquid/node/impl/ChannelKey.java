package com.uniquid.node.impl;

import org.bitcoinj.crypto.DeterministicKey;

public class ChannelKey {
    private DeterministicKey key;

    public ChannelKey(DeterministicKey key) {
        this.key = key;
    }

    public String getPublicKey() {
        return key.getPublicKeyAsHex();
    }

    public String getPrivateKey() {
        return bytesToHex(key.getPrivKey().toByteArray());
    }

    final private static char[] hexArray = "0123456789abcdef".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
