package com.uniquid.node.impl;

import java.io.File;

import org.bitcoinj.core.NetworkParameters;
import org.junit.Assert;
import org.junit.Test;

import com.uniquid.node.UniquidNodeState;
import com.uniquid.node.impl.params.UniquidRegTest;
import com.uniquid.node.impl.utils.DummyProviderRegister;
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
		RegisterFactory dummyRegister = new RegisterFactory() {
					
					@Override
					public UserRegister getUserRegister() throws RegisterException {
						return null;
					}
					
					@Override
					public ProviderRegister getProviderRegister() throws RegisterException {
						return null;
					}
					
					@Override
					public TransactionManager getTransactionManager() throws RegisterException {
						
						return new DummyTransactionManager();
					}
					
				};
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
		
		Assert.assertEquals("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", uniquidNode.getHexSeed());
		
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
		
		RegisterFactory dummyFactory = new RegisterFactory() {
					
					@Override
					public UserRegister getUserRegister() throws RegisterException {
						return dummyUser;
					}
					
					@Override
					public ProviderRegister getProviderRegister() throws RegisterException {
						return dummyProvider;
					}
					
					@Override
					public TransactionManager getTransactionManager() throws RegisterException {

						return new DummyTransactionManager();
						
					}
					
				};
				
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
		
		Assert.assertEquals("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", uniquidNode.getHexSeed());
		
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
		
		RegisterFactory dummyFactory = new RegisterFactory() {
					
					@Override
					public UserRegister getUserRegister() throws RegisterException {
						return dummyUser;
					}
					
					@Override
					public ProviderRegister getProviderRegister() throws RegisterException {
						return dummyProvider;
					}
					
					@Override
					public TransactionManager getTransactionManager() throws RegisterException {

						return new DummyTransactionManager();
					}
					
				};
				
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
		
		Assert.assertEquals("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", uniquidNode.getHexSeed());
		
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
		
		RegisterFactory dummyFactory = new RegisterFactory() {
					
					@Override
					public UserRegister getUserRegister() throws RegisterException {
						return dummyUser;
					}
					
					@Override
					public ProviderRegister getProviderRegister() throws RegisterException {
						return dummyProvider;
					}
					
					@Override
					public TransactionManager getTransactionManager() throws RegisterException {

						return new DummyTransactionManager();
					}
					
				};
				
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
		
		Assert.assertEquals("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", uniquidNode.getHexSeed());
		
		Assert.assertEquals("0.00 BTC", uniquidNode.getSpendableBalance());
		
		Assert.assertNotNull(uniquidNode.getProviderWallet());
		
		Assert.assertNotNull(uniquidNode.getUserWallet());
		
		uniquidNode.updateNode();
		
		Thread.sleep(5000); // wait to update
		
		Assert.assertEquals(1, dummyProvider.getAllChannels().size());
		
		Assert.assertEquals(UniquidNodeState.READY, uniquidNode.getNodeState());
		
		Assert.assertEquals("2.00 BTC", uniquidNode.getSpendableBalance());

		UniquidNodeImpl uniquidNodeReloaded = builder.buildFromHexSeed("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", 1487159470);
		
		Assert.assertNotNull(uniquidNodeReloaded);
		
		uniquidNodeReloaded.initNode();
		
		Assert.assertEquals(1, dummyProvider.getAllChannels().size());
		
		Assert.assertEquals(UniquidNodeState.READY, uniquidNodeReloaded.getNodeState());
		
		Assert.assertEquals("2.00 BTC", uniquidNodeReloaded.getSpendableBalance());
	}
	
}
