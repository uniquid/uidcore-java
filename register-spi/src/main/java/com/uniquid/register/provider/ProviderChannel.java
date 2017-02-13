package com.uniquid.register.provider;

/**
 * Created by Beatrice Formai on 03/01/17, for Uniquid Inc.
 */
public class ProviderChannel {
	private String providerAddress;
	private String userAddress;
	private String revokeAddress;
	private String bitmask;
	private String revokeTxId;

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
	
	public String toString() {
		return "provider address: " + providerAddress + "; user address: " + userAddress + "; bitmask: " + bitmask +
				"; revoke address: " + revokeAddress + "; revokeTxId: " + revokeTxId;
	}
	
}
