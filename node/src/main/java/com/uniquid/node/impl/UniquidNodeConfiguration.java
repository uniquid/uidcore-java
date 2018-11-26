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
