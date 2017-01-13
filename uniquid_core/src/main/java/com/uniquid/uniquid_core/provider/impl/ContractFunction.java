package com.uniquid.uniquid_core.provider.impl;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONObject;

import com.uniquid.spv_node.SpvNode;
import com.uniquid.uniquid_core.provider.FunctionException;
import com.uniquid.uniquid_core.provider.GenericFunction;
import com.uniquid.uniquid_core.provider.ProviderRequest;
import com.uniquid.uniquid_core.provider.ProviderResponse;

public class ContractFunction extends GenericFunction {

	@Override
	public void service(ProviderRequest functionRequest, ProviderResponse functionResponse)
			throws FunctionException, IOException {
		
		String params = functionRequest.getParameter(ProviderRequest.PARAMS);
		
		JSONObject jsonMessage = new JSONObject(params);
		
		String tx = jsonMessage.getString("tx");
		
		try {
			
			SpvNode spvNode = (SpvNode) getFunctionContext().getAttribute("com.uniquid.spv_node.SpvNode");
		
			String txid = spvNode.signTransaction(tx);
			
			PrintWriter printWriter = functionResponse.getWriter();
			
			printWriter.print("0 - " + txid);
		
		} catch (Exception ex) {
			
			PrintWriter printWriter = functionResponse.getWriter();
			
			printWriter.print("-1 - " + ex.getMessage());
			
		}
		
	}
	
}
