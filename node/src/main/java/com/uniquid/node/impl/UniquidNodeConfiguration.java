package com.uniquid.node.impl;

import java.io.File;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.DeterministicSeed;

import com.uniquid.register.RegisterFactory;

public class UniquidNodeConfiguration {

	private File providerChainFile;
	private File userChainFile;
	private String nodeName;

	private NetworkParameters networkParameters;
	private File providerFile;
	private File userFile;

	private String publicKey;
	private long creationTime;

	private RegisterFactory registerFactory;

	private DeterministicSeed detSeed;

	public File getProviderChainFile() {
		return providerChainFile;
	}

	public void setProviderChainFile(File providerChainFile) {
		this.providerChainFile = providerChainFile;
	}

	public File getUserChainFile() {
		return userChainFile;
	}

	public void setUserChainFile(File userChainFile) {
		this.userChainFile = userChainFile;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public NetworkParameters getNetworkParameters() {
		return networkParameters;
	}

	public void setNetworkParameters(NetworkParameters networkParameters) {
		this.networkParameters = networkParameters;
	}

	public File getProviderFile() {
		return providerFile;
	}

	public void setProviderFile(File providerFile) {
		this.providerFile = providerFile;
	}

	public File getUserFile() {
		return userFile;
	}

	public void setUserFile(File userFile) {
		this.userFile = userFile;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public RegisterFactory getRegisterFactory() {
		return registerFactory;
	}

	public void setRegisterFactory(RegisterFactory registerFactory) {
		this.registerFactory = registerFactory;
	}

	public DeterministicSeed getDetSeed() {
		return detSeed;
	}

	public void setDetSeed(DeterministicSeed detSeed) {
		this.detSeed = detSeed;
	}

}
