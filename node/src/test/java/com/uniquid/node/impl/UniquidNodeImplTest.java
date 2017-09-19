package com.uniquid.node.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.junit.Assert;
import org.junit.Test;

import com.uniquid.node.UniquidNodeState;
import com.uniquid.node.impl.params.UniquidRegTest;
import com.uniquid.node.impl.utils.DummyProviderRegister;
import com.uniquid.node.impl.utils.DummyRegisterFactory;
import com.uniquid.node.impl.utils.DummyTransactionManager;
import com.uniquid.node.impl.utils.DummyUserRegister;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.transaction.TransactionException;
import com.uniquid.register.transaction.TransactionManager;
import com.uniquid.register.user.UserRegister;

public class UniquidNodeImplTest {

	@Test
	public void testBuild() throws Exception {
		
		@SuppressWarnings("rawtypes")
		UniquidNodeImpl.UniquidNodeBuilder builder = new UniquidNodeImpl.UniquidNodeBuilder();
		
		NetworkParameters parameters = UniquidRegTest.get();
		File providerFile = File.createTempFile("provider", ".wallet");
		File userFile = File.createTempFile("user", ".wallet");
		File chainFile = File.createTempFile("chain", ".chain");
		File userChainFile = File.createTempFile("userchain", ".chain");
		
		RegisterFactory dummyRegister = new DummyRegisterFactory(null, null, new DummyTransactionManager());
		
		String machineName = "machineName";

		builder.setNetworkParameters(parameters);
		builder.setProviderFile(providerFile);
		builder.setUserFile(userFile);
		builder.setProviderChainFile(chainFile);
		builder.setUserChainFile(userChainFile);
		builder.setRegisterFactory(dummyRegister);
		builder.setNodeName(machineName);
		
		UniquidNodeImpl uniquidNode = builder.buildFromHexSeed("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", 1487159470);
		
		Assert.assertNotNull(uniquidNode);
		
		Assert.assertEquals(UniquidNodeState.CREATED, uniquidNode.getNodeState());
		
		uniquidNode.getImprintingAddress();
			
		uniquidNode.getPublicKey();
		
		Assert.assertEquals(machineName, uniquidNode.getNodeName());
		
		Assert.assertEquals(1487159470, uniquidNode.getCreationTime());
		
		try {
			
			uniquidNode.getSpendableBalance();
			Assert.fail();
			
		} catch (Exception e) {
			// do nothing
		}
		
	}
	
	@Test
	public void testInitNode0Elements() throws Exception {

		@SuppressWarnings("rawtypes")
		UniquidNodeImpl.UniquidNodeBuilder builder = new UniquidNodeImpl.UniquidNodeBuilder();
		
		NetworkParameters parameters = UniquidRegTest.get();
		File providerFile = File.createTempFile("provider", ".wallet");
		providerFile.delete();
		File userFile = File.createTempFile("user", ".wallet");
		userFile.delete();
		File chainFile = File.createTempFile("chain", ".chain");
		chainFile.delete();
		File userChainFile = File.createTempFile("userchain", ".chain");
		userChainFile.delete();
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		final UserRegister dummyUser = new DummyUserRegister();
		
		RegisterFactory dummyFactory = new DummyRegisterFactory(dummyUser, dummyProvider, new DummyTransactionManager());
				
		String machineName = "machineName";

		builder.setNetworkParameters(parameters);
		builder.setProviderFile(providerFile);
		builder.setUserFile(userFile);
		builder.setProviderChainFile(chainFile);
		builder.setUserChainFile(userChainFile);
		builder.setRegisterFactory(dummyFactory);
		builder.setNodeName(machineName);
		
		UniquidNodeImpl uniquidNode = builder.buildFromHexSeed("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", 1487159470);
		
		Assert.assertNotNull(uniquidNode);
		
		uniquidNode.initNode();
		
		Assert.assertEquals(UniquidNodeState.IMPRINTING, uniquidNode.getNodeState());
		
		Assert.assertEquals("mj3Ggr43QMSea1s6H3nYJRE3m5GjhGFcLb", uniquidNode.getImprintingAddress());
		
		Assert.assertEquals("tpubDAjeiZPBWjUzphKoEJhkBdiqCSjkunkKQmUxGycsWBPH2aHqdAmJXoRhY6NfNAmRXLmQikRxZM1urcP4Rv7Fb3mcSFpozMstdqUtmSwjFJp", uniquidNode.getPublicKey());
		
		Assert.assertEquals("0.00 BTC", uniquidNode.getSpendableBalance());
		
		Assert.assertNotNull(uniquidNode.getProviderWallet());
		
		Assert.assertNotNull(uniquidNode.getUserWallet());
		
	}
	
	@Test
	public void testInitNode1Elements() throws Exception {

		@SuppressWarnings("rawtypes")
		UniquidNodeImpl.UniquidNodeBuilder builder = new UniquidNodeImpl.UniquidNodeBuilder();
		
		NetworkParameters parameters = UniquidRegTest.get();
		File providerFile = File.createTempFile("provider", ".wallet");
		providerFile.delete();
		File userFile = File.createTempFile("user", ".wallet");
		userFile.delete();
		File chainFile = File.createTempFile("chain", ".chain");
		chainFile.delete();
		File userChainFile = File.createTempFile("userchain", ".chain");
		userChainFile.delete();
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		String providerAddress = "providerAddress";
		String userAddress = "userAddress";
		String bitmask = "bitmask";
		
		ProviderChannel providerChannel = new ProviderChannel(providerAddress, userAddress, bitmask);
		
		Assert.assertEquals(providerAddress, providerChannel.getProviderAddress());
		Assert.assertEquals(userAddress, providerChannel.getUserAddress());
		Assert.assertEquals(bitmask, providerChannel.getBitmask());
		Assert.assertEquals(null, providerChannel.getRevokeAddress());
		Assert.assertEquals(null, providerChannel.getRevokeTxId());
		
		dummyProvider.insertChannel(providerChannel);
		
		final UserRegister dummyUser = new DummyUserRegister();
		
		RegisterFactory dummyFactory = new DummyRegisterFactory(dummyUser, dummyProvider, new DummyTransactionManager());
				
		String machineName = "machineName";

		builder.setNetworkParameters(parameters);
		builder.setProviderFile(providerFile);
		builder.setUserFile(userFile);
		builder.setProviderChainFile(chainFile);
		builder.setUserChainFile(userChainFile);
		builder.setRegisterFactory(dummyFactory);
		builder.setNodeName(machineName);
		
		UniquidNodeImpl uniquidNode = builder.buildFromHexSeed("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", 1487159470);
		
		Assert.assertNotNull(uniquidNode);
		
		uniquidNode.initNode();
		
		Assert.assertEquals(UniquidNodeState.READY, uniquidNode.getNodeState());
		
		Assert.assertEquals("mj3Ggr43QMSea1s6H3nYJRE3m5GjhGFcLb", uniquidNode.getImprintingAddress());
		
		Assert.assertEquals("tpubDAjeiZPBWjUzphKoEJhkBdiqCSjkunkKQmUxGycsWBPH2aHqdAmJXoRhY6NfNAmRXLmQikRxZM1urcP4Rv7Fb3mcSFpozMstdqUtmSwjFJp", uniquidNode.getPublicKey());
		
		Assert.assertEquals("0.00 BTC", uniquidNode.getSpendableBalance());
		
		Assert.assertNotNull(uniquidNode.getProviderWallet());
		
		Assert.assertNotNull(uniquidNode.getUserWallet());
		
	}
	
	@Test
	public void testUpdateNode() throws Exception {

		@SuppressWarnings("rawtypes")
		UniquidNodeImpl.UniquidNodeBuilder builder = new UniquidNodeImpl.UniquidNodeBuilder();
		
		NetworkParameters parameters = UniquidRegTest.get();
		File providerFile = File.createTempFile("provider", ".wallet");
		providerFile.delete();
		File userFile = File.createTempFile("user", ".wallet");
		userFile.delete();
		File chainFile = File.createTempFile("chain", ".chain");
		chainFile.delete();
		File userChainFile = File.createTempFile("userchain", ".chain");
		userChainFile.delete();
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		final UserRegister dummyUser = new DummyUserRegister();
		
		RegisterFactory dummyFactory = new DummyRegisterFactory(dummyUser, dummyProvider, new DummyTransactionManager());
				
		String machineName = "machineName";

		builder.setNetworkParameters(parameters);
		builder.setProviderFile(providerFile);
		builder.setUserFile(userFile);
		builder.setProviderChainFile(chainFile);
		builder.setUserChainFile(userChainFile);
		builder.setRegisterFactory(dummyFactory);
		builder.setNodeName(machineName);
		
		UniquidNodeImpl uniquidNode = builder.buildFromHexSeed("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", 1487159470);
		
		Assert.assertNotNull(uniquidNode);
		
		uniquidNode.initNode();
		
		Assert.assertEquals(UniquidNodeState.IMPRINTING, uniquidNode.getNodeState());
		
		Assert.assertEquals("mj3Ggr43QMSea1s6H3nYJRE3m5GjhGFcLb", uniquidNode.getImprintingAddress());
		
		Assert.assertEquals("tpubDAjeiZPBWjUzphKoEJhkBdiqCSjkunkKQmUxGycsWBPH2aHqdAmJXoRhY6NfNAmRXLmQikRxZM1urcP4Rv7Fb3mcSFpozMstdqUtmSwjFJp", uniquidNode.getPublicKey());
		
		Assert.assertEquals("0.00 BTC", uniquidNode.getSpendableBalance());
		
		Assert.assertNotNull(uniquidNode.getProviderWallet());
		
		Assert.assertNotNull(uniquidNode.getUserWallet());
		
		uniquidNode.updateNode();
		
		Thread.sleep(5000); // wait to update
		
		Assert.assertEquals(1, dummyProvider.getAllChannels().size());
		
		Assert.assertEquals(UniquidNodeState.READY, uniquidNode.getNodeState());
		
		Assert.assertEquals("7.00 BTC", uniquidNode.getSpendableBalance());

		UniquidNodeImpl uniquidNodeReloaded = builder.buildFromHexSeed("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", 1487159470);
		
		Assert.assertNotNull(uniquidNodeReloaded);
		
		uniquidNodeReloaded.initNode();
		
		Assert.assertEquals(1, dummyProvider.getAllChannels().size());
		
		Assert.assertEquals(UniquidNodeState.READY, uniquidNodeReloaded.getNodeState());
		
		Assert.assertEquals("7.00 BTC", uniquidNodeReloaded.getSpendableBalance());
		
		List<String> paths = new ArrayList<String>();
		paths.add("0/0/0");
		paths.add("0/0/1");
		
		String unsigned_tx = "010000000247a327c7f5d626a7159c5c0fccf90732ba733ab6e9eea53db24c4829b3cc46a40000000000ffffffffced72f216e191ebc3be3b7b8c5d8fc0a7ac52fa934e395f837a28f96df2d8f900100000000ffffffff0140420f00000000001976a91457c9afb8bc5e4fa738f5b46afcb51b43a48b270988ac00000000";
		
		String signed_tx = "010000000247a327c7f5d626a7159c5c0fccf90732ba733ab6e9eea53db24c4829b3cc46a4000000006a473044022014fac39447707341f16cac6fcd9a7258dcc636767016e225c5bb2a2ed4462f4c02202867a07f0695109b47cd9de86d06393c9f3f1f0ebbde5f3f7914f5296edf1be4012102461fb3538ffec054fd4ee1e9087e7debf8442028f941bda308c24b508cbf69f7ffffffffced72f216e191ebc3be3b7b8c5d8fc0a7ac52fa934e395f837a28f96df2d8f90010000006a473044022061e3c20622dcbe8ea3a62c66ba56da91c4f1083b11bbd6e912df81bc92826ac50220631d302f309a1c5212933830f910ba2931ff32a5b41a2c9aaa808b926aa99363012102ece5ce70796b6893283aa0c8f30273c7dc0ff0b82a75017285387ecd2d767110ffffffff0140420f00000000001976a91457c9afb8bc5e4fa738f5b46afcb51b43a48b270988ac00000000";
		
		Assert.assertEquals(signed_tx, uniquidNode.signTransaction(unsigned_tx, paths));
		
		Assert.assertEquals("IOAhyp0at0puRgDZD3DJl0S2FjgLEo0q7nBdgzDrWpbDR+B3daIlN3R20lhcpQKZFWl8/ttxUXzQYS0EFso2VLo=", uniquidNode.signMessage("Hello World!", "0/0/0"));
		
		final ECKey key = ECKey.signedMessageToKey("Hello World!", "IOAhyp0at0puRgDZD3DJl0S2FjgLEo0q7nBdgzDrWpbDR+B3daIlN3R20lhcpQKZFWl8/ttxUXzQYS0EFso2VLo=");
		
		Assert.assertEquals("mj3Ggr43QMSea1s6H3nYJRE3m5GjhGFcLb", key.toAddress(UniquidRegTest.get()).toBase58());
		
		Assert.assertEquals("H3UHssQig0Vef9VIzUmDW0HV37vpm5ZZGF0zbw6xxMMoTTbUm/efPIQDcx5IlOgflC7BcR90aXHsV7BBaQx+b9Q=", uniquidNode.signMessage("Hello World!", "1/0/0"));
		
		final ECKey key2 = ECKey.signedMessageToKey("Hello World!", "H3UHssQig0Vef9VIzUmDW0HV37vpm5ZZGF0zbw6xxMMoTTbUm/efPIQDcx5IlOgflC7BcR90aXHsV7BBaQx+b9Q=");
		
		Assert.assertEquals("mgXg8FWaYaDVcsvjJq4jW7vrxQCRtjPchs", key2.toAddress(UniquidRegTest.get()).toBase58());
		
		String signedMessage = uniquidNode.signMessage("Hello World!", Address.fromBase58(UniquidRegTest.get(), "mj3Ggr43QMSea1s6H3nYJRE3m5GjhGFcLb").getHash160());
		
		Assert.assertEquals("IOAhyp0at0puRgDZD3DJl0S2FjgLEo0q7nBdgzDrWpbDR+B3daIlN3R20lhcpQKZFWl8/ttxUXzQYS0EFso2VLo=", signedMessage);
		
		try {
			List<String> invalid = new ArrayList<String>();
			invalid.add("2/0/0");
			uniquidNode.signTransaction(unsigned_tx, invalid);
			Assert.fail();
		} catch (Exception ex) {
			// NOTHING TO DO
		}
		
	}
	
}
