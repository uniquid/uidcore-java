package com.uniquid.register.provider;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Beatrice Formai on 03/01/17, for Uniquid Inc.
 */
public class ProviderChannel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String providerAddress;
	private String userAddress;
	private String revokeAddress;
	private String bitmask;
	private String revokeTxId;
	private long creationTime;

	public ProviderChannel() {

	}

	public ProviderChannel(String providerAddress, String userAddress, String bitmask) {
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
	
	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public String toString() {
		return "provider address: " + providerAddress + "; user address: " + userAddress + "; bitmask: " + bitmask +
				"; revoke address: " + revokeAddress + "; revokeTxId: " + revokeTxId + "; creationTime: " + creationTime;
	}
	
	@Override
    public boolean equals(Object object) {

    	if (!(object instanceof ProviderChannel))
    		return false;
    	
    	if (this == object)
    		return true;
    	
    	ProviderChannel providerChannel = (ProviderChannel) object;
    	
    	return Objects.equals(providerAddress, providerChannel.providerAddress) &&
    			Objects.equals(userAddress, providerChannel.userAddress) &&
    			Objects.equals(revokeAddress, providerChannel.revokeAddress) &&
    			Objects.equals(bitmask, providerChannel.bitmask) &&
    			Objects.equals(revokeTxId, providerChannel.revokeTxId) &&
    			creationTime == providerChannel.creationTime;
    }
    
    @Override
    public int hashCode() {
    	
    	return Objects.hash(providerAddress, userAddress, revokeAddress, bitmask, revokeTxId, creationTime);
    
    }

}