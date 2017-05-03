package com.uniquid.core.provider.impl;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import com.uniquid.node.impl.UniquidNodeImpl;
import com.uniquid.core.Core;
import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;
import com.uniquid.core.provider.exception.FunctionException;

public class ContractFunction extends GenericFunction {

	@Override
	public void service(ProviderRequest inputMessage, ProviderResponse outputMessage, byte[] payload)
			throws FunctionException, IOException {
		
		String params = inputMessage.getParams();
		
		JSONObject jsonMessage = new JSONObject(params);
		
		String tx = jsonMessage.getString("tx");

		JSONArray paths = jsonMessage.getJSONArray("paths");
		
		try {
			
			UniquidNodeImpl spvNode = (UniquidNodeImpl) getFunctionContext().getAttribute(Core.NODE_ATTRIBUTE);
		
			String txid = spvNode.signTransaction(tx, paths.getString(0));
			
			outputMessage.setResult("0 - " + txid);
		
		} catch (Exception ex) {
			
			outputMessage.setResult("-1 - " + ex.getMessage());
			
		}
		
	}

}
