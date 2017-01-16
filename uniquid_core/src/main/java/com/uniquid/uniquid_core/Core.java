package com.uniquid.uniquid_core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.spv_node.SpvNode;
import com.uniquid.uniquid_core.connector.Connector;
import com.uniquid.uniquid_core.connector.ConnectorException;
import com.uniquid.uniquid_core.connector.ConnectorFactory;
import com.uniquid.uniquid_core.connector.EndPoint;
import com.uniquid.uniquid_core.provider.ApplicationContext;
import com.uniquid.uniquid_core.provider.FunctionConfigImpl;
import com.uniquid.uniquid_core.provider.FunctionException;
import com.uniquid.uniquid_core.provider.ProviderFunction;
import com.uniquid.uniquid_core.provider.impl.ContractFunction;
import com.uniquid.uniquid_core.provider.impl.EchoFunction;

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
	private Connector<?> connectorService;
	private ApplicationContext applicationContext;
	private SpvNode spvNode;

	private Thread thread;

	private final Map<Integer, ProviderFunction> functionsMap = new HashMap<>();

	public Core(RegisterFactory registerFactory, ConnectorFactory connectorServiceFactory, SpvNode spvNode) throws Exception {

		this.registerFactory = registerFactory;
		this.connectorService = connectorServiceFactory.createConnector();
		this.spvNode = spvNode;
		
		applicationContext = new ApplicationContext();
		applicationContext.setAttribute("com.uniquid.spv_node.SpvNode", spvNode);
		applicationContext.setAttributeReadOnly("com.uniquid.spv_node.SpvNode");
		applicationContext.setAttribute("com.uniquid.register.RegisterFactory", registerFactory);
		applicationContext.setAttributeReadOnly("com.uniquid.register.RegisterFactory");
		applicationContext.setAttribute("com.uniquid.uniquid_core.connector.Connector", connectorService);
		applicationContext.setAttributeReadOnly("com.uniquid.uniquid_core.connector.Connector");
		
		// Register core functions
		try {

			addUniquidFunction(new ContractFunction(), 30);
			addUniquidFunction(new EchoFunction(), 31);

		} catch (FunctionException ex) {
			// This will never happens!
		}
	}

	private ProviderFunction getFunction(InputMessage<?> inputMessage) {

		String method = inputMessage.getParameter(InputMessage.METHOD);

		return functionsMap.get(Integer.valueOf(method).intValue());

	}

	public void addFunction(ProviderFunction function, int value) throws FunctionException {

		if (value >= 32) {
			
			FunctionConfigImpl functionConfigImpl = new FunctionConfigImpl(applicationContext);
			
			function.init(functionConfigImpl);

			functionsMap.put(value, function);

		} else {
			
			throw new FunctionException("Invalid function number!");
			
		}

	}
	
	private void addUniquidFunction(ProviderFunction function, int value) throws FunctionException {
		
		if (value >= 0 && value <= 31) {
			
			FunctionConfigImpl functionConfigImpl = new FunctionConfigImpl(applicationContext);
			
			function.init(functionConfigImpl);

			functionsMap.put(value, function);
			
		} else {
			
			throw new FunctionException("Invalid function number!");
			
		}
		
	}

	/**
	 * Execute a command in range 0-31
	 * 
	 * @param inputMessage
	 *            message parsed in {@link #parseReqMessage}
	 * @param outputMessage
	 *            object to fill with the execution result
	 * @throws ClassNotFoundException
	 */
	private void performProviderRequest(InputMessage<?> inputMessage, OutputMessage<?> outputMessage) {

		ProviderFunction function = getFunction(inputMessage);

		if (function != null) {

			try {

				function.service(inputMessage, outputMessage);
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

			} finally {
				
				// Populate all missing parameters...
				outputMessage.setParameter(OutputMessage.SENDER, spvNode.getWallet().currentReceiveAddress().toBase58());
				
				outputMessage.setParameter(OutputMessage.ID, Long.valueOf(inputMessage.getParameter(InputMessage.ID)));
				
				String sender = inputMessage.getParameter(InputMessage.SENDER);
				outputMessage.setDestination(sender);
			}

		} else {

			outputMessage.setParameter(OutputMessage.ERROR, RESULT_NO_FUNCTION);

		}

	}

	/**
	 * Initialize the library and start processing
	 */
	public void start() {

		// Init node
		spvNode.startNode();
		
		// start connector
		try {
			connectorService.start();
		} catch (ConnectorException e) {
			LOGGER.error("Exception", e);
		}

		// Create a thread to wait for messages
		thread = new Thread() {

			@Override
			public void run() {

				// until not interrupted
				while (!Thread.currentThread().isInterrupted()) {

					try {

						// this will block until a message is received
						EndPoint<?> endPoint = connectorService.accept();

						InputMessage<?> inputMessage = endPoint.getInputMessage();

						OutputMessage<?> outputMessage = endPoint.getOutputMessage();

						// Check if sender is authorized
						if (checkSender(inputMessage)) {
							
							performProviderRequest(inputMessage, outputMessage);

							endPoint.close();

						} else {
							
							LOGGER.warn("Skipping Request! Sender " +
							inputMessage.getParameter(InputMessage.SENDER) + " not authorized for method " +
							inputMessage.getParameter(InputMessage.METHOD));
							
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
	private boolean checkSender(InputMessage inputMessage) throws Exception {
		
		// Retrieve sender
		String sender = inputMessage.getParameter(InputMessage.SENDER);
		
		ProviderRegister providerRegister = registerFactory.createProviderRegister();
		
		ProviderChannel providerChannel = providerRegister.getChannelByUserAddress(sender);
		
		// Check if there is a channel available
		if (providerChannel != null) {
			
			String bitmask = providerChannel.getBitmask();
			
			// decode
			byte[] b = Hex.decode(bitmask);
			
			BitSet bitset = BitSet.valueOf(b);
			
			String method = inputMessage.getParameter(InputMessage.METHOD);
			
			if (bitset.get(Integer.valueOf(method))) {
				
				return true;

			} else {
			
				return false;
			
			}
			
		}
		
		return false;
	}

	public void shutdown() {

		thread.interrupt();

		spvNode.stopNode();
		
		try {
			connectorService.stop();
		} catch (ConnectorException e) {
			LOGGER.error("Error", e);
		}
	}
	
	/**
	 * Execute a call to the Provider
	 * @param functionRequest
	 * @return
	 * @throws Exception 
	 */
	public InputMessage<?> performUserRequest(OutputMessage outputMessage, long timeout) throws Exception, TimeoutException {
		
		return connectorService.sendOutputMessage(outputMessage, timeout);
	}
	
	public OutputMessage<?> createOutputMessage() throws Exception {
		
		return connectorService.createOutputMessage();
		
	}

}
