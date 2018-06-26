package com.uniquid.node.impl.utils;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import com.google.common.collect.ImmutableList;
import com.uniquid.params.UniquidRegTest;


public class NodeUtilsTest {
	
	public void testSendTransaction() throws Exception {
		
		org.bitcoinj.core.Context currentContext = new org.bitcoinj.core.Context(UniquidRegTest.get());
		org.bitcoinj.core.Context.propagate(currentContext);
		
		String imprinting = "0100000001f9b665bb7e333ae1a36ea3a0b54478f5f13cf4779a5c5e0ff2acbb4d8dbf1db5010000006a47304402202b28f031a63a3e5107a05bb09cf8a12be3a5d659b7bdaef2150f76f0837e39c7022015bfcde7475ceedf556061811b1bcc18290e9fff73195074142ecdc4ed15d8c3012103fd0f9cc115ebf8fa49d8915c0c0cee926d3a6a67754849da230639f0384d25c9ffffffff0240420f00000000001976a914856ef40e30d6f46afb403a46104dbcea53729e7088ace0c35b05000000001976a9149723c240599c36a9dd5a3d3473dbb0bd7875f56a88ac00000000";
		
		Transaction originalTransaction = UniquidRegTest.get().getDefaultSerializer().makeTransaction(Hex.decode(imprinting));
		
		SendRequest sendRequest = SendRequest.forTx(originalTransaction);
		
		NodeUtils.sendTransaction(UniquidRegTest.get(), sendRequest);
		
	}
	
	@Test
	public void testcreateDeterministicKeyFromDeterministicSeed() throws Exception {
		
		String mnemonic = "sunny current people chicken once sauce radar decade word judge craft when where assume world";
		long creationTime = System.currentTimeMillis() / 1000;
		
		DeterministicSeed detSeed = new DeterministicSeed(mnemonic, null, "", creationTime);
		
		DeterministicKey detKey = NodeUtils.createDeterministicKeyFromDeterministicSeed(detSeed);
		
		Assert.assertEquals("09cd6950b9154693eb43f129234b008e6b19d0635ddd0a8e50888f12c25bb4b1", detKey.getPrivateKeyAsHex()); 
		Assert.assertEquals("03ee71bd702ce4b9992260e750a72bc6d0731892554c6aca524e613dd59e764a1b", detKey.getPublicKeyAsHex()); 
		Assert.assertEquals("5670db79964182eb728d43ee737059ba685862ae45ee4da2c1e25f9b1b551132", org.bitcoinj.core.Utils.HEX.encode(detKey.getChainCode()));
		
	}

	@Test
	public void calculateImprintAddressNodeTest() throws Exception {

		String tpub = "tpubDBKYnCCudkHGmnLfM5FK7r1PE8uPsrWwsL4hn6phPJz3Y6TSkLXEavQMuQiEWVxyYTKcrp4e5q1ujLfJQWEsLpvV1mswvY6dQXsMatv4NxY";

		String expectedImprintAddress = "mvMD34qjTuMSoaHifCmjtjiPLXgfFNtCiV";

		Address address = NodeUtils.calculateImprintAddress(tpub, UniquidRegTest.get());

		Assert.assertEquals(expectedImprintAddress, address.toBase58());

		String tpubWithContext = "tpubDDt3WNcECJE3964xRwj8kYe13vrjgbd2SAyqbmJyErz5F1g2p2xj6QFGkr2oHg2tTTipzfA3wxAxs72LaA3qBnCTmmvYBRGsfyTZxuhoVzL";

		address = NodeUtils.calculateImprintAddress(tpubWithContext, UniquidRegTest.get());

		Assert.assertEquals(expectedImprintAddress, address.toBase58());

	}
	
	@Test
	public void testList() throws Exception {
		
		ImmutableList<ChildNumber> list = NodeUtils.listFromPath(NodeUtils.M_BASE_PATH, "0/0/0");
		
		Assert.assertEquals("[44H, 0H, 0, 0, 0, 0]", list.toString());
		
		ImmutableList<ChildNumber> list2 = NodeUtils.listFromPath(NodeUtils.M_BASE_PATH, "1/0/0");
		
		Assert.assertEquals("[44H, 0H, 0, 1, 0, 0]", list2.toString());
		
		ImmutableList<ChildNumber> list3 = NodeUtils.listFromPath(NodeUtils.M_BASE_PATH, "1/0/1");
		
		Assert.assertEquals("[44H, 0H, 0, 1, 0, 1]", list3.toString());
		
		ImmutableList<ChildNumber> list4 = NodeUtils.listFromPath(NodeUtils.M_BASE_PATH, "0/1/0");
		
		Assert.assertEquals("[44H, 0H, 0, 0, 1, 0]", list4.toString());
		
	}

}
