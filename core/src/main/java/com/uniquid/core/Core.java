package com.uniquid.core;

import java.util.Arrays;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.core.connector.Connector;
import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.impl.ApplicationContext;
import com.uniquid.node.UniquidNode;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;

/**
 * This is the core of Uniquid library. It contains a collection of functionalities
 * needed by the machines to send, receive, decode request.
 */
public abstract class Core {

	private static final Logger LOGGER = LoggerFactory.getLogger(Core.class.getName());

	public static final int RESULT_OK = 0;
	public static final int RESULT_NO_PERMISSION = 2;
	public static final int RESULT_FUNCTION_NOT_AVAILABLE = 3;
	public static final int RESULT_ERROR = 4;
	
	public static final String NODE_ATTRIBUTE = com.uniquid.node.UniquidNode.class.getName();
	public static final String REGISTER_FACTORY_ATTRIBUTE = com.uniquid.register.RegisterFactory.class.getName();
	public static final String CONNECTOR_ATTRIBUTE = com.uniquid.core.connector.Connector.class.getName();

	private RegisterFactory registerFactory;
	private Connector connector;
	private ApplicationContext applicationContext;
	private UniquidNode uniquidNode;

	public Core(RegisterFactory registerFactory, Connector connectorServiceFactory, UniquidNode node)
			throws Exception {

		this.registerFactory = registerFactory;
		this.connector = connectorServiceFactory;
		this.uniquidNode = node;

		applicationContext = new ApplicationContext();
		applicationContext.setAttribute(NODE_ATTRIBUTE, node);
		applicationContext.setAttributeReadOnly(NODE_ATTRIBUTE);
		applicationContext.setAttribute(REGISTER_FACTORY_ATTRIBUTE, registerFactory);
		applicationContext.setAttributeReadOnly(REGISTER_FACTORY_ATTRIBUTE);
		applicationContext.setAttribute(CONNECTOR_ATTRIBUTE, connector);
		applicationContext.setAttributeReadOnly(CONNECTOR_ATTRIBUTE);

	}
	
	public UniquidNode getNode() {
		return uniquidNode;
	}
	
	public RegisterFactory getRegisterFactory() {
		return registerFactory;
	}
	
	public Connector getConnector() {
		return connector;
	}
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	protected abstract Function getFunction(ProviderRequest inputMessage);

	/**
	 * Execute a command in range 0-31
	 * 
	 * @param inputMessage
	 *            message parsed in {@link #parseReqMessage}
	 * @param outputMessage
	 *            object to fill with the execution result
	 * @throws ClassNotFoundException
	 */
	protected final void performProviderRequest(ProviderRequest inputMessage, ProviderResponse outputMessage, byte[] payload) throws Exception {

		Function function = getFunction(inputMessage);

		try {
			if (function != null) {

				try {

					function.service(inputMessage, outputMessage, payload);
					outputMessage.setError(RESULT_OK);

				} catch (Exception ex) {

					LOGGER.error("Exception", ex);
					outputMessage.setError(RESULT_ERROR);

					outputMessage.setResult("Error while executing function: " + ex.getMessage());
				}

			} else {

				outputMessage.setError(RESULT_FUNCTION_NOT_AVAILABLE);
				
				outputMessage.setResult("Function not available");

			}

		} finally {

			// Populate all missing parameters...
			String sender = inputMessage.getSender();

			ProviderRegister providerRegister = registerFactory.getProviderRegister();

			ProviderChannel providerChannel = providerRegister.getChannelByUserAddress(sender);
			
			outputMessage.setSender(providerChannel.getProviderAddress());

		}

	}

	/**
	 * Check if sender is authorized
	 * 
	 * @param sender
	 * @return
	 * @throws Exception
	 */
	protected final byte[] checkSender(ProviderRequest inputMessage) throws Exception {

		// Retrieve sender
		String sender = inputMessage.getSender();

		ProviderRegister providerRegister = registerFactory.getProviderRegister();

		ProviderChannel providerChannel = providerRegister.getChannelByUserAddress(sender);

		// Check if there is a channel available
		if (providerChannel != null) {

			String bitmask = providerChannel.getBitmask();

			// decode
			byte[] b = Hex.decode(bitmask);
			
			// Check first byte:
			if (b[0] == 0) {
				
				// first byte at 0 means original contract with bitmask
				BitSet bitset = BitSet.valueOf(Arrays.copyOfRange(b, 1, b.length));

				int method = inputMessage.getFunction();

				if (bitset.get(method) /*&& 
						WalletUtils.isUnspent(providerChannel.getRevokeTxId(), providerChannel.getRevokeAddress())*/) {

					return b;

				} else {

					throw new Exception("Sender not authorized!");

				}
				
			} else if (b[0] == 1) {
				
				// first byte at 1 means new contract
				
				int method = inputMessage.getFunction();
				
				if (method == b[1]) {
					
					return b;
					
				} else {

					throw new Exception("Sender not authorized!");

				}
				
			} else {
				
				throw new Exception("Invalid contract version!");
				
			}

		} else {

			throw new Exception("Sender not found in Provider register!");

		}
		
	}
	
}
