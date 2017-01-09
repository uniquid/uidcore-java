package com.uniquid.register.provider;

import java.util.BitSet;

/**
 * Created by Beatrice Formai on 03/01/17, for Uniquid Inc.
 */
public class ProviderChannel {
    private String providerAddress;
    private String userAddress;
    private BitSet bitmask;

    public ProviderChannel(){

    }

    public ProviderChannel(String providerAddress, String userAddress, BitSet bitmask){
        this.providerAddress = providerAddress;
        this.userAddress = userAddress;
        this.bitmask = bitmask;
    }

    public void setProviderAddress(String providerAddress) {
        this.providerAddress = providerAddress;
    }

    public String getProviderAddress() {
        return providerAddress;
    }

    public void setUserAddress(String clientAddress) {
        this.userAddress = clientAddress;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public BitSet getBitmask() {
        return bitmask;
    }

    public void setBitmask(String bitmask) {
        this.bitmask = toBitset(bitmask);
    }

    public BitSet toBitset(String bitmask){
        BitSet bitset = new BitSet(bitmask.length());

        for (int i = 0; i < bitmask.length(); i++) {
            bitset.set(i, bitmask.charAt(i) == '1' ? true : false );
        }

        return bitset;
    }

    public String fromBitset(){
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < this.bitmask.length(); i++) {
            buffer.append(this.bitmask.get(i) == true ? '1' : '0');
        }

        return buffer.toString();
    }
}
