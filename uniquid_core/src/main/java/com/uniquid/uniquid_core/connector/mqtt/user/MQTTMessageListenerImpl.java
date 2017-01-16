package com.uniquid.uniquid_core.connector.mqtt.user;

import java.util.concurrent.TimeoutException;

import com.uniquid.uniquid_core.InputMessage;
import com.uniquid.uniquid_core.connector.mqtt.JSONMessage;

public class MQTTMessageListenerImpl implements MQTTMessageListener<JSONMessage> {

	private int id;
	private JSONMessage received;
	private Object syncObj;

	public MQTTMessageListenerImpl(int id) {
		this.id = id;
		this.syncObj = new Object();
	}

	@Override
	public void receive(JSONMessage message) {
		
		synchronized (syncObj) {
		
			received = message;
			
			syncObj.notify();
			
		}
		
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public InputMessage<JSONMessage> waitForResponse(long timeout) {
		
		try {

			synchronized (syncObj) {

				while (received == null) {

					syncObj.wait(timeout);
					
					// if we are here, then timeout raised and no response 
					if (received == null) {
						throw new TimeoutException("Timeout elapsed!");
					}

				}
				
				return new MQTTMessageResponse(received);
			}

		} catch (Exception ex) {

			return null;

		}
	}

}
