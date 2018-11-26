package com.uniquid.node.impl;

import com.uniquid.node.UniquidCapability;
import com.uniquid.node.UniquidNodeState;
import com.uniquid.node.exception.NodeException;
import com.uniquid.node.impl.utils.DummyProviderRegister;
import com.uniquid.node.impl.utils.DummyRegisterFactory;
import com.uniquid.node.impl.utils.DummyTransactionManager;
import com.uniquid.node.impl.utils.DummyUserRegister;
import com.uniquid.params.UniquidRegTest;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class UniquidNodeImplTest {

    @Test
    public void testBuild() throws Exception {

        @SuppressWarnings("rawtypes")
        UniquidNodeImpl.UniquidNodeBuilder builder = new UniquidNodeImpl.UniquidNodeBuilder();

        NetworkParameters parameters = UniquidRegTest.get();
        File providerFile = File.createTempFile("provider", ".wallet");
        File userFile = File.createTempFile("user", ".wallet");
        File chainFile = File.createTempFile("chain", ".chain");
        File userChainFile = File.createTempFile("userchain", ".chain");

        RegisterFactory dummyRegister = new DummyRegisterFactory(null, null, new DummyTransactionManager());

        String machineName = "machineName";

        builder.setNetworkParameters(parameters);
        builder.setProviderFile(providerFile);
        builder.setUserFile(userFile);
        builder.setProviderChainFile(chainFile);
        builder.setUserChainFile(userChainFile);
        builder.setRegisterFactory(dummyRegister);
        builder.setNodeName(machineName);


        // corresponding tpriv is tprv8ZgxMBicQKsPeUjbnmwN54rKdA1UCsoJsY3ngzhVxyqeTV5pPNo77heffPbSfWVy8vLkTcMwpQHTxJzjz8euKsdDzETM5WKyKFYNLxMAcmQ
        UniquidNodeImpl uniquidNode = builder.buildFromHexSeed("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", 1487159470);

        Assert.assertNotNull(uniquidNode);

        Assert.assertEquals(UniquidNodeState.CREATED, uniquidNode.getNodeState());

        uniquidNode.getImprintingAddress();

        uniquidNode.getPublicKey();

        Assert.assertEquals(machineName, uniquidNode.getNodeName());

        Assert.assertEquals(1487159470, uniquidNode.getCreationTime());

        try {

            uniquidNode.getSpendableBalance();
            Assert.fail();

        } catch (Exception e) {
            // do nothing
        }

    }

    @Test
    public void testInitNode0Elements() throws Exception {

        @SuppressWarnings("rawtypes")
        UniquidNodeImpl.UniquidNodeBuilder builder = new UniquidNodeImpl.UniquidNodeBuilder();

        NetworkParameters parameters = UniquidRegTest.get();
        File providerFile = File.createTempFile("provider", ".wallet");
        providerFile.delete();
        File userFile = File.createTempFile("user", ".wallet");
        userFile.delete();
        File chainFile = File.createTempFile("chain", ".chain");
        chainFile.delete();
        File userChainFile = File.createTempFile("userchain", ".chain");
        userChainFile.delete();

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        final UserRegister dummyUser = new DummyUserRegister();

        RegisterFactory dummyFactory = new DummyRegisterFactory(dummyUser, dummyProvider, new DummyTransactionManager());

        String machineName = "machineName";

        builder.setNetworkParameters(parameters);
        builder.setProviderFile(providerFile);
        builder.setUserFile(userFile);
        builder.setProviderChainFile(chainFile);
        builder.setUserChainFile(userChainFile);
        builder.setRegisterFactory(dummyFactory);
        builder.setNodeName(machineName);

        UniquidNodeImpl uniquidNode = builder.buildFromHexSeed("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", 1487159470);

        Assert.assertNotNull(uniquidNode);

        uniquidNode.initNode();

        Assert.assertEquals(UniquidNodeState.IMPRINTING, uniquidNode.getNodeState());

        Assert.assertEquals("mj3Ggr43QMSea1s6H3nYJRE3m5GjhGFcLb", uniquidNode.getImprintingAddress());

        Assert.assertEquals("tpubDAjeiZPBWjUzphKoEJhkBdiqCSjkunkKQmUxGycsWBPH2aHqdAmJXoRhY6NfNAmRXLmQikRxZM1urcP4Rv7Fb3mcSFpozMstdqUtmSwjFJp", uniquidNode.getPublicKey());

        Assert.assertEquals("0.00 BTC", uniquidNode.getSpendableBalance());

        Assert.assertNotNull(uniquidNode.getProviderWallet());

        Assert.assertNotNull(uniquidNode.getUserWallet());

    }

    @Test
    public void testInitNode1Elements() throws Exception {

        @SuppressWarnings("rawtypes")
        UniquidNodeImpl.UniquidNodeBuilder builder = new UniquidNodeImpl.UniquidNodeBuilder();

        NetworkParameters parameters = UniquidRegTest.get();
        File providerFile = File.createTempFile("provider", ".wallet");
        providerFile.delete();
        File userFile = File.createTempFile("user", ".wallet");
        userFile.delete();
        File chainFile = File.createTempFile("chain", ".chain");
        chainFile.delete();
        File userChainFile = File.createTempFile("userchain", ".chain");
        userChainFile.delete();

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        String providerAddress = "providerAddress";
        String userAddress = "userAddress";
        String bitmask = "bitmask";

        ProviderChannel providerChannel = new ProviderChannel(providerAddress, userAddress, bitmask);

        Assert.assertEquals(providerAddress, providerChannel.getProviderAddress());
        Assert.assertEquals(userAddress, providerChannel.getUserAddress());
        Assert.assertEquals(bitmask, providerChannel.getBitmask());
        Assert.assertNull(providerChannel.getRevokeAddress());
        Assert.assertNull(providerChannel.getRevokeTxId());

        dummyProvider.insertChannel(providerChannel);

        final UserRegister dummyUser = new DummyUserRegister();

        RegisterFactory dummyFactory = new DummyRegisterFactory(dummyUser, dummyProvider, new DummyTransactionManager());

        String machineName = "machineName";

        builder.setNetworkParameters(parameters);
        builder.setProviderFile(providerFile);
        builder.setUserFile(userFile);
        builder.setProviderChainFile(chainFile);
        builder.setUserChainFile(userChainFile);
        builder.setRegisterFactory(dummyFactory);
        builder.setNodeName(machineName);

        UniquidNodeImpl uniquidNode = builder.buildFromHexSeed("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", 1487159470);

        Assert.assertNotNull(uniquidNode);

        uniquidNode.initNode();

        Assert.assertEquals(UniquidNodeState.READY, uniquidNode.getNodeState());

        Assert.assertEquals("mj3Ggr43QMSea1s6H3nYJRE3m5GjhGFcLb", uniquidNode.getImprintingAddress());

        Assert.assertEquals("tpubDAjeiZPBWjUzphKoEJhkBdiqCSjkunkKQmUxGycsWBPH2aHqdAmJXoRhY6NfNAmRXLmQikRxZM1urcP4Rv7Fb3mcSFpozMstdqUtmSwjFJp", uniquidNode.getPublicKey());

        Assert.assertEquals("0.00 BTC", uniquidNode.getSpendableBalance());

        Assert.assertNotNull(uniquidNode.getProviderWallet());

        Assert.assertNotNull(uniquidNode.getUserWallet());

    }

    @Test
    public void testUpdateNode() throws Exception {

        @SuppressWarnings("rawtypes")
        UniquidNodeImpl.UniquidNodeBuilder builder = new UniquidNodeImpl.UniquidNodeBuilder();

        NetworkParameters parameters = UniquidRegTest.get();
        File providerFile = File.createTempFile("provider", ".wallet");
        providerFile.delete();
        File userFile = File.createTempFile("user", ".wallet");
        userFile.delete();
        File chainFile = File.createTempFile("chain", ".chain");
        chainFile.delete();
        File userChainFile = File.createTempFile("userchain", ".chain");
        userChainFile.delete();

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        final UserRegister dummyUser = new DummyUserRegister();

        RegisterFactory dummyFactory = new DummyRegisterFactory(dummyUser, dummyProvider, new DummyTransactionManager());

        String machineName = "machineName";

        builder.setNetworkParameters(parameters);
        builder.setProviderFile(providerFile);
        builder.setUserFile(userFile);
        builder.setProviderChainFile(chainFile);
        builder.setUserChainFile(userChainFile);
        builder.setRegisterFactory(dummyFactory);
        builder.setNodeName(machineName);

        UniquidNodeImpl uniquidNode = builder.buildFromHexSeed("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", 1487159470);

        Assert.assertNotNull(uniquidNode);

        uniquidNode.initNode();

        Assert.assertEquals(UniquidNodeState.IMPRINTING, uniquidNode.getNodeState());

        Assert.assertEquals("mj3Ggr43QMSea1s6H3nYJRE3m5GjhGFcLb", uniquidNode.getImprintingAddress());

        Assert.assertEquals("tpubDAjeiZPBWjUzphKoEJhkBdiqCSjkunkKQmUxGycsWBPH2aHqdAmJXoRhY6NfNAmRXLmQikRxZM1urcP4Rv7Fb3mcSFpozMstdqUtmSwjFJp", uniquidNode.getPublicKey());

        Assert.assertEquals("0.00 BTC", uniquidNode.getSpendableBalance());

        Assert.assertNotNull(uniquidNode.getProviderWallet());

        Assert.assertNotNull(uniquidNode.getUserWallet());

        uniquidNode.updateNode();

        Thread.sleep(5000); // wait to update

        Assert.assertEquals(1, dummyProvider.getAllChannels().size());

        Assert.assertEquals(UniquidNodeState.READY, uniquidNode.getNodeState());

        Assert.assertEquals("7.00 BTC", uniquidNode.getSpendableBalance());

        UniquidNodeImpl uniquidNodeReloaded = builder.buildFromHexSeed("01b30b9f68e59936712f0c416ceb1c73f01fa97f665acfa898e6e3c19c5ab577", 1487159470);

        Assert.assertNotNull(uniquidNodeReloaded);

        uniquidNodeReloaded.initNode();

        Assert.assertEquals(1, dummyProvider.getAllChannels().size());

        Assert.assertEquals(UniquidNodeState.READY, uniquidNodeReloaded.getNodeState());

        Assert.assertEquals("7.00 BTC", uniquidNodeReloaded.getSpendableBalance());

        List<String> paths = new ArrayList<>();
        paths.add("0/0/0");
        paths.add("0/0/1");

        String unsigned_tx = "010000000247a327c7f5d626a7159c5c0fccf90732ba733ab6e9eea53db24c4829b3cc46a40000000000ffffffffced72f216e191ebc3be3b7b8c5d8fc0a7ac52fa934e395f837a28f96df2d8f900100000000ffffffff0140420f00000000001976a91457c9afb8bc5e4fa738f5b46afcb51b43a48b270988ac00000000";

        String signed_tx = "010000000247a327c7f5d626a7159c5c0fccf90732ba733ab6e9eea53db24c4829b3cc46a4000000006a473044022014fac39447707341f16cac6fcd9a7258dcc636767016e225c5bb2a2ed4462f4c02202867a07f0695109b47cd9de86d06393c9f3f1f0ebbde5f3f7914f5296edf1be4012102461fb3538ffec054fd4ee1e9087e7debf8442028f941bda308c24b508cbf69f7ffffffffced72f216e191ebc3be3b7b8c5d8fc0a7ac52fa934e395f837a28f96df2d8f90010000006a473044022061e3c20622dcbe8ea3a62c66ba56da91c4f1083b11bbd6e912df81bc92826ac50220631d302f309a1c5212933830f910ba2931ff32a5b41a2c9aaa808b926aa99363012102ece5ce70796b6893283aa0c8f30273c7dc0ff0b82a75017285387ecd2d767110ffffffff0140420f00000000001976a91457c9afb8bc5e4fa738f5b46afcb51b43a48b270988ac00000000";

        Transaction tx = uniquidNode.createTransaction(unsigned_tx);
        uniquidNode.signTransaction(tx, paths);
        Assert.assertEquals(signed_tx, Hex.toHexString(tx.bitcoinSerialize()));


        List<String> paths2 = new ArrayList<>();
        paths2.add("1/1/1");

        String unsigned_tx2 = "010000000144106042786d7b5f3bb24777ce51024e8de51a8845dfd05f59f04fcb78331f6a0100000000ffffffff010084d717000000001976a9141686d60b9db2bd5646b493ce17dd7bb0731cb50788ac00000000";

        String signed_tx2 = "010000000144106042786d7b5f3bb24777ce51024e8de51a8845dfd05f59f04fcb78331f6a010000006a47304402201f7f991baf62b800e49348ddd4de2f6d1cb589d6e1344a89f0e2bfdac852d6e602203cb054754a82029f75af8e445eb5c5ce8cd4a669dac5f29ae3cdb12c4d97ce13012102d42246dbf36fd5c73d318e3db60c172ddaa9debe7d7908ac1b36efa46d818b4bffffffff010084d717000000001976a9141686d60b9db2bd5646b493ce17dd7bb0731cb50788ac00000000";

        Transaction tx2 = uniquidNode.createTransaction(unsigned_tx2);
        uniquidNode.signTransaction(tx2, paths2);
        Assert.assertEquals(signed_tx2, Hex.toHexString(tx.bitcoinSerialize()));

        try {

            Transaction tx3 = uniquidNode.createTransaction(unsigned_tx);
            uniquidNode.signTransaction(tx3, paths2);
            Assert.fail();

        } catch (NodeException ex) {
            // NOTHING TO DO.
        }

        List<String> paths3 = new ArrayList<>();
        paths3.add("1/0/0");

        try {

            Transaction tx4 = uniquidNode.createTransaction(unsigned_tx);
            uniquidNode.signTransaction(tx4, paths3);
            Assert.fail();

        } catch (NodeException ex) {
            // NOTHING TO DO.
        }


        Assert.assertEquals("IOAhyp0at0puRgDZD3DJl0S2FjgLEo0q7nBdgzDrWpbDR+B3daIlN3R20lhcpQKZFWl8/ttxUXzQYS0EFso2VLo=", uniquidNode.signMessage("Hello World!", "0/0/0"));

        final ECKey key = ECKey.signedMessageToKey("Hello World!", "IOAhyp0at0puRgDZD3DJl0S2FjgLEo0q7nBdgzDrWpbDR+B3daIlN3R20lhcpQKZFWl8/ttxUXzQYS0EFso2VLo=");

        Assert.assertEquals("mj3Ggr43QMSea1s6H3nYJRE3m5GjhGFcLb", LegacyAddress.fromKey(UniquidRegTest.get(), key).toBase58());

        Assert.assertEquals("H3UHssQig0Vef9VIzUmDW0HV37vpm5ZZGF0zbw6xxMMoTTbUm/efPIQDcx5IlOgflC7BcR90aXHsV7BBaQx+b9Q=", uniquidNode.signMessage("Hello World!", "1/0/0"));

        final ECKey key2 = ECKey.signedMessageToKey("Hello World!", "H3UHssQig0Vef9VIzUmDW0HV37vpm5ZZGF0zbw6xxMMoTTbUm/efPIQDcx5IlOgflC7BcR90aXHsV7BBaQx+b9Q=");

        Assert.assertEquals("mgXg8FWaYaDVcsvjJq4jW7vrxQCRtjPchs", LegacyAddress.fromKey(UniquidRegTest.get(), key2).toBase58());

        Assert.assertEquals("mj3Ggr43QMSea1s6H3nYJRE3m5GjhGFcLb", uniquidNode.getAddressAtPath("0/0/0"));

        Assert.assertEquals("mgXg8FWaYaDVcsvjJq4jW7vrxQCRtjPchs", uniquidNode.getAddressAtPath("1/0/0"));

        // allow function 30
        byte[] opreturn = new byte[80];
        BitSet bitSet = new BitSet();
        bitSet.set(29);
        byte[] bitmask = bitSet.toByteArray();

        opreturn[0] = 0;

        // skip first byte!!!
        // This will create the opreturn of 80 bytes containing the orchestration bit set to 1
        System.arraycopy(bitmask, 0, opreturn, 1, bitmask.length);

        dummyFactory.getUserRegister().insertChannel(new UserChannel("test", "1234", "muwk2Z1HiysDAADXC5UMvpvmmCjuZdFnoP", Hex.toHexString(opreturn), "M/1/1/1"));

        byte[] rights =  new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00 };

        UniquidCapability uniquidCapability =  new UniquidCapability.UniquidCapabilityBuilder()
                .setResourceID("1234")
                .setAssigner("muwk2Z1HiysDAADXC5UMvpvmmCjuZdFnoP")
                .setAssignee("12345")
                .setRights(rights)
                .setSince(1234)
                .setUntil(12345)
                .build();

        try {

            byte[] wrongRights =  new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00 };

            new UniquidCapability.UniquidCapabilityBuilder()
                    .setResourceID("1234")
                    .setAssigner("muwk2Z1HiysDAADXC5UMvpvmmCjuZdFnoP")
                    .setAssignee("12345")
                    .setRights(wrongRights)
                    .setSince(1234)
                    .setUntil(12345)
                    .build();
            Assert.fail();
        } catch (Exception ex) {
            //Expected
        }

        Assert.assertEquals("muwk2Z1HiysDAADXC5UMvpvmmCjuZdFnoP12341234500000000000000000000000000000000000000123412345", uniquidCapability.prepareToSign());

        UniquidCapability capability = uniquidNode.createCapability("test", "12345", rights, 1234, 12345);

        Assert.assertEquals("IK9G0DS7d0Blh94YmcdaDJALan63ZNN1gL0SdVQ7fj94L9OkNmjs325Zx+jWSqYPxn4c61IhUXlY9FfwGnzwLQk=", capability.getAssignerSignature());

        ECKey signingKey = ECKey.signedMessageToKey(capability.prepareToSign(), capability.getAssignerSignature());

        LegacyAddress a = LegacyAddress.fromKey(UniquidRegTest.get(), signingKey);

        Assert.assertEquals(capability.getAssigner(), a.toBase58());

        try {
            uniquidNode.receiveProviderCapability(capability);
            Assert.fail();
        } catch (Exception ex) {
            // Expected
        }


//		byte[] rights2 =  new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0x00, (byte) 0x00,
//				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
//				(byte) 0x00, (byte) 0x00, (byte) 0x00 };
//		
//		UniquidCapability uniquidCapability2 = new UniquidCapability.UniquidCapabilityBuilder()
//			.setAssigner("muwk2Z1HiysDAADXC5UMvpvmmCjuZdFnoP")
//			.setResourceID("mp246b2KBN5xncctJxtj7UHiEo5GfiewMT")
//			.setAssignee("mvmmEz4nduzpLk4KR6JMQn3LyZuHYt6NTc")
//			.setRights(rights2)
//			.setSince(0xffffffffffffffffL)
//			.setUntil(0xffffffffffffffffL)
//			.build();
//		
//		Assert.assertEquals("muwk2Z1HiysDAADXC5UMvpvmmCjuZdFnoPmp246b2KBN5xncctJxtj7UHiEo5GfiewMTmvmmEz4nduzpLk4KR6JMQn3LyZuHYt6NTc0000fe000000000000000000000000000000001844674407370955161518446744073709551615", uniquidCapability2.prepareToSign());

        // Create fake ownership
        ProviderChannel providerChannel = new ProviderChannel("1234", "muwk2Z1HiysDAADXC5UMvpvmmCjuZdFnoP", Hex.toHexString(opreturn));

        dummyProvider.insertChannel(providerChannel);

        uniquidNode.receiveProviderCapability(capability);

        Assert.assertNotNull(dummyProvider.getChannelByUserAddress(capability.getAssignee()));

        try {
            List<String> invalid = new ArrayList<>();
            invalid.add("2/0/0");
            Transaction tx5 = uniquidNode.createTransaction(unsigned_tx);
            uniquidNode.signTransaction(tx5, invalid);
            Assert.fail();
        } catch (Exception ex) {
            // NOTHING TO DO
        }



    }

}
