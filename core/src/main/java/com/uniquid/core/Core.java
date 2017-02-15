package com.uniquid.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.node.UniquidNode;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;
import com.uniquid.core.connector.Connector;
import com.uniquid.core.connector.ConnectorFactory;
import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.impl.ApplicationContext;

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
	private Connector<?> connector;
	private ApplicationContext applicationContext;
	private UniquidNode uniquidNode;

	public Core(RegisterFactory registerFactory, ConnectorFactory connectorServiceFactory, UniquidNode node)
			throws Exception {

		this.registerFactory = registerFactory;
		this.connector = connectorServiceFactory.createConnector();
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
	
	public Connector<?> getConnector() {
		return connector;
	}
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	protected abstract Function getFunction(InputMessage<?> inputMessage);

	/**
	 * Execute a command in range 0-31
	 * 
	 * @param inputMessage
	 *            message parsed in {@link #parseReqMessage}
	 * @param outputMessage
	 *            object to fill with the execution result
	 * @throws ClassNotFoundException
	 */
	protected final void performProviderRequest(InputMessage<?> inputMessage, OutputMessage<?> outputMessage, byte[] payload) throws Exception {

		Function function = getFunction(inputMessage);

		try {
			if (function != null) {

				try {

					function.service(inputMessage, outputMessage, payload);
					outputMessage.setParameter(OutputMessage.ERROR, RESULT_OK);

				} catch (Exception ex) {

					LOGGER.error("Exception", ex);
					outputMessage.setParameter(OutputMessage.ERROR, RESULT_ERROR);

					PrintWriter printWriter;
					try {
						printWriter = outputMessage.getWriter();
						printWriter.print("Error while executing function: " + ex.getMessage());
					} catch (IOException ex2) {

						LOGGER.error("Exception", ex2);
					}

				}

			} else {

				outputMessage.setParameter(OutputMessage.ERROR, RESULT_FUNCTION_NOT_AVAILABLE);
				
				PrintWriter printWriter;
				try {
					printWriter = outputMessage.getWriter();
					printWriter.print("Function not available");
				} catch (IOException ex2) {

					LOGGER.error("Exception", ex2);
				}

			}

		} finally {

			// Populate all missing parameters...
			String sender = inputMessage.getParameter(InputMessage.SENDER);

			ProviderRegister providerRegister = registerFactory.getProviderRegister();

			ProviderChannel providerChannel = providerRegister.getChannelByUserAddress(sender);
			
			outputMessage.setParameter(OutputMessage.SENDER, providerChannel.getProviderAddress());

			outputMessage.setParameter(OutputMessage.ID, Long.valueOf(inputMessage.getParameter(InputMessage.ID)));

			outputMessage.setParameter(OutputMessage.RECEIVER_ADDRESS, sender);

		}

	}

	/**
	 * Check if sender is authorized
	 * 
	 * @param sender
	 * @return
	 * @throws Exception
	 */
	protected final byte[] checkSender(InputMessage inputMessage) throws Exception {

		// Retrieve sender
		String sender = inputMessage.getParameter(InputMessage.SENDER);

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

				String method = inputMessage.getParameter(InputMessage.RPC_METHOD);

				if (bitset.get(Integer.valueOf(method)) /*&& 
						WalletUtils.isUnspent(providerChannel.getRevokeTxId(), providerChannel.getRevokeAddress())*/) {

					return b;

				} else {

					throw new Exception("Sender not authorized!");

				}
				
			} else if (b[0] == 1) {
				
				// first byte at 1 means new contract
				
				String method = inputMessage.getParameter(InputMessage.RPC_METHOD);
				
				if (Integer.valueOf(method) == b[1]) {
					
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
	
	/**
	 * Check if sender is authorized
	 * 
	 * @param sender
	 * @return
	 * @throws Exception
	 */
	protected final void checkSender(OutputMessage outputMessage) throws Exception {

		// Retrieve destination
		String receiver = (String) outputMessage.getParameter(OutputMessage.RECEIVER);

		UserRegister userRegister = registerFactory.getUserRegister();

		UserChannel userChannel = userRegister.getChannelByProviderAddress(receiver);

		// Check if there is a channel available
		if (userChannel != null) {

			String bitmask = userChannel.getBitmask();

			// decode
			byte[] b = Hex.decode(bitmask);
			
			// Check first byte:
			if (b[0] == 0) {
			
				BitSet bitset = BitSet.valueOf(Arrays.copyOfRange(b, 1, b.length));
	
				Integer method = (Integer) outputMessage.getParameter(OutputMessage.RPC_METHOD);
	
				if (bitset.get(method)) {
					
					outputMessage.setParameter(OutputMessage.RECEIVER_ADDRESS, userChannel.getProviderName());
	
					return;
	
				} else {
	
					throw new Exception("Sender not authorized!");
	
				}
				
			} else if (b[0] == 1) {
				
				Integer method = (Integer) outputMessage.getParameter(OutputMessage.RPC_METHOD);
	
				if (method.intValue() == b[1]) {
					
					outputMessage.setParameter(OutputMessage.RECEIVER_ADDRESS, userChannel.getProviderName());
	
					return;
	
				} else {
	
					throw new Exception("Sender not authorized!");
	
				}
				
			}

		} else {

			throw new Exception("Provider not found in User register!");

		}

	}

	/**
	 * Execute a call to the Provider
	 * 
	 * @param functionRequest
	 * @return
	 * @throws Exception
	 */
	public final InputMessage<?> performUserRequest(OutputMessage outputMessage, long timeout)
			throws Exception, TimeoutException {
		
		LOGGER.info("Checking sender...");
		
		// Check if sender is authorized or throw exception
		checkSender(outputMessage);

		LOGGER.info("Performing function...");
		
		return connector.sendOutputMessage(outputMessage, timeout);
	}

	public final OutputMessage<?> createOutputMessage() throws Exception {

		return connector.createOutputMessage();

	}

}
