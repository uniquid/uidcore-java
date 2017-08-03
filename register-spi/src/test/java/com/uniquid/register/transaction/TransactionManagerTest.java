package com.uniquid.register.transaction;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;

public abstract class TransactionManagerTest {
	
	public abstract RegisterFactory getRegisterFactory() throws RegisterException;
	
	@Test
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
		
		Thread t1 = new Thread(new Inserter(getRegisterFactory(), 50), "t1");
		
		Thread t2 = new Thread(new Inserter(getRegisterFactory(), 200), "t2");
		
		t1.start();
		t2.start();
		
		t1.join();
		t2.join();
		
		List<ProviderChannel> channels = getRegisterFactory().getProviderRegister().getAllChannels();
		
		Assert.assertNotNull(channels);
		Assert.assertEquals(channels.size(), 251);
	}
	
	class Inserter implements Runnable {
		
		private RegisterFactory registerFactory;
		private int loop;
		
		public Inserter(RegisterFactory registerFactory, int loop) {
			this.registerFactory = registerFactory;
			this.loop = loop;
		}

		@Override
		public void run() {

			try {
				
				registerFactory.getTransactionManager().startTransaction();
				
				for (int i = 0; i < loop; i++) {
					
					String str = Thread.currentThread().getName() + i;
					
					ProviderChannel providerChannel = new ProviderChannel();
					providerChannel.setProviderAddress(String.valueOf(str));
					providerChannel.setUserAddress(String.valueOf(str));
					providerChannel.setRevokeAddress(String.valueOf(str));
					providerChannel.setRevokeTxId(String.valueOf(str));
					providerChannel.setBitmask(String.valueOf(str));
					
					System.out.println("Inserting " + providerChannel);
					
					registerFactory.getProviderRegister().insertChannel(providerChannel);
					
				}
				
				registerFactory.getTransactionManager().commitTransaction();
			
			} catch (Exception e) {
				
				try {

					registerFactory.getTransactionManager().rollbackTransaction();
				
				} catch (Throwable e1) {

					// TODO Auto-generated catch block
					e1.printStackTrace();

				}
				
			}
			
		}
		
	}

}
