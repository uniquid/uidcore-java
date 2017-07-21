package com.uniquid.node.impl;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.node.impl.contract.ImprintingContract;
import com.uniquid.node.impl.params.UniquidRegTest;
import com.uniquid.node.impl.utils.DummyProviderRegister;
import com.uniquid.node.impl.utils.DummyUserRegister;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.transaction.TransactionManager;
import com.uniquid.register.user.UserRegister;

public class ImprintingContractTest {
	
	@Test
	public void testimprint() throws Exception {
		
		String imprinttx = "01000000015b6a47b5af13409ee10d5746c9495b5ed670f5747624fde28a1463eb5a0b8c6b000000006b483045022100e03d95afe9af969f801e3b17a26d8673ae05f4f4ac06d99dabafd1705af32b8f022062a5a0846661e497fce573dc23371e9e559378c3c818702db5dccbc4f501bceb012102bbb2c6c173d83f18ea0ed0ac38dfa8e450db063eb7dd1d19c3cd2d43313124fcfeffffff02fefc74e9f20000001976a914d9e70431b7efd65fb4e0892e026b004d051a729788ac00e1f505000000001976a91426a435bb27adcd02c08a1e4aaab971cb2afb058688ac2c0b0000";
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		final UserRegister dummyUser = new DummyUserRegister();
		
		RegisterFactory dummyFactory = new RegisterFactory() {
					
			@Override
			public UserRegister getUserRegister() throws RegisterException {
				return dummyUser;
			}
			
			@Override
			public ProviderRegister getProviderRegister() throws RegisterException {
				return dummyProvider;
			}
			
			@Override
			public TransactionManager getTransactionManager() throws RegisterException {
				return null;
			}
			
		};
		
		Transaction originalTransaction = UniquidRegTest.get().getDefaultSerializer().makeTransaction(Hex.decode(imprinttx));
		
		Address address = Address.fromBase58(UniquidRegTest.get(), "mj3Ggr43QMSea1s6H3nYJRE3m5GjhGFcLb");
		
		ImprintingContract contract = new ImprintingContract(UniquidRegTest.get(), null,
				null, dummyFactory, new UniquidNodeEventService(), null, address);
		
		contract.doRealContract(originalTransaction);
		
		Assert.assertEquals(1, dummyProvider.getAllChannels().size());
		
	}
	
}

