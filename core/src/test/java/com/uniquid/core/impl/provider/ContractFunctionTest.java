package com.uniquid.core.impl.provider;

import java.util.Enumeration;

import org.junit.Assert;
import org.junit.Test;

import com.uniquid.core.Core;
import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;
import com.uniquid.core.impl.test.DummyNode;
import com.uniquid.core.impl.test.DummyProviderRequest;
import com.uniquid.core.impl.test.DummyProviderResponse;
import com.uniquid.core.provider.FunctionConfig;
import com.uniquid.core.provider.FunctionContext;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.core.provider.impl.ApplicationContext;
import com.uniquid.core.provider.impl.ContractFunction;

public class ContractFunctionTest {

	@Test
	public void testService() throws Exception {
		
		ContractFunction contractFunction = new ContractFunction();
		
		Assert.assertNotNull(contractFunction);
		
		ProviderRequest providerRequest = new DummyProviderRequest("sender", 30, "");
		
		ProviderResponse providerResponse = new DummyProviderResponse();
		
		try {

			contractFunction.service(providerRequest, providerResponse, null);
			
		} catch (FunctionException ex) {
			
			Assert.assertEquals("Problem with input JSON", ex.getMessage());
			
		}
		

		ProviderRequest providerRequest2 = new DummyProviderRequest("sender", 30, "{ \"tx\":\"1234\", \"paths\": [\"1234\"] } ");
		
		ProviderResponse providerResponse2 = new DummyProviderResponse();
		
		contractFunction.init(new FunctionConfig() {
			
			@Override
			public Enumeration getInitParameterNames() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getInitParameter(String name) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public FunctionContext getFunctionContext() {
				ApplicationContext appContext = new ApplicationContext();
				appContext.setAttribute(Core.NODE_ATTRIBUTE, new DummyNode());
				
				return appContext;
			}
		});

		
		contractFunction.service(providerRequest2, providerResponse2, null);
		
		
	}
}
