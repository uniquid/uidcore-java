package com.uniquid.node.impl.utils;

import static com.google.common.base.Preconditions.checkState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.Wallet.MissingSigsMode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

public abstract class WalletUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(WalletUtils.class.getName());
	
	private static String URL_REGISTRY = "http://104.130.230.85:8080/registry";
    private static String URL_UTXO = "http://52.167.211.151:3001/insight-api/addr/%1&s/utxo";
    private static String URL_PROVIDER = "http://appliance4.uniquid.co:8080/registry";

	public static void newCompleteTransaction(SendRequest sendRequest, Wallet wallet, NetworkParameters params) throws Exception {

		Transaction tx = sendRequest.tx;
		List<TransactionInput> inputs = tx.getInputs();
		checkState(inputs.size() > 0);
		
		int numInputs = tx.getInputs().size();
		for (int i = 0; i < numInputs; i++) {
			TransactionInput txIn = tx.getInput(i);
			if (txIn.getConnectedOutput() == null) {
				// no connected input linked. We need to search if a transaction is present in wallet

				List<TransactionOutput> candidates = wallet.calculateAllSpendCandidates(true,
						sendRequest.missingSigsMode == MissingSigsMode.THROW);
				
				TransactionOutput cloned = connectedTxOut(candidates, txIn, params);
				
				if (cloned != null) {
					txIn.connect(cloned);
				}
				
			}
		
		}
	}
	
	private static TransactionOutput connectedTxOut(List<TransactionOutput> candidates, TransactionInput txIn, NetworkParameters params) {
		
		for (TransactionOutput outCandidate : candidates) {
			
			if (outCandidate.getOutPointFor().getHash().equals(txIn.getOutpoint().getHash())) {
				
				return cloneTx(outCandidate, params);
				
			}
			
		}
		
		return null;
		
	}
	
	
	private static TransactionOutput cloneTx(TransactionOutput t, NetworkParameters params) {
		
		Transaction original = t.getParentTransaction();
		
		byte[] tSerialized = original.bitcoinSerialize();
		
		// Make a previous tx simply to send us sufficient coins. This prev tx is not really valid but it doesn't
	    // matter for our purposes.
	    Transaction prevTx = new Transaction(params, tSerialized);

	    	return prevTx.getOutput(0);
	    
	}
	
	public static boolean hasTransaction(String txid, Wallet wallet) {
		// NativeSecp256k1.schnorrSign();
		return wallet.getTransaction(Sha256Hash.of(txid.getBytes())) != null;
	}
	
	/**
     * Check if a Transaction have a valid (Uniquid) OP_RETURN
     * */
	public static boolean isValidOpReturn(Transaction tx){
        String op_return = getOpReturn(tx);
        return Hex.decode(op_return).length == 80;
    }
	
    public static boolean isUnspent(String txid, String address){
        String result = httpGet(URL_UTXO, address);

        if(result == null)
            return false;

        JSONArray jArray = new JSONArray(result);

        for(int i = 0; i < jArray.length(); i++){
            JSONObject jsonObject = jArray.getJSONObject(i);
            if(jsonObject.getString("txid").equals(txid) && jsonObject.getInt("vout") == 2)
                return true;
        }

        return false;
    }
    
    public static String retrieveNameFromProvider(String provider){
        String result = httpGet(URL_REGISTRY, null);

        if (result == null)
            return null;

        JSONArray jArray = new JSONArray(result);

        for (int i = 0; i < jArray.length(); i++){
            JSONObject jsonObject = jArray.getJSONObject(i);
            
            String name = jsonObject.getString("provider_name");
            String address = jsonObject.getString("provider_address");
            
            if (provider.equals(address)) {
            		return name;
            }
        }

        return null;
    }
    
    public static String httpGet(String url, String param){
    	try {
            HttpURLConnection connection;
            if(param != null){
                connection = (HttpURLConnection) new URL(url.replace("%1&s", param)).openConnection();
            } else {
                connection = (HttpURLConnection) new URL(url).openConnection();
            }
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if(responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                LOGGER.info("HTTPGET_RESP: " + response.toString());
                return response.toString();
            }
        } catch (java.net.ProtocolException e) {
        		LOGGER.error("Exception", e);
        } catch (MalformedURLException e) {
        		LOGGER.error("Exception", e);
        } catch (IOException e) {
        		LOGGER.error("Exception", e);
        }

        return null;
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
	
}
