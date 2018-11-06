/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.node.impl;

import com.uniquid.register.RegisterFactory;
import com.uniquid.userclient.UserClientFactory;
import org.bitcoinj.core.NetworkParameters;

import java.io.File;

/**
 * Bean to encapsulate Node configuration data
 */
public class UniquidNodeConfigurationImpl implements UniquidNodeConfiguration {

	private File providerChainFile;
	private File userChainFile;
	private String nodeName;

	private NetworkParameters networkParameters;
	private File providerFile;
	private File userFile;

	private String publicKey;
	private long creationTime;

	private RegisterFactory registerFactory;

	private String registryUrl;
	
	private UserClientFactory userClientFactory;

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

	public String getRegistryUrl() {
		return registryUrl;
	}

	public void setRegistryUrl(String registryUrl) {
		this.registryUrl = registryUrl;
	}

	public UserClientFactory getUserClientFactory() {
		return userClientFactory;
	}

	public void setUserClientFactory(UserClientFactory userClientFactory) {
		this.userClientFactory = userClientFactory;
	}
	
}
