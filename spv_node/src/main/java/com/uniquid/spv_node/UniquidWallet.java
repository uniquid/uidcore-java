package com.uniquid.spv_node;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;

import com.google.common.collect.ImmutableList;

public class UniquidWallet extends Wallet {
	
	public UniquidWallet(NetworkParameters params) {
		super(params);
	}

	public UniquidWallet(Context context) {
		super(context);
	}

	public UniquidWallet(NetworkParameters params, KeyChainGroup keyChainGroup) {
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
	
//	@Override
//	public boolean isDeterministicUpgradeRequired() {
//        return false;
//    }
	
	public static UniquidWallet fromKeys(NetworkParameters params, List<ECKey> keys) {
        for (ECKey key : keys)
            checkArgument(!(key instanceof DeterministicKey));

        KeyChainGroup group = new KeyChainGroup(params);
        group.importKeys(keys);
        return new UniquidWallet(params, group);
    }
	
	public static Wallet fromSeed(NetworkParameters params, DeterministicSeed seed, ImmutableList<ChildNumber> accountPath) {
        return new UniquidWallet(params, new KeyChainGroup(params, seed, accountPath));
    }
	
}
