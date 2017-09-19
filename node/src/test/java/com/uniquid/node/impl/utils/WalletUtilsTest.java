package com.uniquid.node.impl.utils;

import org.bitcoinj.core.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.node.impl.params.UniquidRegTest;

public class WalletUtilsTest {
	
	@Test
	public void testOpReturn() throws Exception {
		
		String imprinting = "0100000001f9b665bb7e333ae1a36ea3a0b54478f5f13cf4779a5c5e0ff2acbb4d8dbf1db5010000006a47304402202b28f031a63a3e5107a05bb09cf8a12be3a5d659b7bdaef2150f76f0837e39c7022015bfcde7475ceedf556061811b1bcc18290e9fff73195074142ecdc4ed15d8c3012103fd0f9cc115ebf8fa49d8915c0c0cee926d3a6a67754849da230639f0384d25c9ffffffff0240420f00000000001976a914856ef40e30d6f46afb403a46104dbcea53729e7088ace0c35b05000000001976a9149723c240599c36a9dd5a3d3473dbb0bd7875f56a88ac00000000";
		
		Transaction originalTransaction = UniquidRegTest.get().getDefaultSerializer().makeTransaction(Hex.decode(imprinting));
		
		Assert.assertNull(WalletUtils.getOpReturn(originalTransaction));
		
		Assert.assertNull(WalletUtils.getOpReturnAsByteArray(originalTransaction));
		
		Assert.assertFalse(WalletUtils.isValidOpReturn(originalTransaction));
		
		String contract = "0100000001de7ba00f57e9eff0800a1e1a320b50ab9f211bd88513dc269b6c3eadc6d1fc82030000006b48304502210083a33d3e9b16ce40fd5d345c0ea72e6b4841d4a4620d4c938bb82a560bf8b96e02202a066892a719b21cadfc73d904146cbbd1dfb734d2210d7883080e6a207bd095012102254109107da0a2865c55fe86f1ffb667e32ddc3e2682fbd819178108781319f8ffffffff0410270000000000001976a9142e2b75077116bb2f2fe1a3e76329363c6df2dc2588ac0000000000000000536a4c5000000000003e000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010270000000000001976a9146fd6ef3235d1dcf69e9248825447fe6d42105fce88acd04e0900000000001976a914c99699e460544b03f886c0ef0005e9d7d6e7bc5788ac00000000";
		
		Transaction tx = UniquidRegTest.get().getDefaultSerializer().makeTransaction(Hex.decode(contract));

		Assert.assertArrayEquals(new byte[] {0, 0, 0, 0, 0, 62, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, WalletUtils.getOpReturnAsByteArray(tx));
		
		Assert.assertEquals("00000000003e0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", WalletUtils.getOpReturn(tx));
		
		Assert.assertTrue(WalletUtils.isValidOpReturn(tx));
	}
	
}
