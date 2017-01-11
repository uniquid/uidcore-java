package com.uniquid.uniquid_core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.spv_node.SpvNode;
import com.uniquid.uniquid_core.connector.Connector;
import com.uniquid.uniquid_core.connector.ConnectorFactory;
import com.uniquid.uniquid_core.connector.EndPoint;
import com.uniquid.uniquid_core.function.ApplicationContext;
import com.uniquid.uniquid_core.function.Function;
import com.uniquid.uniquid_core.function.FunctionConfigImpl;
import com.uniquid.uniquid_core.function.FunctionContext;
import com.uniquid.uniquid_core.function.FunctionException;
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
	public static final int RESULT_ERROR = 4;

	private RegisterFactory registerFactory;
	private ConnectorFactory connectorServiceFactory;
	private ApplicationContext applicationContext;
	private SpvNode spvNode;

	private Thread thread;

	private final Map<Integer, Function> functionsMap = new HashMap<>();

	public Core(RegisterFactory registerFactory, ConnectorFactory connectorServiceFactory, SpvNode spvNode) {

		this.registerFactory = registerFactory;
		this.connectorServiceFactory = connectorServiceFactory;
		this.spvNode = spvNode;
		
		applicationContext = new ApplicationContext();
		applicationContext.setAttribute("com.uniquid.spv_node.SpvNode", spvNode);
		applicationContext.setAttributeReadOnly("com.uniquid.spv_node.SpvNode");
		applicationContext.setAttribute("com.uniquid.register.RegisterFactory", registerFactory);
		applicationContext.setAttributeReadOnly("com.uniquid.register.RegisterFactory");
		applicationContext.setAttribute("com.uniquid.uniquid_core.connector.ConnectorFactory", connectorServiceFactory);
		applicationContext.setAttributeReadOnly("com.uniquid.uniquid_core.connector.ConnectorFactory");
		
		// Register core functions
		try {

			addFunction(new ContractFunction(), 30);
			addFunction(new EchoFunction(), 31);

		} catch (FunctionException ex) {
			// This will never happens!
		}
	}

	public Function getFunction(FunctionRequest functionRequest) {

		String method = functionRequest.getParameter(FunctionRequest.METHOD);

		return functionsMap.get(Integer.valueOf(method).intValue());

	}

	public void addFunction(Function function, int value) throws FunctionException {

		if (value >= 32) {
			
			FunctionConfigImpl functionConfigImpl = new FunctionConfigImpl(applicationContext);
			
			function.init(functionConfigImpl);

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
	private void performRequest(FunctionRequest functionRequest, FunctionResponse functionResponse) {

		Function function = getFunction(functionRequest);

		if (function != null) {

			try {

				function.service(functionRequest, functionResponse);
				functionResponse.setStatus(RESULT_OK);

			} catch (Exception ex) {

				LOGGER.error("Exception", ex);
				functionResponse.setStatus(RESULT_ERROR);

				PrintWriter printWriter;
				try {
					printWriter = functionResponse.getWriter();
					printWriter.print("Error while executing function: " + ex.getMessage());
				} catch (IOException ex2) {

					LOGGER.error("Exception", ex2);
				}

			}

		} else {

			functionResponse.setStatus(RESULT_NO_FUNCTION);

		}

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

						Connector connectorService = connectorServiceFactory.createConnector();

						// this will block until a message is received
						EndPoint endPoint = connectorService.accept();

						FunctionRequest functionRequest = endPoint.getFunctionRequest();

						FunctionResponse functionResponse = endPoint.getFunctionResponse();

						// Retrieve sender
						String sender = functionRequest.getParameter(FunctionRequest.SENDER);

						// Check if sender is authorized
						if (checkSender(sender)) {

							functionResponse.setSender(spvNode.getWallet().currentReceiveAddress().toBase58());

							performRequest(functionRequest, functionResponse);

							endPoint.close();

						} else {
							
							LOGGER.warn("No channel found associated with address: " + sender);
							
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
	
	/**
	 * Check if sender is authorized
	 * @param sender
	 * @return
	 * @throws Exception
	 */
	public boolean checkSender(String sender) throws Exception {
		
		ProviderRegister providerRegister = registerFactory.createProviderRegister();
		
		ProviderChannel providerChannel = providerRegister.getChannelByUserAddress(sender);
		
		// Check if there is a channel available
		if (providerChannel != null) {
			// check bitmask
			// BitSet bitset = providerChannel.getBitmask();
			
			return true;
		}
		
		return false;
	}

	public void shutdown() {

		thread.interrupt();

		spvNode.stopNode();
	}

}
