package com.uniquid.core;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.core.connector.Connector;
import com.uniquid.core.connector.ConnectorException;
import com.uniquid.core.connector.EndPoint;
import com.uniquid.core.impl.test.DummyNode;
import com.uniquid.core.impl.test.DummyProviderRegister;
import com.uniquid.core.impl.test.DummyProviderRequest;
import com.uniquid.core.impl.test.DummyProviderResponse;
import com.uniquid.core.impl.test.DummyUserRegister;
import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.core.provider.impl.EchoFunction;
import com.uniquid.core.provider.impl.GenericFunction;
import com.uniquid.node.UniquidNode;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

public class CoreTest {
	
	@Test
	public void testContructor() throws Exception {
		
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
			
		};
		
		final Connector connector = createDummyConnector();
		
		final UniquidNode node = new DummyNode();
		
		Core core = new Core(dummyFactory, connector, node) {
			
			@Override
			protected Function getFunction(ProviderRequest inputMessage) {
				return null;
			}
		};
		
		Assert.assertNotNull(core);
		Assert.assertEquals(dummyFactory, core.getRegisterFactory());
		Assert.assertEquals(connector, core.getConnector());
		Assert.assertEquals(node, core.getNode());
		Assert.assertNotNull(core.getFunctionContext());
		Assert.assertNull(core.getFunction(null));
		
	}
	
	@Test
	public void testPerformProviderRequestFunctionOk() throws Exception {
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", "bitmask");
		
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
			
		};
		
		final Connector connector = createDummyConnector();
		
		final UniquidNode node = new DummyNode();
		
		Core core = new Core(dummyFactory, connector, node) {
			
			@Override
			protected Function getFunction(ProviderRequest inputMessage) {
				return new EchoFunction();
			}
		};
		
		final ProviderRequest providerRequest = new DummyProviderRequest("userAddress", 30, "params");
		
		final ProviderResponse providerResponse = new DummyProviderResponse();
		
		core.performProviderRequest(providerRequest, providerResponse, null);
		
		Assert.assertEquals(ProviderResponse.RESULT_OK, providerResponse.getError());
		Assert.assertEquals(providerChannel.getProviderAddress(), providerResponse.getSender());
		
	}
	
	@Test
	public void testPerformProviderRequestFunctionException() throws Exception {
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", "bitmask");
		
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
			
		};
		
		final Connector connector = createDummyConnector();
		
		final UniquidNode node = new DummyNode();
		
		Core core = new Core(dummyFactory, connector, node) {
			
			@Override
			protected Function getFunction(ProviderRequest inputMessage) {
				return new GenericFunction() {
					
					@Override
					public void service(ProviderRequest inputMessage, ProviderResponse outputMessage, byte[] payload)
							throws FunctionException, IOException {
						throw new FunctionException("Error!");
						
					}
				};
			}
		};
		
		final ProviderRequest providerRequest = new DummyProviderRequest("userAddress", 30, "params");
		
		final ProviderResponse providerResponse = new DummyProviderResponse();
		
		core.performProviderRequest(providerRequest, providerResponse, null);
		
		Assert.assertEquals(ProviderResponse.RESULT_ERROR, providerResponse.getError());
		Assert.assertEquals(providerChannel.getProviderAddress(), providerResponse.getSender());
		
	}
	
	@Test
	public void testPerformProviderRequestNoFunction() throws Exception {
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", "bitmask");
		
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
			
		};
		
		final Connector connector = createDummyConnector();
		
		final UniquidNode node = new DummyNode();
		
		Core core = new Core(dummyFactory, connector, node) {
			
			@Override
			protected Function getFunction(ProviderRequest inputMessage) {
				return null;
			}
		};
		
		final ProviderRequest providerRequest = new DummyProviderRequest("userAddress", 30, "params");
		
		final ProviderResponse providerResponse = new DummyProviderResponse();
		
		core.performProviderRequest(providerRequest, providerResponse, null);
		
		Assert.assertEquals(ProviderResponse.RESULT_FUNCTION_NOT_AVAILABLE, providerResponse.getError());
		Assert.assertEquals(providerChannel.getProviderAddress(), providerResponse.getSender());
		
	}
	
	@Test
	public void testCheckSenderNoContract() throws Exception {
		
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
			
		};
		
		final Connector connector = createDummyConnector();
		
		final UniquidNode node = new DummyNode();
		
		Core core = new Core(dummyFactory, connector, node) {
			
			@Override
			protected Function getFunction(ProviderRequest inputMessage) {
				return null;
			}
		};
		
		final ProviderRequest providerRequest = new DummyProviderRequest("userAddress", 30, "params");
		
		try {
			
			core.checkSender(providerRequest);
			Assert.fail();
		
		} catch (Exception ex) {
			
			Assert.assertEquals("Sender not found in Provider register!", ex.getMessage());
			
		}
		
	}
	
	@Test
	public void testCheckSenderNotAuthorized() throws Exception {
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		byte[] b = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		
		String bitmaskToString = new String(Hex.encode(b));
		
		ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", bitmaskToString);
		
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
			
		};
		
		final Connector connector = createDummyConnector();
		
		final UniquidNode node = new DummyNode();
		
		Core core = new Core(dummyFactory, connector, node) {
			
			@Override
			protected Function getFunction(ProviderRequest inputMessage) {
				return null;
			}
		};
		
		final ProviderRequest providerRequest = new DummyProviderRequest("userAddress", 30, "params");
		
		try {
			
			core.checkSender(providerRequest);
			Assert.fail();
		
		} catch (Exception ex) {
			
			Assert.assertEquals("Sender not authorized!", ex.getMessage());
			
		}
		
	}
	
	@Test
	public void testCheckSenderNotAuthorized1() throws Exception {
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		byte[] b = {1,30,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		
		String bitmaskToString = new String(Hex.encode(b));
		
		ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", bitmaskToString);
		
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
			
		};
		
		final Connector connector = createDummyConnector();
		
		final UniquidNode node = new DummyNode();
		
		Core core = new Core(dummyFactory, connector, node) {
			
			@Override
			protected Function getFunction(ProviderRequest inputMessage) {
				return null;
			}
		};
		
		final ProviderRequest providerRequest = new DummyProviderRequest("userAddress", 31, "params");
		
		try {
			
			core.checkSender(providerRequest);
			Assert.fail();
		
		} catch (Exception ex) {
			
			Assert.assertEquals("Sender not authorized!", ex.getMessage());
			
		}
		
	}
	
	@Test
	public void testCheckSenderAuthorized() throws Exception {
		
		byte[] b2 = {0, 0, 0, 0, 64};
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		String bitmaskToString = new String(Hex.encode(b2));
		
		ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", bitmaskToString);
		
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
			
		};
		
		final Connector connector = createDummyConnector();
		
		final UniquidNode node = new DummyNode();
		
		Core core = new Core(dummyFactory, connector, node) {
			
			@Override
			protected Function getFunction(ProviderRequest inputMessage) {
				return null;
			}
		};
		
		final ProviderRequest providerRequest = new DummyProviderRequest("userAddress", 30, "params");
		Assert.assertNotNull(core.checkSender(providerRequest));
		Assert.assertTrue(Arrays.equals(b2, core.checkSender(providerRequest)));
		
	}
	
	@Test
	public void testCheckSenderAuthorized1() throws Exception {
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		byte[] b = {1,30,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		
		String bitmaskToString = new String(Hex.encode(b));
		
		ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", bitmaskToString);
		
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
			
		};
		
		final Connector connector = createDummyConnector();
		
		final UniquidNode node = new DummyNode();
		
		Core core = new Core(dummyFactory, connector, node) {
			
			@Override
			protected Function getFunction(ProviderRequest inputMessage) {
				return null;
			}
		};
		
		final ProviderRequest providerRequest = new DummyProviderRequest("userAddress", 30, "params");
		
		try {
			
			Assert.assertTrue(Arrays.equals(b, core.checkSender(providerRequest)));
		
		} catch (Exception ex) {
			
			Assert.fail();
			
		}
		
	}
	
	@Test
	public void testCheckSenderInvalidContractVersion() throws Exception {
		
		byte[] b2 = {2, 0, 0, 0, 64};
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		String bitmaskToString = new String(Hex.encode(b2));
		
		ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", bitmaskToString);
		
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
			
		};
		
		final Connector connector = createDummyConnector();
		
		final UniquidNode node = new DummyNode();
		
		Core core = new Core(dummyFactory, connector, node) {
			
			@Override
			protected Function getFunction(ProviderRequest inputMessage) {
				return null;
			}
		};
		
		final ProviderRequest providerRequest = new DummyProviderRequest("userAddress", 30, "params");
		
		try {
			
			core.checkSender(providerRequest);
			Assert.fail();
		
		} catch (Exception ex) {
			
			Assert.assertEquals("Invalid contract version!", ex.getMessage());
			
		}
		
	}
	
	public Connector createDummyConnector() {
		
		return new Connector() {
			
			@Override
			public void stop() throws ConnectorException {
			}
			
			@Override
			public void start() throws ConnectorException {
			}
			
			@Override
			public EndPoint accept() throws ConnectorException {
				return null;
			}
		};
		
	}
	
}
