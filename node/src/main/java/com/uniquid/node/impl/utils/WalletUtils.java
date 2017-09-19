package com.uniquid.node.impl.utils;

import java.util.List;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

/**
 * Utility class that contains useful methods for managing wallets
 */
public abstract class WalletUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WalletUtils.class.getName());
	
	/**
     * Check if a Transaction have a valid (Uniquid) OP_RETURN
     * */
	public static boolean isValidOpReturn(Transaction tx){
        String op_return = getOpReturn(tx);
        
        if (op_return != null) {

        	return Hex.decode(op_return).length == 80;

        } else {
        	
        	return false;
        
        }
    }
	
	/**
     * Check if a Transaction have a valid (Uniquid) OP_RETURN
     * */
	public static boolean isValidOpReturn(TransactionOutput to){
		
		String op_return = getOpReturn(to);
        
        if (op_return != null) {

        	return Hex.decode(op_return).length == 80;

        } else {
        	
        	return false;
        
        }
        
    }
	
    /**
     * Retrieve OP_RETURN from a Transaction
     * */
    public static String getOpReturn(Transaction tx){
        List<TransactionOutput> to = tx.getOutputs();
        Script script = to.get(1).getScriptPubKey();
        if(script.isOpReturn()){
            String script_string = script.toString();
            return script_string.substring(script_string.indexOf("[")+1, script_string.indexOf("]"));
        }
        return null;
    }
    
    public static String getOpReturn(TransactionOutput to) {
    	
        Script script = to.getScriptPubKey();
        
        if (script.isOpReturn()) {
        	
            String script_string = script.toString();
            return script_string.substring(script_string.indexOf("[")+1, script_string.indexOf("]"));
            
        }
        
        return null;
    }
	
}
