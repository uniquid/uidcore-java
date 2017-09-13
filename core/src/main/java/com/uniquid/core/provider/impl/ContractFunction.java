package com.uniquid.core.provider.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContractFunction.class);

	@Override
	public void service(ProviderRequest inputMessage, ProviderResponse outputMessage, byte[] payload)
			throws FunctionException, IOException {

		String params = inputMessage.getParams();
		String tx;
		
		List<String> path = new ArrayList<String>();
		
		LOGGER.trace("Received input {}", inputMessage);
		
		try {
		
			JSONObject jsonMessage = new JSONObject(params);
			
			tx = jsonMessage.getString("tx");
			
			JSONArray paths = jsonMessage.getJSONArray("paths");
			
			for (int i = 0; i < paths.length(); i++) {

				path.add(paths.getString(i));
				
			}
			
			
		} catch (JSONException ex) {
			
			throw new FunctionException("Problem with input JSON", ex);
			
		}
			
		try {
			
			LOGGER.info("Signing on path {}", path);
			
			UniquidNode spvNode = (UniquidNode) getFunctionContext().getAttribute(Core.NODE_ATTRIBUTE);
		
			String signedTx = spvNode.signTransaction(tx, path);
			
			LOGGER.info("Broadcasting TX");
			
			String txid = spvNode.broadCastTransaction(signedTx);
			
			outputMessage.setResult("0 - " + txid);
		
		} catch (Exception ex) {
			
			outputMessage.setResult("-1 - " + ex.getMessage());
			
		}
		
	}

}
