package com.uniquid.core.impl.test;

import java.util.ArrayList;
import java.util.List;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;

public class DummyProviderRegister implements ProviderRegister {

	private ArrayList<ProviderChannel> channels = new ArrayList<ProviderChannel>();
	
	@Override
	public void insertChannel(ProviderChannel providerChannel) throws RegisterException {
		channels.add(providerChannel);
	}
	
	@Override
	public ProviderChannel getChannelByUserAddress(String address) throws RegisterException {
		
		for (ProviderChannel p : channels) {
			
			if (p.getUserAddress().equals(address)) {
				return p;
			}
			
		}
			
		return null;
	}
	
	@Override
	public ProviderChannel getChannelByRevokeTxId(String revokeTxId) throws RegisterException {

		for (ProviderChannel p : channels) {
			
			if (p.getRevokeTxId().equals(revokeTxId)) {
				return p;
			}
			
		}
		
		return null;
	}
	
	@Override
	public ProviderChannel getChannelByRevokeAddress(String revokeAddress) throws RegisterException {
	
		for (ProviderChannel p : channels) {
			
			if (p.getRevokeAddress().equals(revokeAddress)) {
				return p;
			}
			
		}
		
		return null;
	}
	
	@Override
	public List<ProviderChannel> getAllChannels() throws RegisterException {
		return channels;
	}
	
	@Override
	public void deleteChannel(ProviderChannel providerChannel) throws RegisterException {
		channels.remove(providerChannel);
		
	}
	
}
