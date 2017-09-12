package com.uniquid.node.impl;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.RedeemData;

public class UniquidKeyBag implements KeyBag {
	
	private DeterministicKey detKey;
	
	public UniquidKeyBag(DeterministicKey detKey) {
		this.detKey = detKey;
	}

	@Override
	public ECKey findKeyFromPubHash(byte[] pubkeyHash) {
		return detKey;
	}

	@Override
	public ECKey findKeyFromPubKey(byte[] pubkey) {
		return detKey;
	}

	@Override
	public RedeemData findRedeemDataFromScriptHash(byte[] scriptHash) {
		return null;
	}

}