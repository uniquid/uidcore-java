package com.uniquid.node.impl;

import java.io.File;

import org.bitcoinj.core.NetworkParameters;
import org.junit.Assert;
import org.junit.Test;

import com.uniquid.node.impl.UniquidNodeImpl.Builder;
import com.uniquid.node.impl.params.UniquidRegTest;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

public class UniquidNodeImplBuilderTest {
	
	@Test
	public void testConstructor() throws Exception {
		
		UniquidNodeImpl.Builder builder = new Builder();
		
		Assert.assertNotNull(builder);
		
		Assert.assertNull(builder.getNetworkParameters());
		Assert.assertNull(builder.getProviderFile());
		Assert.assertNull(builder.getUserFile());
		Assert.assertNull(builder.getChainFile());
		Assert.assertNull(builder.getUserChainFile());
		Assert.assertNull(builder.getRegisterFactory());
		Assert.assertNull(builder.getNodeName());
		
	}
	
	@Test
	public void testParams() {
		
		UniquidNodeImpl.Builder builder = new Builder();
		
		Assert.assertNull(builder.getNetworkParameters());
		
		NetworkParameters parameters = UniquidRegTest.get();
		
		builder.setNetworkParameters(parameters);
		
		Assert.assertEquals(parameters, builder.getNetworkParameters());
		
	}
	
	@Test
	public void testProviderFile() throws Exception {
		
		UniquidNodeImpl.Builder builder = new Builder();
		
		Assert.assertNull(builder.getProviderFile());
		
		File file = File.createTempFile("provider", ".wallet");
		
		builder.setProviderFile(file);
		
		Assert.assertEquals(file, builder.getProviderFile());
		
	}
	
	@Test
	public void testUserFile() throws Exception {
		
		UniquidNodeImpl.Builder builder = new Builder();
		
		Assert.assertNull(builder.getUserFile());
		
		File file = File.createTempFile("user", ".wallet");
		
		builder.setUserFile(file);
		
		Assert.assertEquals(file, builder.getUserFile());
		
	}
	
	@Test
	public void testChainFile() throws Exception {
		
		UniquidNodeImpl.Builder builder = new Builder();
		
		Assert.assertNull(builder.getChainFile());
		
		File file = File.createTempFile("chain", ".chain");
		
		builder.setChainFile(file);
		
		Assert.assertEquals(file, builder.getChainFile());
		
	}
	
	@Test
	public void testUserChainFile() throws Exception {
		
		UniquidNodeImpl.Builder builder = new Builder();
		
		Assert.assertNull(builder.getUserFile());
		
		File file = File.createTempFile("userchain", ".chain");
		
		builder.setUserChainFile(file);
		
		Assert.assertEquals(file, builder.getUserChainFile());
		
	}
	
	@Test
	public void testRegisterFactory() {
		
		UniquidNodeImpl.Builder builder = new Builder();
		
		Assert.assertNull(builder.getRegisterFactory());
		
		RegisterFactory dummy = new RegisterFactory() {
			
			@Override
			public UserRegister getUserRegister() throws RegisterException {
				return null;
			}
			
			@Override
			public ProviderRegister getProviderRegister() throws RegisterException {
				return null;
			}
			
		};
		
		builder.setRegisterFactory(dummy);
		
		Assert.assertEquals(dummy, builder.getRegisterFactory());
		
	}
	
	@Test
	public void testMachineName() {
		
		UniquidNodeImpl.Builder builder = new Builder();
		
		Assert.assertNull(builder.getNodeName());
		
		String machineName = "machineName";
		
		builder.setNodeName(machineName);
		
		Assert.assertEquals(machineName, builder.getNodeName());
		
	}
	
	@Test
	public void testBuild() throws Exception {
		
		UniquidNodeImpl.Builder builder = new Builder();
		
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

		builder.setNetworkParameters(parameters);
		builder.setProviderFile(providerFile);
		builder.setUserFile(userFile);
		builder.setChainFile(chainFile);
		builder.setUserChainFile(userChainFile);
		builder.setRegisterFactory(dummyRegister);
		builder.setNodeName(machineName);
		
		UniquidNodeImpl uniquidNode = builder.build();
		
		Assert.assertNotNull(uniquidNode);
		
	}

}
