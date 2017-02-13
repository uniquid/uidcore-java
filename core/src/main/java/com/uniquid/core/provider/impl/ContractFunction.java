package com.uniquid.core.provider.impl;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import com.uniquid.node.impl.UniquidNodeImpl;
import com.uniquid.core.Core;
import com.uniquid.core.InputMessage;
import com.uniquid.core.OutputMessage;
import com.uniquid.core.provider.exception.FunctionException;

public class ContractFunction extends GenericFunction {

	@Override
	public void service(InputMessage inputMessage, OutputMessage outputMessage)
			throws FunctionException, IOException {
		
		String params = inputMessage.getParameter(InputMessage.PARAMS);
		
		JSONObject jsonMessage = new JSONObject(params);
		
		String tx = jsonMessage.getString("tx");

		JSONArray paths = jsonMessage.getJSONArray("paths");
		
		try {
			
			UniquidNodeImpl spvNode = (UniquidNodeImpl) getFunctionContext().getAttribute(Core.NODE_ATTRIBUTE);
		
			String txid = spvNode.signTransaction(tx, paths.getString(0));
			
			PrintWriter printWriter = outputMessage.getWriter();
			
			printWriter.print("0 - " + txid);
		
		} catch (Exception ex) {
			
			PrintWriter printWriter = outputMessage.getWriter();
			
			printWriter.print("-1 - " + ex.getMessage());
			
		}
		
	}

}
