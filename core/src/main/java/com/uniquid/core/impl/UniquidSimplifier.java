package com.uniquid.core.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.uniquid.node.exception.NodeException;
import org.bitcoinj.utils.ContextPropagatingThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.core.Core;
import com.uniquid.connector.Connector;
import com.uniquid.connector.ConnectorException;
import com.uniquid.connector.EndPoint;
import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.core.provider.impl.ContractFunction;
import com.uniquid.core.provider.impl.EchoFunction;
import com.uniquid.core.provider.impl.FunctionConfigImpl;
import com.uniquid.messages.CapabilityMessage;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.messages.MessageType;
import com.uniquid.messages.UniquidMessage;
import com.uniquid.node.UniquidCapability;
import com.uniquid.node.UniquidNode;
import com.uniquid.node.UniquidNodeState;
import com.uniquid.register.RegisterFactory;

/**
 * Uniquid reference implementation of {@link Core}.
 */
public class UniquidSimplifier extends Core {

	private static final Logger LOGGER = LoggerFactory.getLogger(Core.class.getName());

	private final Map<Integer, Function> functionsMap = new HashMap<>();
	
	private ScheduledExecutorService scheduledExecutorService;
	private ExecutorService receiverExecutorService;

	
	/**
	 * Creates an instance from {@link RegisterFactory}, {@link Connector} and {@link UniquidNode}
	 * @param registerFactory the {@link RegisterFactory} to use
	 * @param node the {@link UniquidNode} to use
	 * @throws Exception in case an error occurs
	 */
	public UniquidSimplifier(RegisterFactory registerFactory, UniquidNode node)
			throws Exception {

		// Call superclass
		super(registerFactory, node);

		// Register core functions
		try {

			addUniquidFunction(new ContractFunction(), 30);
			addUniquidFunction(new EchoFunction(), 31);

		} catch (FunctionException ex) {
			// This will never happens!
		}

		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ContextPropagatingThreadFactory("scheduledExecutorService"));
		receiverExecutorService = Executors.newCachedThreadPool(new ContextPropagatingThreadFactory("receiverExecutorService"));
	}
	
	@Override
	protected Function getFunction(FunctionRequestMessage inputMessage) {

		int rpcMethod = inputMessage.getFunction();

		return functionsMap.get(rpcMethod);

	}

	/**
	 * Register a {@link Function} inside the library with the specified number.
	 * @param function the {@link Function} to register inside the library.
	 * @param functionNumber the number to assign to the {@link Function}
	 * @throws FunctionException in case a problem occurs.
	 */
	public void addFunction(Function function, int functionNumber) throws FunctionException {
		
		LOGGER.trace("Associating function {} with number {}", function.toString(), functionNumber );

		if (functionNumber >= 32) {

			FunctionConfigImpl functionConfigImpl = new FunctionConfigImpl(getFunctionContext());

			function.init(functionConfigImpl);

			functionsMap.put(functionNumber, function);

		} else {

			throw new FunctionException("Invalid function number!");

		}

	}

	/*
	 * Register an internal Function
	 */
	private void addUniquidFunction(Function function, int value) throws FunctionException {
		
		LOGGER.trace("Associating internal function {} with number {}", function.toString(), value );

		if (value >= 0 && value <= 31) {

			FunctionConfigImpl functionConfigImpl = new FunctionConfigImpl(getFunctionContext());

			function.init(functionConfigImpl);

			functionsMap.put(value, function);

		} else {

			throw new FunctionException("Invalid function number!");

		}

	}

	/**
	 *
	 * Initialize the library and start the processing
	 *
	 * @throws Exception in case a problem occurs.
	 */
	public void syncBlockchain() throws NodeException {
		// initialize node if not yet initilized
		if (UniquidNodeState.CREATED.equals(getNode().getNodeState())) {

			LOGGER.info("Initializing node");
			getNode().initNode();

		}

		final Runnable walletSyncher = new Runnable() {

			public void run() {

				try {

					LOGGER.info("Updating node from the BlockChain");

					// Update node from blockchain
					getNode().updateNode();

				} catch (Exception e) {

					LOGGER.error("Exception while updating node from the BlockChain", e);
				}

			}
		};

		final ScheduledFuture<?> walletSyncherFuture = scheduledExecutorService.scheduleWithFixedDelay(walletSyncher, 0, 1,
				TimeUnit.MINUTES);

	}

	public void start(final Connector connector) throws Exception {

		try {
			LOGGER.info("Starting connector");

			this.addConnector(connector);

			// start connector
			connector.start();
		} catch (ConnectorException e) {
			LOGGER.error("Exception while starting the connector", e);
			throw e;
		}

		// Create a thread to wait for messages
		final Runnable receiver = new Runnable() {

			@Override
			public void run() {

				// until not interrupted
				while (!Thread.currentThread().isInterrupted()) {

					try {
						LOGGER.info("Wait to receive request...");

						// this will block until a message is received
						EndPoint endPoint = connector.accept();

						LOGGER.info("Request received!");
						UniquidMessage inputMessage = endPoint.getInputMessage();
						UniquidMessage outputMessage = endPoint.getOutputMessage();

						if (!UniquidNodeState.READY.equals(getNode().getNodeState())) {
							LOGGER.warn("Node is not yet READY! Skipping request");
							continue;
						}

						if (MessageType.FUNCTION_REQUEST.equals(inputMessage.getMessageType())) {

							LOGGER.info("Received input message {}", inputMessage.getMessageType());

						} else if (MessageType.UNIQUID_CAPABILITY.equals(inputMessage.getMessageType())) {

							LOGGER.info("Received capability!");
							CapabilityMessage capabilityMessage = (CapabilityMessage) inputMessage;

							// transform inputMessage to uniquidCapability
							UniquidCapability capability = new UniquidCapability.UniquidCapabilityBuilder()
									.setResourceID(capabilityMessage.getResourceID())
									.setAssigner(capabilityMessage.getAssigner())
									.setAssignee(capabilityMessage.getAssignee())
									.setRights(Hex.decode(capabilityMessage.getRights()))
									.setSince(capabilityMessage.getSince())
									.setUntil(capabilityMessage.getUntil())
									.setAssignerSignature(capabilityMessage.getAssignerSignature())
									.build();

							// tell node to receive provider capability
							getNode().receiveProviderCapability(capability);

							// flush
							endPoint.flush();

							// skip all following code!
							continue;
						} else if (MessageType.ANNOUNCE.equals(inputMessage.getMessageType())) {
							LOGGER.info("Received input message {} - Skip it", inputMessage.getMessageType());
							continue;
						} else {
							LOGGER.info("Unknown message type {} received", inputMessage.getMessageType());
							throw new Exception("Unknown message type");
						}

						// Check if sender is authorized or throw exception
						byte[] payload = checkSender((FunctionRequestMessage) inputMessage);

						LOGGER.info("Performing function...");
						performProviderRequest((FunctionRequestMessage) inputMessage, (FunctionResponseMessage) outputMessage, payload);
						endPoint.flush();
						LOGGER.info("Done!");
					} catch (InterruptedException ex) {
						LOGGER.info("Received request to stop. Exiting");
						return;
					} catch (Throwable t) {
						LOGGER.error("Throwable catched", t);
					}
				}
			}
		};

		// Start receiver
		receiverExecutorService.execute(receiver);
	}

	/**
	 * Stop the library and stop the processing
	 * 
	 * @throws Exception in case a problem occurs
	 */
	public void shutdown() throws Exception {
		
		LOGGER.info("Shutting down!");
		
		try {
			
			LOGGER.info("Stopping connector");
			for (Connector c : getConnectors()) {
				c.stop();
			}
			
		} catch (ConnectorException e) {
			
			LOGGER.error("Exception while stopping the connector", e);
			
		}

		scheduledExecutorService.shutdown();
		receiverExecutorService.shutdownNow();

	}

}
