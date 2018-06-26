package com.uniquid.node.impl;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.DeterministicSeed;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.uniquid.node.impl.utils.NodeUtils;

public class UniquidKeyBagTest {
	
	@Test
	public void test() throws Exception{
		
		String mnemonic = "sunny current people chicken once sauce radar decade word judge craft when where assume world";
		long creationTime = System.currentTimeMillis() / 1000;
		
		DeterministicSeed detSeed = new DeterministicSeed(mnemonic, null, "", creationTime);
		
		DeterministicKey deterministicKey = NodeUtils
				.createDeterministicKeyFromDeterministicSeed(detSeed);
		
		DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(deterministicKey);
		
		ImmutableList<ChildNumber> list = NodeUtils.listFromPath(NodeUtils.M_BASE_PATH, "0/0/0");

		DeterministicKey signingKey1 = deterministicHierarchy.get(list, true, true);
	
		UniquidKeyBag uniquidKeyBag = new UniquidKeyBag();

		uniquidKeyBag.addDeterministicKey(signingKey1);
		
		list = NodeUtils.listFromPath(NodeUtils.M_BASE_PATH, "1/0/0");
		
		DeterministicKey signingKey2 = deterministicHierarchy.get(list, true, true);
		
		uniquidKeyBag.addDeterministicKey(signingKey2);
		
		Assert.assertEquals(signingKey1, uniquidKeyBag.findKeyFromPubHash(signingKey1.getPubKeyHash()));
		
		Assert.assertEquals(signingKey1, uniquidKeyBag.findKeyFromPubKey(signingKey1.getPubKey()));
		
		Assert.assertEquals(null, uniquidKeyBag.findRedeemDataFromScriptHash(signingKey1.getPubKey()));
		
		Assert.assertEquals(signingKey2, uniquidKeyBag.findKeyFromPubHash(signingKey2.getPubKeyHash()));
		
		Assert.assertEquals(signingKey2, uniquidKeyBag.findKeyFromPubKey(signingKey2.getPubKey()));
		
		Assert.assertEquals(null, uniquidKeyBag.findRedeemDataFromScriptHash(signingKey2.getPubKey()));
		
	}

}
