package com.uniquid.node.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bitcoinj.core.NetworkParameters;
import org.junit.Assert;
import org.junit.Test;

import com.uniquid.node.UniquidNodeState;
import com.uniquid.node.impl.UniquidWatchingNodeImpl.Builder;
import com.uniquid.node.impl.params.UniquidRegTest;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

public class UniquidWatchingNodeImplTest {

	@Test
	public void testBuild() throws Exception {
		
		UniquidWatchingNodeImpl.Builder builder = new Builder();
		
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
					
				};
		String machineName = "machineName";

		builder.set_params(parameters);
		builder.set_providerFile(providerFile);
		builder.set_userFile(userFile);
		builder.set_chainFile(chainFile);
		builder.set_userChainFile(userChainFile);
		builder.set_registerFactory(dummyRegister);
		builder.set_machine_name(machineName);
		
		UniquidWatchingNodeImpl uniquidNode = builder.buildFromXpub("tpubDAnD549eCz2j2w21P6sx9NvXJrEoWzVevpbvXDpwQzKTC9xWsr8emiEdJ64h1qXbYE4SbDJNbZ7imotNPsGD8RvHQvh6xtgMJTczb8WW8X8", 1487159470);
		
		Assert.assertNotNull(uniquidNode);
		
		Assert.assertEquals(UniquidNodeState.CREATED, uniquidNode.getNodeState());
		
		try {
			
			uniquidNode.getImprintingAddress();
			Assert.fail();
			
		} catch (Exception e) {
			// do nothing
		}
		
		try {
			
			uniquidNode.getPublicKey();
			Assert.fail();
			
		} catch (Exception e) {
			// do nothing
		}
		
		Assert.assertEquals(machineName, uniquidNode.getNodeName());
		
		Assert.assertEquals(1487159470, uniquidNode.getCreationTime());
		
		Assert.assertEquals(null, uniquidNode.getHexSeed());
		
		try {
			
			uniquidNode.getSpendableBalance();
			Assert.fail();
			
		} catch (Exception e) {
			// do nothing
		}
		
	}
	
	@Test
	public void testInitNode0Elements() throws Exception {

		UniquidWatchingNodeImpl.Builder builder = new Builder();
		
		NetworkParameters parameters = UniquidRegTest.get();
		File providerFile = File.createTempFile("provider", ".wallet");
		providerFile.delete();
		File userFile = File.createTempFile("user", ".wallet");
		userFile.delete();
		File chainFile = File.createTempFile("chain", ".chain");
		chainFile.delete();
		File userChainFile = File.createTempFile("userchain", ".chain");
		userChainFile.delete();
		
		final ProviderRegister dummyProvider = createDummyProviderRegister();
		
		final UserRegister dummyUser = createDummyUserRegister();
		
		RegisterFactory dummyFactory = new RegisterFactory() {
					
					@Override
					public UserRegister getUserRegister() throws RegisterException {
						return dummyUser;
					}
					
					@Override
					public ProviderRegister getProviderRegister() throws RegisterException {
						return dummyProvider;
					}
					
				};
				
		String machineName = "machineName";

		builder.set_params(parameters);
		builder.set_providerFile(providerFile);
		builder.set_userFile(userFile);
		builder.set_chainFile(chainFile);
		builder.set_userChainFile(userChainFile);
		builder.set_registerFactory(dummyFactory);
		builder.set_machine_name(machineName);
		
		UniquidWatchingNodeImpl uniquidNode = builder.buildFromXpub("tpubDAnD549eCz2j2w21P6sx9NvXJrEoWzVevpbvXDpwQzKTC9xWsr8emiEdJ64h1qXbYE4SbDJNbZ7imotNPsGD8RvHQvh6xtgMJTczb8WW8X8", 1487159470);
		
		Assert.assertNotNull(uniquidNode);
		
		uniquidNode.initNode();
		
		Assert.assertEquals(UniquidNodeState.IMPRINTING, uniquidNode.getNodeState());
		
		Assert.assertEquals("mgvAfpjBzHVtGQqtqRC9QSgfR7AKawSUEN", uniquidNode.getImprintingAddress());
		
		Assert.assertEquals("tpubDAnD549eCz2j2w21P6sx9NvXJrEoWzVevpbvXDpwQzKTC9xWsr8emiEdJ64h1qXbYE4SbDJNbZ7imotNPsGD8RvHQvh6xtgMJTczb8WW8X8", uniquidNode.getPublicKey());
		
		Assert.assertEquals(null, uniquidNode.getHexSeed());
		
		Assert.assertEquals("0.00 BTC", uniquidNode.getSpendableBalance());
		
		Assert.assertNotNull(uniquidNode.getProviderWallet());
		
		Assert.assertNotNull(uniquidNode.getUserWallet());
		
	}
	
	@Test
	public void testInitNode1Elements() throws Exception {

		UniquidWatchingNodeImpl.Builder builder = new Builder();
		
		NetworkParameters parameters = UniquidRegTest.get();
		File providerFile = File.createTempFile("provider", ".wallet");
		providerFile.delete();
		File userFile = File.createTempFile("user", ".wallet");
		userFile.delete();
		File chainFile = File.createTempFile("chain", ".chain");
		chainFile.delete();
		File userChainFile = File.createTempFile("userchain", ".chain");
		userChainFile.delete();
		
		final ProviderRegister dummyProvider = createDummyProviderRegister();
		
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
		
		final UserRegister dummyUser = createDummyUserRegister();
		
		RegisterFactory dummyFactory = new RegisterFactory() {
					
					@Override
					public UserRegister getUserRegister() throws RegisterException {
						return dummyUser;
					}
					
					@Override
					public ProviderRegister getProviderRegister() throws RegisterException {
						return dummyProvider;
					}
					
				};
				
		String machineName = "machineName";

		builder.set_params(parameters);
		builder.set_providerFile(providerFile);
		builder.set_userFile(userFile);
		builder.set_chainFile(chainFile);
		builder.set_userChainFile(userChainFile);
		builder.set_registerFactory(dummyFactory);
		builder.set_machine_name(machineName);
		
		UniquidWatchingNodeImpl uniquidNode = builder.buildFromXpub("tpubDAnD549eCz2j2w21P6sx9NvXJrEoWzVevpbvXDpwQzKTC9xWsr8emiEdJ64h1qXbYE4SbDJNbZ7imotNPsGD8RvHQvh6xtgMJTczb8WW8X8", 1487159470);
		
		Assert.assertNotNull(uniquidNode);
		
		uniquidNode.initNode();
		
		Assert.assertEquals(UniquidNodeState.READY, uniquidNode.getNodeState());
		
		Assert.assertEquals("mgvAfpjBzHVtGQqtqRC9QSgfR7AKawSUEN", uniquidNode.getImprintingAddress());
		
		Assert.assertEquals("tpubDAnD549eCz2j2w21P6sx9NvXJrEoWzVevpbvXDpwQzKTC9xWsr8emiEdJ64h1qXbYE4SbDJNbZ7imotNPsGD8RvHQvh6xtgMJTczb8WW8X8", uniquidNode.getPublicKey());
		
		Assert.assertEquals(null, uniquidNode.getHexSeed());
		
		Assert.assertEquals("0.00 BTC", uniquidNode.getSpendableBalance());
		
		Assert.assertNotNull(uniquidNode.getProviderWallet());
		
		Assert.assertNotNull(uniquidNode.getUserWallet());
		
	}
	
	@Test
	public void testUpdateNode() throws Exception {

		UniquidWatchingNodeImpl.Builder builder = new Builder();
		
		NetworkParameters parameters = UniquidRegTest.get();
		File providerFile = File.createTempFile("provider", ".wallet");
		providerFile.delete();
		File userFile = File.createTempFile("user", ".wallet");
		userFile.delete();
		File chainFile = File.createTempFile("chain", ".chain");
		chainFile.delete();
		File userChainFile = File.createTempFile("userchain", ".chain");
		userChainFile.delete();
		
		final ProviderRegister dummyProvider = createDummyProviderRegister();
		
		final UserRegister dummyUser = createDummyUserRegister();
		
		RegisterFactory dummyFactory = new RegisterFactory() {
					
					@Override
					public UserRegister getUserRegister() throws RegisterException {
						return dummyUser;
					}
					
					@Override
					public ProviderRegister getProviderRegister() throws RegisterException {
						return dummyProvider;
					}
					
				};
				
		String machineName = "machineName";

		builder.set_params(parameters);
		builder.set_providerFile(providerFile);
		builder.set_userFile(userFile);
		builder.set_chainFile(chainFile);
		builder.set_userChainFile(userChainFile);
		builder.set_registerFactory(dummyFactory);
		builder.set_machine_name(machineName);
		
		UniquidWatchingNodeImpl uniquidNode = builder.buildFromXpub("tpubDAnD549eCz2j2w21P6sx9NvXJrEoWzVevpbvXDpwQzKTC9xWsr8emiEdJ64h1qXbYE4SbDJNbZ7imotNPsGD8RvHQvh6xtgMJTczb8WW8X8", 1487159470);
		
		Assert.assertNotNull(uniquidNode);
		
		uniquidNode.initNode();
		
		Assert.assertEquals(UniquidNodeState.IMPRINTING, uniquidNode.getNodeState());
		
		Assert.assertEquals("mgvAfpjBzHVtGQqtqRC9QSgfR7AKawSUEN", uniquidNode.getImprintingAddress());
		
		Assert.assertEquals("tpubDAnD549eCz2j2w21P6sx9NvXJrEoWzVevpbvXDpwQzKTC9xWsr8emiEdJ64h1qXbYE4SbDJNbZ7imotNPsGD8RvHQvh6xtgMJTczb8WW8X8", uniquidNode.getPublicKey());
		
		Assert.assertEquals(null, uniquidNode.getHexSeed());
		
		Assert.assertEquals("0.00 BTC", uniquidNode.getSpendableBalance());
		
		Assert.assertNotNull(uniquidNode.getProviderWallet());
		
		Assert.assertNotNull(uniquidNode.getUserWallet());
		
		uniquidNode.updateNode();
		
		Thread.sleep(5000); // wait to update
		
		Assert.assertEquals(2, dummyProvider.getAllChannels().size());
		
		Assert.assertEquals(UniquidNodeState.READY, uniquidNode.getNodeState());
		
		Assert.assertEquals("0.00 BTC", uniquidNode.getSpendableBalance());
	}
	
	private ProviderRegister createDummyProviderRegister() {
	
		return new ProviderRegister() {
			
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
			
		};
		
	}
	
	private UserRegister createDummyUserRegister() {
		
		return new UserRegister() {
			
			private ArrayList<UserChannel> channels = new ArrayList<UserChannel>();
			
			@Override
			public void insertChannel(UserChannel userChannel) throws RegisterException {
				channels.add(userChannel);
				
			}
			
			@Override
			public UserChannel getUserChannelByRevokeTxId(String revokeTxId) throws RegisterException {

				for (UserChannel p : channels) {
					
					if (p.getRevokeTxId().equals(revokeTxId)) {
						return p;
					}
					
				}
					
				return null;
			}
			
			@Override
			public UserChannel getUserChannelByRevokeAddress(String revokeTxId) throws RegisterException {
				
				for (UserChannel p : channels) {
					
					if (p.getRevokeAddress().equals(revokeTxId)) {
						return p;
					}
					
				}
					
				return null;
			}
			
			@Override
			public UserChannel getChannelByProviderAddress(String name) throws RegisterException {

				for (UserChannel p : channels) {
					
					if (p.getProviderAddress().equals(name)) {
						return p;
					}
					
				}
					
				return null;
			}
			
			@Override
			public UserChannel getChannelByName(String name) throws RegisterException {

				for (UserChannel p : channels) {
					
					if (p.getProviderName().equals(name)) {
						return p;
					}
					
				}
					
				return null;
			}
			
			@Override
			public List<UserChannel> getAllUserChannels() throws RegisterException {
				return channels;
			}
			
			@Override
			public void deleteChannel(UserChannel userChannel) throws RegisterException {
				channels.remove(userChannel);
			}
			
		};
		
	}
	
}
