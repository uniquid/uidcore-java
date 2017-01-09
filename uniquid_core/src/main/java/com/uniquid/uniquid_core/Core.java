package com.uniquid.uniquid_core;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.spv_node.SpvNode;
import com.uniquid.uniquid_core.connector.ConnectorService;
import com.uniquid.uniquid_core.connector.ConnectorServiceFactory;
import com.uniquid.uniquid_core.function.Function;
import com.uniquid.uniquid_core.function.FunctionRequest;
import com.uniquid.uniquid_core.function.FunctionResponse;
import com.uniquid.uniquid_core.function.impl.ContractFunction;
import com.uniquid.uniquid_core.function.impl.EchoFunction;

/**
 * This is the core of Uniquid library. It contains all the functionalities
 * needed by the machines to send and receive request.
 */
public final class Core {

	private static final Logger LOGGER = LoggerFactory.getLogger(Core.class.getName());

	public static final int RESULT_OK = 0;
	public static final int RESULT_NO_PERMISSION = 2;
	public static final int RESULT_NO_FUNCTION = 3;

	private RegisterFactory registerFactory;
	private ConnectorServiceFactory connectorServiceFactory;
	private SpvNode spvNode;
	
	private Thread thread;

	private final Map<Integer, Function> functionsMap = new HashMap<>();

	public Core(RegisterFactory registerFactory, ConnectorServiceFactory connectorServiceFactory, SpvNode spvNode) {

		// Register core functions
		functionsMap.put(0, new ContractFunction());
		functionsMap.put(31, new EchoFunction());

		this.registerFactory = registerFactory;
		this.connectorServiceFactory = connectorServiceFactory;
		this.spvNode = spvNode;

	}

	public Function getFunction(FunctionRequest functionRequest) {

		return functionsMap.get(functionRequest.getAttribute(FunctionRequest.FUNCTION_NUMBER));

	}
	
	public void addFunction(Function function, int value) {

		if (value >= 32) {

			functionsMap.put(value, function);

		}

	}
	
	/**
	 * Execute a command in range 0-31
	 * 
	 * @param functionRequest
	 *            message parsed in {@link #parseReqMessage}
	 * @param functionResponse
	 *            object to fill with the execution result
	 * @throws ClassNotFoundException
	 */
	private int performRequest(FunctionRequest functionRequest, FunctionResponse functionResponse) {

		Function function = getFunction(functionRequest);

		if (function != null) {

			try {
				
				function.service(functionRequest, functionResponse);
				return RESULT_OK;

			} catch (Exception ex) {

				LOGGER.error("Exception", ex);

			}

		} else {

			return RESULT_NO_FUNCTION;

		}

		return RESULT_OK;

	}

	/**
	 * Initialize the library and start processing
	 */
	public void start() {
		
		// Init node
		spvNode.startNode();
		
		// Create a thread to wait for messages
		thread = new Thread() {
			
			@Override
			public void run() {
				
				// until not interrupted
				while (!Thread.currentThread().isInterrupted()) {
				
					try {

						ConnectorService connectorService = connectorServiceFactory.createConnectorService();
						
						// this will block until a message is received
						FunctionRequest functionRequest = connectorService.receiveRequest();
						
						// Retrieve sender
						String sender = (String) functionRequest.getAttribute(FunctionRequest.SENDER);
						
						ProviderRegister providerRegister = registerFactory.createProviderRegister();
						
						ProviderChannel providerChannel = providerRegister.getChannelByUserAddress(sender);
						
						// Check if there is a channel available
						if (providerChannel != null) {
							
							// check bitmask
							BitSet bitset = providerChannel.getBitmask();
							
		                    int error = performRequest(functionRequest, null);
	
		                    connectorService.sendResponse(null);
		                    
		            		}
						
					} catch (Exception ex) {
						
						LOGGER.error("Exception catched", ex);

					}
				
				}
				
			}
			
		};
		
		// Start thread
		thread.start();
		
	}
	
	public void shutdown() {
		
		thread.interrupt();
		
		spvNode.stopNode();
	}

}
