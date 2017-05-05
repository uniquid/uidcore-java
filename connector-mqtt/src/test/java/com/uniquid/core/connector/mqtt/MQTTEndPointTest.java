package com.uniquid.core.connector.mqtt;

import org.junit.Assert;
import org.junit.Test;

import com.uniquid.core.connector.ConnectorException;

public class MQTTEndPointTest {
	
	@Test
	public void contructorTest() {
		String broker = "tcp://appliance4.uniquid.co:1883";
		
		RPCProviderRequest rpcProviderRequest = new RPCProviderRequest
				.Builder()
				.set_sender("sender")
				.set_rpcMethod(33)
				.set_params("hola!")
				.build();
		
		String request = rpcProviderRequest.toJSONString();
		byte[] mqttMessageRequest = request.getBytes();
		
		try {
			MQTTEndPoint mqttEndPoint = new MQTTEndPoint(mqttMessageRequest, broker);
			RPCProviderRequest endpointRequest = (RPCProviderRequest) mqttEndPoint.getInputMessage();
			Assert.assertEquals(rpcProviderRequest.getSender(), endpointRequest.getSender());
			Assert.assertEquals(rpcProviderRequest.getParams(), endpointRequest.getParams());
			Assert.assertEquals(rpcProviderRequest.getFunction(), endpointRequest.getFunction());
			Assert.assertEquals(rpcProviderRequest.getId(), endpointRequest.getId());
			
		} catch (ConnectorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
