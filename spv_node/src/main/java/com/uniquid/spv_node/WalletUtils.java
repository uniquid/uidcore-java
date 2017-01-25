package com.uniquid.spv_node;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.Wallet.MissingSigsMode;

public abstract class WalletUtils {

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
	
}
