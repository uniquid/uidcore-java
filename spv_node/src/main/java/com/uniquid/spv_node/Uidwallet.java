package com.uniquid.spv_node;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.signers.MissingSigResolutionSigner;
import org.bitcoinj.signers.TransactionSigner;
import org.bitcoinj.wallet.DecryptingKeyBag;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.RedeemData;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.Wallet.MissingSigsMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Uidwallet extends Wallet {
	
	private static final Logger log = LoggerFactory.getLogger(Wallet.class);

	public Uidwallet(NetworkParameters params) {
		super(params);
	}

	public Uidwallet(Context context) {
		super(context);
	}

	public Uidwallet(NetworkParameters params, KeyChainGroup keyChainGroup) {
		super(params, keyChainGroup);
	}

	public void peppeSignTransaction(SendRequest req) {

		lock.lock();
		try {
			Transaction tx = req.tx;
			List<TransactionInput> inputs = tx.getInputs();
			List<TransactionOutput> outputs = tx.getOutputs();
			checkState(inputs.size() > 0);
			checkState(outputs.size() > 0);

			KeyBag maybeDecryptingKeyBag = new DecryptingKeyBag(this, req.aesKey);

			int numInputs = tx.getInputs().size();
			for (int i = 0; i < numInputs; i++) {
				TransactionInput txIn = tx.getInput(i);
				if (txIn.getConnectedOutput() == null) {
					// l'input non ci sta.
					// cerchiamolo nel wallet

					List<TransactionOutput> candidates = calculateAllSpendCandidates(true,
							req.missingSigsMode == MissingSigsMode.THROW);

					for (TransactionOutput outCandidate : candidates) {

						txIn.connect(outCandidate.getOutPointFor().getConnectedOutput());
					}

					break;
				}

				try {
					// We assume if its already signed, its hopefully got a
					// SIGHASH type that will not invalidate when
					// we sign missing pieces (to check this would require
					// either assuming any signatures are signing
					// standard output types or a way to get processed
					// signatures out of script execution)
					txIn.getScriptSig().correctlySpends(tx, i, txIn.getConnectedOutput().getScriptPubKey());
					log.warn(
							"Input {} already correctly spends output, assuming SIGHASH type used will be safe and skipping signing.",
							i);
					continue;
				} catch (ScriptException e) {
					log.debug("Input contained an incorrect signature", e);
					// Expected.
				}

				Script scriptPubKey = txIn.getConnectedOutput().getScriptPubKey();
				RedeemData redeemData = txIn.getConnectedRedeemData(maybeDecryptingKeyBag);
				checkNotNull(redeemData, "Transaction exists in wallet that we cannot redeem: %s",
						txIn.getOutpoint().getHash());
				txIn.setScriptSig(scriptPubKey.createEmptyInputScript(redeemData.keys.get(0), redeemData.redeemScript));
			}

			TransactionSigner.ProposedTransaction proposal = new TransactionSigner.ProposedTransaction(tx);
			for (TransactionSigner signer : signers) {
				if (!signer.signInputs(proposal, maybeDecryptingKeyBag))
					log.info("{} returned false for the tx", signer.getClass().getName());
			}

			// resolve missing sigs if any
			new MissingSigResolutionSigner(req.missingSigsMode).signInputs(proposal, maybeDecryptingKeyBag);
		} finally {
			lock.unlock();
		}

	}

}
