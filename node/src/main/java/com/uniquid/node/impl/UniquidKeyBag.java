package com.uniquid.node.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.RedeemData;

public class UniquidKeyBag implements KeyBag {
	
	HashMap<ByteBuffer, DeterministicKey> pubKeys = new HashMap<ByteBuffer, DeterministicKey>();
	HashMap<ByteBuffer, DeterministicKey> pubKeyhashes = new HashMap<ByteBuffer, DeterministicKey>();
	
	public void addDeterministicKey(DeterministicKey deterministicKey) {
		pubKeys.put(ByteBuffer.wrap(deterministicKey.getPubKey()), deterministicKey);
		pubKeyhashes.put(ByteBuffer.wrap(deterministicKey.getPubKeyHash()), deterministicKey);
	}

	@Override
	public ECKey findKeyFromPubHash(byte[] pubkeyHash) {
		return pubKeyhashes.get(ByteBuffer.wrap(pubkeyHash));
	}

	@Override
	public ECKey findKeyFromPubKey(byte[] pubkey) {
		return pubKeys.get(ByteBuffer.wrap(pubkey));
	}

	@Override
	public RedeemData findRedeemDataFromScriptHash(byte[] scriptHash) {
		return null;
	}

}