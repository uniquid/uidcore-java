package com.uniquid.core.provider.impl;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniquid.core.Core;
import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;
import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.node.UniquidNode;

/**
 * {@link Function} designed to manage Contract signing from Orchestrator 
 */
public class ContractFunction extends GenericFunction {

	@Override
	public void service(ProviderRequest inputMessage, ProviderResponse outputMessage, byte[] payload)
			throws FunctionException, IOException {

		String params = inputMessage.getParams();
		String tx, path;
		
		try {
		
			JSONObject jsonMessage = new JSONObject(params);
			
			tx = jsonMessage.getString("tx");
			
			JSONArray paths = jsonMessage.getJSONArray("paths");
			
			path = paths.getString(0);
			
		} catch (JSONException ex) {
			
			throw new FunctionException("Problem with input JSON", ex);
			
		}
			
		try {
			
			UniquidNode spvNode = (UniquidNode) getFunctionContext().getAttribute(Core.NODE_ATTRIBUTE);
		
			String signedTx = spvNode.signTransaction(tx, path);
			
			String txid = spvNode.broadCastTransaction(signedTx);
			
			outputMessage.setResult("0 - " + txid);
		
		} catch (Exception ex) {
			
			outputMessage.setResult("-1 - " + ex.getMessage());
			
		}
		
	}

}
