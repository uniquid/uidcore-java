package com.uniquid.node.listeners;

import java.util.Date;

import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.user.UserChannel;

public interface UniquidNodeEventListener {
	
	void onProviderContractCreated(ProviderChannel providerChannel);
	
	void onProviderContractRevoked(ProviderChannel providerChannel);
	
	void onUserContractCreated(UserChannel userChannel);
	
	void onUserContractRevoked(UserChannel userChannel);
	
	void onSyncStarted(final int blocks);
	
	void onSyncProgress(double pct, final int blocksSoFar, Date date);
	
	void onSyncEnded();

}
