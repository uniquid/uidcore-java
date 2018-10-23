package com.uniquid.node.impl.utils;

import com.uniquid.params.UniquidRegTest;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.transaction.TransactionManager;
import com.uniquid.register.user.UserRegister;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class UniquidNodeStateUtilsTest {

    @Test
    public void testIsValidImprintingTransaction() {

        // from tx 0e9b113b468c07d946c5395cfd18ad7fbd66cf9007a1df0b9343ee77af02dc89
        String imprinting = "0100000001f9b665bb7e333ae1a36ea3a0b54478f5f13cf4779a5c5e0ff2acbb4d8dbf1db5010000006a47304402202b28f031a63a3e5107a05bb09cf8a12be3a5d659b7bdaef2150f76f0837e39c7022015bfcde7475ceedf556061811b1bcc18290e9fff73195074142ecdc4ed15d8c3012103fd0f9cc115ebf8fa49d8915c0c0cee926d3a6a67754849da230639f0384d25c9ffffffff0240420f00000000001976a914856ef40e30d6f46afb403a46104dbcea53729e7088ace0c35b05000000001976a9149723c240599c36a9dd5a3d3473dbb0bd7875f56a88ac00000000";

        Transaction originalTransaction = UniquidRegTest.get().getDefaultSerializer().makeTransaction(Hex.decode(imprinting));

        NetworkParameters networkParameters = UniquidRegTest.get();

        Assert.assertNotNull(originalTransaction);

        Assert.assertTrue(UniquidNodeStateUtils.isValidImprintingTransaction(originalTransaction, networkParameters, LegacyAddress.fromBase58(networkParameters, "msgV5jvGCFWevX6UeqEmYXgAJTpZ1BLHh6")));

        Assert.assertFalse(UniquidNodeStateUtils.isValidImprintingTransaction(originalTransaction, networkParameters, LegacyAddress.fromBase58(networkParameters, "n1CiwrPuM6aYt2NQYnfbaqCQzsTm7WKPgX")));

    }

    @Test
    public void testIsValidIsValidRevokeContract() throws Exception {

        // from tx 0e9b113b468c07d946c5395cfd18ad7fbd66cf9007a1df0b9343ee77af02dc89
        String imprinting = "0100000001f9b665bb7e333ae1a36ea3a0b54478f5f13cf4779a5c5e0ff2acbb4d8dbf1db5010000006a47304402202b28f031a63a3e5107a05bb09cf8a12be3a5d659b7bdaef2150f76f0837e39c7022015bfcde7475ceedf556061811b1bcc18290e9fff73195074142ecdc4ed15d8c3012103fd0f9cc115ebf8fa49d8915c0c0cee926d3a6a67754849da230639f0384d25c9ffffffff0240420f00000000001976a914856ef40e30d6f46afb403a46104dbcea53729e7088ace0c35b05000000001976a9149723c240599c36a9dd5a3d3473dbb0bd7875f56a88ac00000000";

        Transaction originalTransaction = UniquidRegTest.get().getDefaultSerializer().makeTransaction(Hex.decode(imprinting));

        Assert.assertNotNull(originalTransaction);

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        String providerAddress = "providerAddress";
        String userAddress = "userAddress";
        String bitmask = "bitmask";
        String revokeAddr = "n3KQpPe5AGgdTDrRo3aLiy85aSDSPethpu";

        ProviderChannel providerChannel = new ProviderChannel(providerAddress, userAddress, bitmask);
        providerChannel.setRevokeAddress(revokeAddr);

        dummyProvider.insertChannel(providerChannel);

        RegisterFactory dummyRegister = new RegisterFactory() {

            @Override
            public UserRegister getUserRegister() throws RegisterException {
                return new DummyUserRegister();
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

        RegisterFactory dummyRegister2 = new RegisterFactory() {

            @Override
            public UserRegister getUserRegister() throws RegisterException {
                return new DummyUserRegister();
            }

            @Override
            public ProviderRegister getProviderRegister() throws RegisterException {
                return new DummyProviderRegister();
            }

            @Override
            public TransactionManager getTransactionManager() throws RegisterException {
                return new DummyTransactionManager();
            }

        };

        Assert.assertTrue(UniquidNodeStateUtils.isValidRevokeContract(originalTransaction, UniquidRegTest.get(), dummyRegister));

        Assert.assertFalse(UniquidNodeStateUtils.isValidRevokeContract(originalTransaction, UniquidRegTest.get(), dummyRegister2));

    }

}
