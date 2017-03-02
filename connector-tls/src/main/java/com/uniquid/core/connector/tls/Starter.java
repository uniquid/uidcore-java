package com.uniquid.core.connector.tls;

import java.util.Timer;
import java.util.TimerTask;

public class Starter {
	
	private Boolean received = false;
	
	private String message;
	
	public Starter(String message) {
		
		this.message = message;
		
	}
	
	public Object startServiceWrapper() {
		
		synchronized (received) {
			
			// Send BT data
			
			
			
			
			while (!received) {
				
				try {
					
					Timer t = new Timer();
					
					t.schedule(new TimerTask() {
						
						@Override
						public void run() {

							synchronized (received) {
								
								received = true;
								
							}
							
						}
					}, 20000);
					
					received.wait();

				} catch (InterruptedException e) {
				
					// Show exception
					
				}
				
			}
			
		}
		
		
		// Read risposta
		
		return null;
	}
	
	
	private void eventReceived() {
		
		synchronized (received) {
			
			received = true;
			
			received.notify();
			
		}
	}

}
