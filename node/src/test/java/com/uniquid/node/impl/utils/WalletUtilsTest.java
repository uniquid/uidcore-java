package com.uniquid.node.impl.utils;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.node.impl.params.UniquidRegTest;

public class WalletUtilsTest {
	
	@Test
	public void testCloneTx() {
		
		String imprinting = "0100000001f9b665bb7e333ae1a36ea3a0b54478f5f13cf4779a5c5e0ff2acbb4d8dbf1db5010000006a47304402202b28f031a63a3e5107a05bb09cf8a12be3a5d659b7bdaef2150f76f0837e39c7022015bfcde7475ceedf556061811b1bcc18290e9fff73195074142ecdc4ed15d8c3012103fd0f9cc115ebf8fa49d8915c0c0cee926d3a6a67754849da230639f0384d25c9ffffffff0240420f00000000001976a914856ef40e30d6f46afb403a46104dbcea53729e7088ace0c35b05000000001976a9149723c240599c36a9dd5a3d3473dbb0bd7875f56a88ac00000000";
		
		Transaction originalTransaction = UniquidRegTest.get().getDefaultSerializer().makeTransaction(Hex.decode(imprinting));
		
		TransactionOutput cloned = WalletUtils.cloneTx(originalTransaction.getOutput(0), UniquidRegTest.get());
		
		Assert.assertEquals(originalTransaction.getOutput(0).toString(), cloned.toString());
		
	}
	
	@Test
	public void testOpReturn() {
		
		String imprinting = "0100000001f9b665bb7e333ae1a36ea3a0b54478f5f13cf4779a5c5e0ff2acbb4d8dbf1db5010000006a47304402202b28f031a63a3e5107a05bb09cf8a12be3a5d659b7bdaef2150f76f0837e39c7022015bfcde7475ceedf556061811b1bcc18290e9fff73195074142ecdc4ed15d8c3012103fd0f9cc115ebf8fa49d8915c0c0cee926d3a6a67754849da230639f0384d25c9ffffffff0240420f00000000001976a914856ef40e30d6f46afb403a46104dbcea53729e7088ace0c35b05000000001976a9149723c240599c36a9dd5a3d3473dbb0bd7875f56a88ac00000000";
		
		Transaction originalTransaction = UniquidRegTest.get().getDefaultSerializer().makeTransaction(Hex.decode(imprinting));
		
		Assert.assertNull(WalletUtils.getOpReturn(originalTransaction));
		
		Assert.assertFalse(WalletUtils.isValidOpReturn(originalTransaction));
		
	}
	
	@Test
	public void testHttpGet() {
		String response = WalletUtils.httpGet("http://www.google.com:80", null);
		
		Assert.assertNotNull(response);
		
	}

}
