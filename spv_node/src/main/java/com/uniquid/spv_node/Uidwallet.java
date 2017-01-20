package com.uniquid.spv_node;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;

public class Uidwallet extends Wallet {
	
	public Uidwallet(NetworkParameters params) {
		super(params);
	}

	public Uidwallet(Context context) {
		super(context);
	}

	public Uidwallet(NetworkParameters params, KeyChainGroup keyChainGroup) {
		super(params, keyChainGroup);
	}

	public void completeTransaction(SendRequest sendRequest) throws Exception {
		lock.lock();
		try {
			
			Transaction tx = sendRequest.tx;
			List<TransactionInput> inputs = tx.getInputs();
			checkState(inputs.size() > 0);
			
			int numInputs = tx.getInputs().size();
			for (int i = 0; i < numInputs; i++) {
				TransactionInput txIn = tx.getInput(i);
				if (txIn.getConnectedOutput() == null) {
					// no connected input linked. We need to search if a transaction is present in wallet

					List<TransactionOutput> candidates = calculateAllSpendCandidates(true,
							sendRequest.missingSigsMode == MissingSigsMode.THROW);
					
					TransactionOutput cloned = connectedTxOut(candidates, txIn);
					
					if (cloned != null) {
						txIn.connect(cloned);
					}
					
				}
			
			}
			
		} finally {
			lock.unlock();
		}
	}
	
	private TransactionOutput connectedTxOut(List<TransactionOutput> candidates, TransactionInput txIn) {
		
		for (TransactionOutput outCandidate : candidates) {
			
			if (outCandidate.getOutPointFor().getHash().equals(txIn.getOutpoint().getHash())) {
				
				return cloneTx(outCandidate);
				
			}
			
		}
		
		return null;
		
	}
	
	
	private TransactionOutput cloneTx(TransactionOutput t) {
		
		Transaction original = t.getParentTransaction();
		
		byte[] tSerialized = original.bitcoinSerialize();
		
		// Make a previous tx simply to send us sufficient coins. This prev tx is not really valid but it doesn't
	    // matter for our purposes.
	    Transaction prevTx = new Transaction(params, tSerialized);

	    	return prevTx.getOutput(0);
	    
	}
	
	public boolean hasTransaction(String txid) {
		// NativeSecp256k1.schnorrSign();
		return getTransaction(Sha256Hash.of(txid.getBytes())) != null;
	}

}
