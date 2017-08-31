package com.uniquid.register.transaction;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;

public abstract class TransactionManagerTest {
	
	public abstract RegisterFactory getRegisterFactory() throws RegisterException;
	
	//@Test
	public void transactionTest() throws Exception {
		
		TransactionManager transactionManager = getRegisterFactory().getTransactionManager();
		
		transactionManager.startTransaction();
		
		List<ProviderChannel> channels = getRegisterFactory().getProviderRegister().getAllChannels();
		
		Assert.assertNotNull(channels);
		Assert.assertEquals(channels.size(), 0);
		
		ProviderChannel providerChannel = new ProviderChannel();
		providerChannel.setProviderAddress("mfuta5iXJNe7yzCaPtmm4W2saiqTbTfxNG");
		providerChannel.setUserAddress("mkw5u34vDegrah5GasD5gKCJQ1NhNGG8tJ");
		providerChannel.setRevokeAddress("mjgWHUCV86eLp7B8mhHUuBAyCS136hz7SH");
		providerChannel.setRevokeTxId("97ab3c1a7bbca566712ab843a65d2e1bf94594b26b2ffe9d3348e4403065c1db");
		providerChannel.setBitmask("00000");

		getRegisterFactory().getProviderRegister().insertChannel(providerChannel);
		
		channels = getRegisterFactory().getProviderRegister().getAllChannels();

		Assert.assertEquals(channels.size(), 1);

		Assert.assertEquals(true, providerChannel.equals(channels.get(0)));
		
		transactionManager.rollbackTransaction();
		
		channels = getRegisterFactory().getProviderRegister().getAllChannels();
		
		Assert.assertNotNull(channels);
		Assert.assertEquals(channels.size(), 0);
		
		transactionManager.startTransaction();
		
		channels = getRegisterFactory().getProviderRegister().getAllChannels();
		
		Assert.assertNotNull(channels);
		Assert.assertEquals(channels.size(), 0);
		
		getRegisterFactory().getProviderRegister().insertChannel(providerChannel);
		
		channels = getRegisterFactory().getProviderRegister().getAllChannels();

		Assert.assertEquals(channels.size(), 1);

		Assert.assertEquals(true, providerChannel.equals(channels.get(0)));
		
		transactionManager.commitTransaction();
		
		channels = getRegisterFactory().getProviderRegister().getAllChannels();
		
		Assert.assertNotNull(channels);
		Assert.assertEquals(channels.size(), 1);
		
		
	}
	
	@Test
	public void multithreadTest() throws Exception {
		
		Thread w1 = new Thread(new Inserter(getRegisterFactory(), 3000), "w1");
		
		Thread w2 = new Thread(new Inserter(getRegisterFactory(), 4000), "w2");

		Thread r1 = new Thread(new Reader(getRegisterFactory(), 12000), "r1");
		
		Thread r2 = new Thread(new Reader(getRegisterFactory(), 11000), "r2");
		
		Thread r3 = new Thread(new Reader(getRegisterFactory(), 15000), "r3");
		
		Thread w3 = new Thread(new Inserter(getRegisterFactory(), 2000), "w3");
		
		r1.start();
		r2.start();
		r3.start();
		
		w1.start();
		w2.start();
		w3.start();
		
		
		r1.join();
		r2.join();
		r3.join();
		w1.join();
		w2.join();
		w3.join();
		
		
		List<ProviderChannel> channels = getRegisterFactory().getProviderRegister().getAllChannels();
		
		Assert.assertNotNull(channels);
		Assert.assertEquals(2101, channels.size());
	}
	
	class Inserter implements Runnable {
		
		private RegisterFactory registerFactory;
		private int duration;
		
		public Inserter(RegisterFactory registerFactory, int duration) {
			this.registerFactory = registerFactory;
			this.duration = duration;
		}

		@Override
		public void run() {

			try {
				
				registerFactory.getTransactionManager().startTransaction();
				
				long now = System.currentTimeMillis();
				int i = 0;
				
				while (System.currentTimeMillis() < (now + duration)) {
				
					String str = Thread.currentThread().getName() + i++;
					
					ProviderChannel providerChannel = new ProviderChannel();
					providerChannel.setProviderAddress(String.valueOf(str));
					providerChannel.setUserAddress(String.valueOf(str));
					providerChannel.setRevokeAddress(String.valueOf(str));
					providerChannel.setRevokeTxId(String.valueOf(str));
					providerChannel.setBitmask(String.valueOf(str));
					
					System.out.println("Writing " + str);
					
					registerFactory.getProviderRegister().insertChannel(providerChannel);
					
					Thread.currentThread().sleep(400);
					
				}
				
				System.out.println(Thread.currentThread().getName() + " finished!");
				
				registerFactory.getTransactionManager().commitTransaction();
			
			} catch (Exception e) {
				
				System.err.println("Exception in " + Thread.currentThread().getName());
				
				e.printStackTrace();
				
				try {

					registerFactory.getTransactionManager().rollbackTransaction();
				
				} catch (Throwable e1) {
					
					System.err.println("Exception in " + Thread.currentThread().getName());

					// TODO Auto-generated catch block
					e1.printStackTrace();

				}
				
			}
			
		}
		
	}
	
	class Reader implements Runnable {
		
		private RegisterFactory registerFactory;
		private int duration;
		
		public Reader(RegisterFactory registerFactory, int duration) {
			this.registerFactory = registerFactory;
			this.duration = duration;
		}

		@Override
		public void run() {

			try {
				
				long now = System.currentTimeMillis();
				
				while (System.currentTimeMillis() < (now + duration)) {
					
					String str = Thread.currentThread().getName();
					
					long size = registerFactory.getProviderRegister().getAllChannels().size();
					
					System.out.println(Thread.currentThread().getName() + " Read elements : " + size);
					
					Thread.currentThread().sleep(100);
					
				}
				
			} catch (Exception e) {
				
				System.err.println("Exception in " + Thread.currentThread().getName());
				
				e.printStackTrace();
				
			}
			
		}
		
	}

}
