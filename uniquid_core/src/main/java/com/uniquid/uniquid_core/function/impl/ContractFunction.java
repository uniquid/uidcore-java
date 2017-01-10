package com.uniquid.uniquid_core.function.impl;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONObject;

import com.uniquid.spv_node.SpvNode;
import com.uniquid.uniquid_core.function.FunctionException;
import com.uniquid.uniquid_core.function.FunctionRequest;
import com.uniquid.uniquid_core.function.FunctionResponse;
import com.uniquid.uniquid_core.function.GenericFunction;

public class ContractFunction extends GenericFunction {

	public ContractFunction(SpvNode spvNode) {
		super(spvNode);
	}

	@Override
	public void service(FunctionRequest functionRequest, FunctionResponse functionResponse)
			throws FunctionException, IOException {
		
		String params = functionRequest.getParameter(FunctionRequest.PARAMS);
		
		JSONObject jsonMessage = new JSONObject(params);
		
		String tx = jsonMessage.getString("tx");
		
		try {
		
			String txid = spvNode.signTransaction(tx);
			
			PrintWriter printWriter = functionResponse.getWriter();
			
			printWriter.print("0 - " + txid);
		
		} catch (Exception ex) {
			
			PrintWriter printWriter = functionResponse.getWriter();
			
			printWriter.print("-1 - " + ex.getMessage());
			
		}
		
	}
	
	@Override
	public String getFunctionName() {
		return "CONTRACT";
	}
	
}
