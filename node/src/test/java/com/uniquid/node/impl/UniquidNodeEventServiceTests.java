package com.uniquid.node.impl;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.uniquid.node.UniquidNodeState;
import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.user.UserChannel;

class UniquidNodeEventListenerTest implements UniquidNodeEventListener {
	
	private boolean triggered = false;
	
	@Override
	public void onUserContractRevoked(UserChannel userChannel) {
		triggered = true;
	}
	
	@Override
	public void onUserContractCreated(UserChannel userChannel) {
		triggered = true;
	}
	
	@Override
	public void onSyncStarted(int blocks) {
		triggered = true;
	}
	
	@Override
	public void onSyncProgress(double pct, int blocksSoFar, Date date) {
		triggered = true;
	}
	
	@Override
	public void onSyncNodeStart() {
		triggered = true;
	}
	
	@Override
	public void onSyncNodeEnd() {
		triggered = true;
	}
	
	@Override
	public void onSyncEnded() {
		triggered = true;
	}
	
	@Override
	public void onProviderContractRevoked(ProviderChannel providerChannel) {
		triggered = true;
	}
	
	@Override
	public void onProviderContractCreated(ProviderChannel providerChannel) {
		triggered = true;
	}
	
	@Override
	public void onNodeStateChange(UniquidNodeState newState) {
		triggered = true;
	}
	
	public boolean isTriggered() {
		return triggered;
	}
	
}

public class UniquidNodeEventServiceTests {
	
	@Test
	public void onProviderContractCreated() throws Exception {
		
		UniquidNodeEventService uniquidNodeEventService = new UniquidNodeEventService();

		UniquidNodeEventListenerTest test = new UniquidNodeEventListenerTest();
		
		uniquidNodeEventService.addUniquidNodeEventListener(test);
		
		uniquidNodeEventService.onProviderContractCreated(null);
		
		Thread.sleep(2000);
		
		Assert.assertTrue(test.isTriggered());
		
	}
	
	@Test
	public void onProviderContractRevoked() throws Exception {
		
		UniquidNodeEventService uniquidNodeEventService = new UniquidNodeEventService();

		UniquidNodeEventListenerTest test = new UniquidNodeEventListenerTest();
		
		uniquidNodeEventService.addUniquidNodeEventListener(test);
		
		uniquidNodeEventService.onProviderContractRevoked(null);
		
		Thread.sleep(2000);
		
		Assert.assertTrue(test.isTriggered());
		
	}
	
	@Test
	public void onUserContractCreated() throws Exception {
		
		UniquidNodeEventService uniquidNodeEventService = new UniquidNodeEventService();

		UniquidNodeEventListenerTest test = new UniquidNodeEventListenerTest();
		
		uniquidNodeEventService.addUniquidNodeEventListener(test);
		
		uniquidNodeEventService.onUserContractCreated(null);
		
		Thread.sleep(2000);
		
		Assert.assertTrue(test.isTriggered());
		
	}
	
	@Test
	public void onUserContractRevoked() throws Exception {
		
		UniquidNodeEventService uniquidNodeEventService = new UniquidNodeEventService();

		UniquidNodeEventListenerTest test = new UniquidNodeEventListenerTest();
		
		uniquidNodeEventService.addUniquidNodeEventListener(test);
		
		uniquidNodeEventService.onUserContractRevoked(null);
		
		Thread.sleep(2000);
		
		Assert.assertTrue(test.isTriggered());
		
	}
	
	@Test
	public void onSyncNodeStart() throws Exception {
		
		UniquidNodeEventService uniquidNodeEventService = new UniquidNodeEventService();

		UniquidNodeEventListenerTest test = new UniquidNodeEventListenerTest();
		
		uniquidNodeEventService.addUniquidNodeEventListener(test);
		
		uniquidNodeEventService.onSyncNodeStart();
		
		Thread.sleep(2000);
		
		Assert.assertTrue(test.isTriggered());
		
	}
	
	@Test
	public void onSyncNodeEnd() throws Exception {
		
		UniquidNodeEventService uniquidNodeEventService = new UniquidNodeEventService();

		UniquidNodeEventListenerTest test = new UniquidNodeEventListenerTest();
		
		uniquidNodeEventService.addUniquidNodeEventListener(test);
		
		uniquidNodeEventService.onSyncNodeEnd();
		
		Thread.sleep(2000);
		
		Assert.assertTrue(test.isTriggered());
		
	}
	
	@Test
	public void onSyncStarted() throws Exception {
		
		UniquidNodeEventService uniquidNodeEventService = new UniquidNodeEventService();

		UniquidNodeEventListenerTest test = new UniquidNodeEventListenerTest();
		
		uniquidNodeEventService.addUniquidNodeEventListener(test);
		
		uniquidNodeEventService.onSyncStarted(3);
		
		Thread.sleep(2000);
		
		Assert.assertTrue(test.isTriggered());
		
	}
	
	@Test
	public void onSyncProgress() throws Exception {
		
		UniquidNodeEventService uniquidNodeEventService = new UniquidNodeEventService();

		UniquidNodeEventListenerTest test = new UniquidNodeEventListenerTest();
		
		uniquidNodeEventService.addUniquidNodeEventListener(test);
		
		uniquidNodeEventService.onSyncProgress(3.0, 10, new Date());
		
		Thread.sleep(2000);
		
		Assert.assertTrue(test.isTriggered());
		
	}
	
	@Test
	public void onSyncEnded() throws Exception {
		
		UniquidNodeEventService uniquidNodeEventService = new UniquidNodeEventService();

		UniquidNodeEventListenerTest test = new UniquidNodeEventListenerTest();
		
		uniquidNodeEventService.addUniquidNodeEventListener(test);
		
		uniquidNodeEventService.onSyncEnded();
		
		Thread.sleep(2000);
		
		Assert.assertTrue(test.isTriggered());
		
	}
	
	@Test
	public void onNodeStateChange() throws Exception {
		
		UniquidNodeEventService uniquidNodeEventService = new UniquidNodeEventService();

		UniquidNodeEventListenerTest test = new UniquidNodeEventListenerTest();
		
		uniquidNodeEventService.addUniquidNodeEventListener(test);
		
		uniquidNodeEventService.onNodeStateChange(UniquidNodeState.IMPRINTING);
		
		Thread.sleep(2000);
		
		Assert.assertTrue(test.isTriggered());
		
	}

}
