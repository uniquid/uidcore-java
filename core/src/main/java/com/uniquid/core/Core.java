package com.uniquid.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.connector.Connector;
import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.FunctionContext;
import com.uniquid.core.provider.impl.ApplicationContext;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.node.UniquidNode;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;

/**
 * This is the core of Uniquid library. It contains a collection of functionalities
 * needed by the Uniquid Node to send, receive and decode requests.
 */
public abstract class Core {

	private static final Logger LOGGER = LoggerFactory.getLogger(Core.class.getName());

	public static final String NODE_ATTRIBUTE = com.uniquid.node.UniquidNode.class.getName();
	public static final String REGISTER_FACTORY_ATTRIBUTE = com.uniquid.register.RegisterFactory.class.getName();
	public static final String CONNECTOR_ATTRIBUTE = com.uniquid.connector.Connector.class.getName();

	private RegisterFactory registerFactory;
	private List<Connector> connectors;
	private ApplicationContext applicationContext;
	private UniquidNode uniquidNode;

	/**
	 * Creates an instance from {@link RegisterFactory}, {@link Connector} and {@link UniquidNode}
	 * @param registerFactory the {@link RegisterFactory} to use
	 * @param connector the {@link Connector} to use
	 * @param node the {@link UniquidNode} to use
	 * @throws Exception in case an error occurs
	 */
	public Core(RegisterFactory registerFactory, final Connector connector, UniquidNode node)
			throws Exception {

		this(registerFactory, new ArrayList<Connector>() {
			{
				add(connector);
			}
		}, node);

	}
	
	/**
	 * Creates an instance from {@link RegisterFactory}, {@link Connector} and {@link UniquidNode}
	 * @param registerFactory the {@link RegisterFactory} to use
	 * @param connector the {@link Connector} to use
	 * @param node the {@link UniquidNode} to use
	 * @throws Exception in case an error occurs
	 */
	public Core(RegisterFactory registerFactory, List<Connector> connectors, UniquidNode node)
			throws Exception {

		this.registerFactory = registerFactory;
		this.connectors = connectors;
		this.uniquidNode = node;

		applicationContext = new ApplicationContext();
		applicationContext.setAttribute(NODE_ATTRIBUTE, node);
		applicationContext.setAttributeReadOnly(NODE_ATTRIBUTE);
		applicationContext.setAttribute(REGISTER_FACTORY_ATTRIBUTE, registerFactory);
		applicationContext.setAttributeReadOnly(REGISTER_FACTORY_ATTRIBUTE);
		applicationContext.setAttribute(CONNECTOR_ATTRIBUTE, connectors);
		applicationContext.setAttributeReadOnly(CONNECTOR_ATTRIBUTE);

	}
	
	/**
	 * Retrieve the {@link UniquidNode} instance in use.
	 * @return the {@link UniquidNode} instance in use.
	 */
	public UniquidNode getNode() {
		return uniquidNode;
	}
	
	/**
	 * Retrieve the {@link RegisterFactory} instance in use.
	 * @return the {@link RegisterFactory} instance in use.
	 */
	public RegisterFactory getRegisterFactory() {
		return registerFactory;
	}
	
	/**
	 * Retrieve the {@link Connector} instance in use.
	 * @return the {@link Connector} instance in use.
	 */
	public List<Connector> getConnectors() {
		return connectors;
	}
	
	/**
	 * Retrieve the {@link FunctionContext} instance in use.
	 * @return the {@link FunctionContext} instance in use.
	 */
	public FunctionContext getFunctionContext() {
		return applicationContext;
	}

	/**
	 * Retrieve the {@link Function} related to the {@link ProviderRequest} parameter.
	 * @param providerRequest the {@link ProviderRequest} to fetch the function number from.
	 * @return the {@link Function} related to the {@link ProviderRequest} parameter.
	 */
	protected abstract Function getFunction(FunctionRequestMessage providerRequest);

	/**
	 * Perform the execution of a {@link Function} related to the {@link ProviderRequest} received.
	 * 
	 * @param providerRequest the {@link ProviderRequest} received from the User
	 * @param providerResponse the {@link ProviderResponse} the response to provide to the User
	 * 
	 * @throws Exception in case a problem occurs.
	 */
	protected final void performProviderRequest(final FunctionRequestMessage providerRequest, final FunctionResponseMessage providerResponse, final byte[] payload) throws Exception {

		Function function = getFunction(providerRequest);

		try {

			if (function != null) {

				try {

					function.service(providerRequest, providerResponse, payload);
					providerResponse.setError(FunctionResponseMessage.RESULT_OK);

				} catch (Exception ex) {

					LOGGER.error("Error while executing function", ex);
					providerResponse.setError(FunctionResponseMessage.RESULT_ERROR);

					providerResponse.setResult("Error while executing function: " + ex.getMessage());
				}

			} else {

				providerResponse.setError(FunctionResponseMessage.RESULT_FUNCTION_NOT_AVAILABLE);
				
				providerResponse.setResult("Function not available");

			}

		} finally {

			// Populate all missing parameters...
			String sender = providerRequest.getUser();

			ProviderRegister providerRegister = registerFactory.getProviderRegister();

			ProviderChannel providerChannel = providerRegister.getChannelByUserAddress(sender);
			
			providerResponse.setProvider(providerChannel.getProviderAddress());

		}

	}

	/**
	 * Check if sender is authorized and return the byte array present in the Smart Contract
	 * 
	 * @param providerRequest coming from User.
	 * @return byte array containing the Smart Contract.
	 * 
	 * @throws Exception in case an error occurs.
	 */
	protected final byte[] checkSender(FunctionRequestMessage providerRequest) throws Exception {

		// Retrieve sender
		String sender = providerRequest.getUser();

		ProviderRegister providerRegister = registerFactory.getProviderRegister();

		ProviderChannel providerChannel = providerRegister.getChannelByUserAddress(sender);

		// Check if there is a channel available and dates are valid
		if (providerChannel != null) {

			 if (!providerChannel.isValid()) {

				 throw new Exception("Sender found in Provider register, but contract is expired/not yet valid!");

			 }

			String bitmask = providerChannel.getBitmask();

			// decode
			byte[] b = Hex.decode(bitmask);
			
			// Check first byte:
			if (b[0] == 0) {
				
				// first byte at 0 means original contract with bitmask
				BitSet bitset = BitSet.valueOf(Arrays.copyOfRange(b, 1, b.length));

				int method = providerRequest.getFunction();

				if (bitset.get(method) /*&& 
						WalletUtils.isUnspent(providerChannel.getRevokeTxId(), providerChannel.getRevokeAddress())*/) {

					return b;

				} else {

					throw new Exception("Sender not authorized!");

				}
				
			} else if (b[0] == 1) {
				
				// first byte at 1 means new contract
				
				int method = providerRequest.getFunction();
				
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
