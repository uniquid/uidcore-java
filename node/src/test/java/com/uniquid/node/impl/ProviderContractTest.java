package com.uniquid.node.impl;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import com.uniquid.node.impl.contract.ProviderContract;
import com.uniquid.node.impl.params.UniquidRegTest;
import com.uniquid.node.impl.state.UniquidNodeState;
import com.uniquid.node.impl.utils.DummyProviderRegister;
import com.uniquid.node.impl.utils.DummyTransactionManager;
import com.uniquid.node.impl.utils.DummyUserRegister;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.transaction.TransactionManager;
import com.uniquid.register.user.UserRegister;

public class ProviderContractTest {
	
	@Test
	public void testprovider() throws Exception {
		
		String providerTx = "0100000001ceea58a6abe24b758091af9b20f11298fcecac97a1ae00bd0864f3d77f2e346a030000006b48304502210085dac060f7e49985900deac71723d2447ea039c7ddaae0002d7b04e6c9f53c9f02203dd0c56e2c6c933fe8d285625a609f1e5231b5fa9c0900b1c01315604bc90973012102177443482b37347f589c248fd171c788e5a23e9719537235d35cf5411322aaa4ffffffff04e87a0100000000001976a9143dd956abd892d5e88583357aaa516fd63c3f858288ac0000000000000000536a4c500000000000000200004000000800400000004000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000a0860100000000001976a91446b6a63b00eb2acf86735e846c5e79abb8582f6388ac483b0900000000001976a914901d5e68ab54d5a5b0ca734fa29efe366401850088ac00000000";
		
		final ProviderRegister dummyProvider = new DummyProviderRegister();
		
		final UserRegister dummyUser = new DummyUserRegister();
		
		final RegisterFactory dummyFactory = new RegisterFactory() {
					
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
				return new DummyTransactionManager();
			}
			
		};
		
		Transaction originalTransaction = UniquidRegTest.get().getDefaultSerializer().makeTransaction(Hex.decode(providerTx));
		
		final Context c = Context.getOrCreate(UniquidRegTest.get());
		
		
		ProviderContract contract = new ProviderContract(new UniquidNodeStateContext() {
			
			@Override
			public void setUniquidNodeState(UniquidNodeState nodeState) {
				
			}
			
			@Override
			public Wallet getUserWallet() {
				return null;
			}
			
			@Override
			public UniquidNodeEventService getUniquidNodeEventService() {
				return new UniquidNodeEventService();
			}
			
			@Override
			public String getPublicKey() {
				return null;
			}
			
			@Override
			public Wallet getProviderWallet() {
				return new DummyWallet(c);
			}
			
			@Override
			public Address getImprintingAddress() {
				return null;
			}
			
			@Override
			public UniquidNodeConfiguration getUniquidNodeConfiguration() {
				return new UniquidNodeConfiguration() {
					
					@Override
					public RegisterFactory getRegisterFactory() {
						return dummyFactory;
					}
					
					@Override
					public NetworkParameters getNetworkParameters() {
						return UniquidRegTest.get();
					}
					
				};
			}
		});
		
		contract.doRealContract(originalTransaction);
		
		Assert.assertEquals(1, dummyProvider.getAllChannels().size());
		
	}

}

class DummyWallet extends Wallet {

	public DummyWallet(Context context) {
		super(context);
	}
	
	@Override
    public boolean isPubKeyHashMine(byte[] pubkeyHash) {
        return true;
    }
	
}
