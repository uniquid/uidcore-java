package com.uniquid.node.impl;

import com.uniquid.register.RegisterFactory;
import com.uniquid.userclient.UserClientFactory;
import org.bitcoinj.core.NetworkParameters;

import java.io.File;

/**
 * Bean to encapsulate Node configuration data
 */
public interface UniquidNodeConfiguration {

    File getProviderChainFile();

    File getUserChainFile();

    String getNodeName();

    NetworkParameters getNetworkParameters();

    File getProviderFile();

    File getUserFile();

    String getPublicKey();

    long getCreationTime();

    RegisterFactory getRegisterFactory();

    String getRegistryUrl();

    UserClientFactory getUserClientFactory();

}
