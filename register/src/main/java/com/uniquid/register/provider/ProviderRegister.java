package com.uniquid.register.provider;

import java.util.List;

import com.uniquid.register.RegisterException;

public interface ProviderRegister {

	public List<ProviderChannel> getAllChannels() throws RegisterException;

	public ProviderChannel getChannelByUserAddress(String address) throws RegisterException;
	
	public ProviderChannel getChannelByRevokeAddress(String revokeAddress) throws RegisterException;
	
	public ProviderChannel getChannelByRevokeTxId(String revokeTxId) throws RegisterException;

	public void insertChannel(ProviderChannel providerChannel) throws RegisterException;

	public void deleteChannel(ProviderChannel providerChannel) throws RegisterException;
}
