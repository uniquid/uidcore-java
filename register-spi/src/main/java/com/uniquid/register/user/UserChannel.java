package com.uniquid.register.user;

import java.io.Serializable;

public class UserChannel implements Serializable {

    private String providerName;
    private String providerAddress;
    private String userAddress;
    private String bitmask;
    private String revokeAddress;
    private String revokeTxId;

    public UserChannel(){

    }

    public UserChannel(String providerName, String providerAddress, String userAddress, String bitmask){
        this.providerName = providerName;
        this.providerAddress = providerAddress;
        this.userAddress = userAddress;
        this.bitmask = bitmask;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderAddress(String providerAddress) {
        this.providerAddress = providerAddress;
    }

    public String getProviderAddress() {
        return providerAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public String getBitmask() {
        return bitmask;
    }

    public void setBitmask(String bitmask) {
        this.bitmask = bitmask;
    }
    
    public String getRevokeAddress() {
		return revokeAddress;
	}

	public void setRevokeAddress(String revokeAddress) {
		this.revokeAddress = revokeAddress;
	}
	
	public void setRevokeTxId(String revokeTxId) {
		this.revokeTxId = revokeTxId;
	}
	
	public String getRevokeTxId() {
		return revokeTxId;
	}
    
    public String toString() {
		return "provider address: " + providerAddress + "; user address: " + userAddress + "; bitmask: " + bitmask +
				"; revoke address: " + revokeAddress + "; revokeTxId: " + revokeTxId;
	}

}