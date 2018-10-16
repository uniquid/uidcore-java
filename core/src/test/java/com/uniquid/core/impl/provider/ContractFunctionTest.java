package com.uniquid.core.impl.provider;

import com.uniquid.core.Core;
import com.uniquid.core.impl.test.DummyExceptionNode;
import com.uniquid.core.impl.test.DummyNode;
import com.uniquid.core.provider.FunctionConfig;
import com.uniquid.core.provider.FunctionContext;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.core.provider.impl.ApplicationContext;
import com.uniquid.core.provider.impl.ContractFunction;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;
import org.junit.Assert;
import org.junit.Test;

public class ContractFunctionTest {

	@Test
	public void testService() throws Exception {
		
		ContractFunction contractFunction = new ContractFunction();
		
		Assert.assertNotNull(contractFunction);
		
		FunctionRequestMessage providerRequest = new FunctionRequestMessage();
		providerRequest.setUser("sender");
		providerRequest.setFunction(30);
		providerRequest.setParameters("{ }");

		FunctionResponseMessage providerResponse = new FunctionResponseMessage();
		
		try {

			contractFunction.service(providerRequest, providerResponse, null);
			
		} catch (FunctionException ex) {
			
			Assert.assertEquals("Problem with input JSON", ex.getMessage());
			
		}
		
		FunctionRequestMessage providerRequest2 = new FunctionRequestMessage();
		providerRequest2.setUser("sender");
		providerRequest2.setFunction(30);
		providerRequest2.setParameters("{ \"tx\":\"1234\", \"paths\": [\"1234\"] }");
		
		FunctionResponseMessage providerResponse2 = new FunctionResponseMessage();
		
		contractFunction.init(new FunctionConfig() {
			
			@Override
			public FunctionContext getFunctionContext() {
				ApplicationContext appContext = new ApplicationContext();
				appContext.setAttribute(Core.NODE_ATTRIBUTE, new DummyNode());
				
				return appContext;
			}
		});

		
		contractFunction.service(providerRequest2, providerResponse2, null);
		
		Assert.assertTrue(providerResponse2.getResult().startsWith("0"));
		
		
	}
	
	@Test
	public void testServiceException() throws Exception {
		
		ContractFunction contractFunction = new ContractFunction();
		
		Assert.assertNotNull(contractFunction);
		
		FunctionRequestMessage providerRequest = new FunctionRequestMessage();
		providerRequest.setUser("sender");
		providerRequest.setFunction(30);
		providerRequest.setParameters("{ \"tx\":\"1234\", \"paths\": [\"1234\"] }");
		
		FunctionResponseMessage providerResponse = new FunctionResponseMessage();
		
		try {

			contractFunction.service(providerRequest, providerResponse, null);
			
		} catch (FunctionException ex) {
			
			Assert.assertEquals("Problem with input JSON", ex.getMessage());
			
		}
		
		FunctionRequestMessage providerRequest2 = new FunctionRequestMessage();
		providerRequest2.setUser("sender");
		providerRequest2.setFunction(30);
		providerRequest2.setParameters("{ \"tx\":\"1234\", \"paths\": [\"1234\"] }");
		
		FunctionResponseMessage providerResponse2 = new FunctionResponseMessage();

		contractFunction.init(new FunctionConfig() {
			
			@Override
			public FunctionContext getFunctionContext() {
				ApplicationContext appContext = new ApplicationContext();
				appContext.setAttribute(Core.NODE_ATTRIBUTE, new DummyExceptionNode());
				
				return appContext;
			}
		});

		
		contractFunction.service(providerRequest2, providerResponse2, null);
		
		Assert.assertTrue(providerResponse2.getResult().startsWith("-1"));
		
		
	}
}
