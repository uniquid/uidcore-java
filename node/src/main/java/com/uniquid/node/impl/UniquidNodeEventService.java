package com.uniquid.node.impl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.node.UniquidNodeState;
import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.user.UserChannel;

/**
 * UniquidNodeEventService allow to manage events asynchronously and split the logic for the subscriber/publisher.
 */
public class UniquidNodeEventService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeEventService.class);
	
	private List<UniquidNodeEventListener> observers;
	
	private ExecutorService executorService;
	
	private ThreadGroup threadGroup;

	/**
	 * Return a new UniquidNodeEventService instance 
	 */
	public UniquidNodeEventService() {

		this.threadGroup = new ThreadGroup("UniquidNodeEventService");
		this.observers = new CopyOnWriteArrayList<UniquidNodeEventListener>();
		this.executorService = Executors.newCachedThreadPool(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable runnable) {

				return new Thread(threadGroup, runnable);

			}
		});
	
	}
	
	/**
	 * Register an event listener
	 * 
	 * @param uniquidNodeEventListener the event listener that will receive callbacks
	 */
	public synchronized void addUniquidNodeEventListener(final UniquidNodeEventListener uniquidNodeEventListener) {
		observers.add(uniquidNodeEventListener);
	}

	/**
	 * Unregister an event listener
	 * 
	 * @param uniquidNodeEventListener the event listener that will be removed
	 */
	public synchronized void removeUniquidNodeEventListener(final UniquidNodeEventListener uniquidNodeEventListener) {
		observers.remove(uniquidNodeEventListener);
	}
	
	/**
	 * Allow to publish the onProviderContractCreated event.
	 * 
	 * @param providerChannel the parameter to pass to subscribers.
	 */
	public void onProviderContractCreated(final ProviderChannel providerChannel) {
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				
				for (UniquidNodeEventListener listener : observers) {
					
					listener.onProviderContractCreated(providerChannel);
					
				}
				
			}
			
		});
	}
	
	/**
	 * Allow to publish the onProviderContractRevoked event.
	 * 
	 * @param providerChannel the parameter to pass to subscribers.
	 */
	public void onProviderContractRevoked(final ProviderChannel providerChannel) {
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				
				for (UniquidNodeEventListener listener : observers) {
					
					listener.onProviderContractRevoked(providerChannel);
					
				}
				
			}
			
		});
	}
	
	/**
	 * Allow to publish the onUserContractCreated event.
	 * 
	 * @param userChannel the parameter to pass to subscribers.
	 */
	public void onUserContractCreated(final UserChannel userChannel) {
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				
				for (UniquidNodeEventListener listener : observers) {
					
					listener.onUserContractCreated(userChannel);
					
				}
				
			}
			
		});
	}
	
	/**
	 * Allow to publish the onUserContractRevoked event.
	 * 
	 * @param userChannel the parameter to pass to subscribers.
	 */
	public void onUserContractRevoked(final UserChannel userChannel) {
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				
				for (UniquidNodeEventListener listener : observers) {
					
					listener.onUserContractRevoked(userChannel);
					
				}
				
			}
			
		});
	}
	
	/**
	 * Allow to publish the onSyncNodeStart event.
	 */
	public void onSyncNodeStart() {
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				
				for (UniquidNodeEventListener listener : observers) {
					
					listener.onSyncNodeStart();
					
				}
				
			}
			
		});
	}
	
	/**
	 * Allow to publish the onSyncNodeEnd event.
	 */
	public void onSyncNodeEnd() {
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				
				for (UniquidNodeEventListener listener : observers) {
					
					listener.onSyncNodeEnd();
					
				}
				
			}
			
		});
	}
	
	/**
	 * Allow to publish the onSyncStarted event.
	 * 
	 * @param blocks the parameter to pass to subscribers.
	 */
	public void onSyncStarted(final int blocks) {
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				
				for (UniquidNodeEventListener listener : observers) {
					
					listener.onSyncStarted(blocks);
					
				}
				
			}
			
		});
	}
	
	/**
	 * Allow to publish the onSyncProgress event.
	 * @param pct
	 * @param blocksSoFar
	 * @param date
	 */
	public void onSyncProgress(final double pct, final int blocksSoFar, final Date date) {
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				
				for (UniquidNodeEventListener listener : observers) {
					
					listener.onSyncProgress(pct, blocksSoFar, date);
					
				}
				
			}
			
		});
	}
	
	/**
	 * Allow to publish the onSyncEnded event.
	 */
	public void onSyncEnded() {
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				
				for (UniquidNodeEventListener listener : observers) {
					
					listener.onSyncEnded();
					
				}
				
			}
			
		});
	}
	
	/**
	 * Allow to publish the onNodeStateChange event.
	 * 
	 * @param newState the parameter to pass to subscribers.
	 */
	public void onNodeStateChange(final UniquidNodeState newState) {
		
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				
				for (UniquidNodeEventListener listener : observers) {
					
					listener.onNodeStateChange(newState);
					
				}
				
			}
			
		});
	}

}
