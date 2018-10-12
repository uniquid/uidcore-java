package com.uniquid.node.impl;

import com.uniquid.register.RegisterFactory;
import com.uniquid.userclient.UserClientFactory;
import org.bitcoinj.core.NetworkParameters;

import java.io.File;

/**
 * Bean to encapsulate Node configuration data
 */
public interface UniquidNodeConfiguration {

	public File getProviderChainFile();

	public File getUserChainFile();

	public String getNodeName();

	public NetworkParameters getNetworkParameters();

	public File getProviderFile();

	public File getUserFile();

	public String getPublicKey();

	public long getCreationTime();

	public RegisterFactory getRegisterFactory();

	public String getRegistryUrl();
	
	public UserClientFactory getUserClientFactory();
	
}
