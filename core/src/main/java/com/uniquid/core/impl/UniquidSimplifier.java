package com.uniquid.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.uniquid.core.Listener;
import com.uniquid.node.exception.NodeException;
import org.bitcoinj.utils.ContextPropagatingThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.core.Core;
import com.uniquid.connector.Connector;
import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.core.provider.impl.ContractFunction;
import com.uniquid.core.provider.impl.EchoFunction;
import com.uniquid.core.provider.impl.FunctionConfigImpl;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.node.UniquidNode;
import com.uniquid.node.UniquidNodeState;
import com.uniquid.register.RegisterFactory;

/**
 * Uniquid reference implementation of {@link Core}.
 */
public class UniquidSimplifier extends Core {

	private static final Logger LOGGER = LoggerFactory.getLogger(Core.class.getName());

	private final Map<Integer, Function> functionsMap = new HashMap<>();
	
	private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ContextPropagatingThreadFactory("scheduledExecutorService"));

	private List<Listener> listeners = new ArrayList<>();
	private ExecutorService threadPool = Executors.newCachedThreadPool(new ContextPropagatingThreadFactory("threadPool"));
	
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

	public void syncBlockchain() throws NodeException {
		// initialize node if not yet initilized
		if (UniquidNodeState.CREATED.equals(getNode().getNodeState())) {

			LOGGER.info("Initializing node");
			getNode().initNode();

		}

		final Runnable walletSyncher = () -> {
            try {
                LOGGER.info("Updating node from the BlockChain");
                // Update node from blockchain
                getNode().updateNode();
            } catch (Exception e) {
                LOGGER.error("Exception while updating node from the BlockChain", e);
            }
        };

		final ScheduledFuture<?> walletSyncherFuture = scheduledExecutorService.scheduleWithFixedDelay(walletSyncher, 0, 1,
				TimeUnit.MINUTES);
	}

	/**
	 * Stop the library and stop the processing
	 * 
	 * @throws Exception in case a problem occurs
	 */
	public void shutdown() {
		LOGGER.info("Shutting down!");

		scheduledExecutorService.shutdown();
		threadPool.shutdownNow();
		listeners.clear();

	}

	/**
	 * Add listener who can catch and handle incoming through the connector messages
	 * @param listener
	 * @return
	 */
	public boolean addListener(Listener listener) {
		if (listeners.add(listener)) {
			listener.setParentSimplifier(this);
			threadPool.execute(listener);
			return true;
		}
		return false;
	}
}
