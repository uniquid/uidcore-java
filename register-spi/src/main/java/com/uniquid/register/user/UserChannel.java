package com.uniquid.register.user;

import java.io.Serializable;
import java.util.Objects;

public class UserChannel implements Serializable, Comparable<Object> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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

    @Override
    public boolean equals(Object object) {

    	if (!(object instanceof UserChannel))
    		return false;
    	
    	if (this == object)
    		return true;
    	
    	UserChannel userChannel = (UserChannel) object;
    	
    	return Objects.equals(providerName, userChannel.providerName) &&
    			Objects.equals(providerAddress, userChannel.providerAddress) &&
    			Objects.equals(userAddress, userChannel.userAddress) &&
    			Objects.equals(bitmask, userChannel.bitmask) &&
    			Objects.equals(revokeAddress, userChannel.revokeAddress) &&
    			Objects.equals(revokeTxId, userChannel.revokeTxId);
    }
    
    @Override
    public int hashCode() {
    	
    	return Objects.hash(providerName, providerAddress, userAddress, bitmask, revokeAddress, revokeTxId);
    
    }

    @Override
    public int compareTo(Object object) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        UserChannel userChannel = (UserChannel) object;
        if(this.providerName == null && userChannel.getProviderName() == null) return EQUAL;
        else if(this.providerName != null && userChannel.getProviderName() == null) return AFTER;
        else if(this.providerName == null && userChannel.getProviderName() != null) return  BEFORE;
        else return this.providerName.compareToIgnoreCase(userChannel.getProviderName());
    }

}