/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.core;

import com.uniquid.core.impl.test.DummyNode;
import com.uniquid.core.impl.test.DummyProviderRegister;
import com.uniquid.core.impl.test.DummyUserRegister;
import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.core.provider.impl.EchoFunction;
import com.uniquid.core.provider.impl.GenericFunction;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.node.UniquidNode;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.transaction.TransactionManager;
import com.uniquid.register.user.UserRegister;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.Arrays;

public class CoreTest {

    @Test
    public void testContructor() throws Exception {

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

        final UniquidNode node = new DummyNode();

        Core core = new Core(dummyFactory, node) {

            @Override
            protected Function getFunction(FunctionRequestMessage inputMessage) {
                return null;
            }
        };

        Assert.assertNotNull(core);
        Assert.assertEquals(dummyFactory, core.getRegisterFactory());
        Assert.assertEquals(node, core.getNode());
        Assert.assertNotNull(core.getFunctionContext());
        Assert.assertNull(core.getFunction(null));

    }

    @Test
    public void testPerformProviderRequestFunctionOk() throws Exception {

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", "bitmask");

        dummyProvider.insertChannel(providerChannel);

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

        final UniquidNode node = new DummyNode();

        Core core = new Core(dummyFactory, node) {

            @Override
            protected Function getFunction(FunctionRequestMessage inputMessage) {
                return new EchoFunction();
            }
        };

        /*final FunctionRequestMessage providerRequest = new FunctionRequestMessage();
        providerRequest.setSender("userAddress");
        providerRequest.setMethod(30);
        providerRequest.setParameters("params");

        FunctionResponseMessage providerResponse = core.performProviderRequest(providerRequest, null);

        Assert.assertEquals(FunctionResponseMessage.RESULT_OK, providerResponse.getError());
        Assert.assertEquals(providerChannel.getProviderAddress(), providerResponse.getProvider());*/

    }

    @Test
    public void testPerformProviderRequestFunctionException() throws Exception {

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", "bitmask");

        dummyProvider.insertChannel(providerChannel);

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

        final UniquidNode node = new DummyNode();

        Core core = new Core(dummyFactory, node) {

            @Override
            protected Function getFunction(FunctionRequestMessage inputMessage) {
                return new GenericFunction() {

                    @Override
                    public void service(FunctionRequestMessage inputMessage, FunctionResponseMessage outputMessage, byte[] payload)
                            throws FunctionException, IOException {
                        throw new FunctionException("Error!");

                    }
                };
            }
        };

        /*final FunctionRequestMessage providerRequest = new FunctionRequestMessage();
        providerRequest.setUser("userAddress");
        providerRequest.setMethod(30);
        providerRequest.setParameters("params");

        FunctionResponseMessage providerResponse = core.performProviderRequest(providerRequest, null);

        Assert.assertEquals(FunctionResponseMessage.RESULT_ERROR, providerResponse.getError());
        Assert.assertEquals(providerChannel.getProviderAddress(), providerResponse.getProvider());*/

    }

    @Test
    public void testPerformProviderRequestNoFunction() throws Exception {

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", "bitmask");

        dummyProvider.insertChannel(providerChannel);

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

        final UniquidNode node = new DummyNode();

        Core core = new Core(dummyFactory, node) {

            @Override
            protected Function getFunction(FunctionRequestMessage inputMessage) {
                return null;
            }
        };

        /*final FunctionRequestMessage providerRequest = new FunctionRequestMessage();
        providerRequest.setUser("userAddress");
        providerRequest.setMethod(30);
        providerRequest.setParameters("params");

        FunctionResponseMessage providerResponse = core.performProviderRequest(providerRequest, null);

        Assert.assertEquals(FunctionResponseMessage.RESULT_FUNCTION_NOT_AVAILABLE, providerResponse.getError());
        Assert.assertEquals(providerChannel.getProviderAddress(), providerResponse.getProvider());*/

    }

    @Test
    public void testCheckSenderNoContract() throws Exception {

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

        final UniquidNode node = new DummyNode();

        Core core = new Core(dummyFactory, node) {

            @Override
            protected Function getFunction(FunctionRequestMessage inputMessage) {
                return null;
            }
        };

        /*final FunctionRequestMessage providerRequest = new FunctionRequestMessage();
        providerRequest.setUser("userAddress");
        providerRequest.setMethod(30);
        providerRequest.setParameters("params");

        try {

            ProviderChannel providerChannel = core.getProvider(providerRequest);
            core.getBitmask(providerChannel, providerRequest.getMethod());
            Assert.fail();

        } catch (Exception ex) {

            Assert.assertEquals("Sender not found in Provider register!", ex.getMessage());

        }*/

    }

    @Test
    public void testCheckSenderNotAuthorized() throws Exception {

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        byte[] b = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        String bitmaskToString = new String(Hex.encode(b));

        ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", bitmaskToString);
        providerChannel.setUntil(System.currentTimeMillis() + 600000);

        dummyProvider.insertChannel(providerChannel);

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

        final UniquidNode node = new DummyNode();

        Core core = new Core(dummyFactory, node) {

            @Override
            protected Function getFunction(FunctionRequestMessage inputMessage) {
                return null;
            }
        };

        /*final FunctionRequestMessage providerRequest = new FunctionRequestMessage();
        providerRequest.setUser("userAddress");
        providerRequest.setMethod(30);
        providerRequest.setParameters("params");

        try {

            ProviderChannel channel = core.getProvider(providerRequest);
            core.getBitmask(channel, providerRequest.getMethod());
            Assert.fail();

        } catch (Exception ex) {

            Assert.assertEquals("Sender not authorized!", ex.getMessage());

        }*/

    }

    @Test
    public void testCheckSenderNotAuthorized1() throws Exception {

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        byte[] b = {1,30,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        String bitmaskToString = new String(Hex.encode(b));

        ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", bitmaskToString);
        providerChannel.setUntil(System.currentTimeMillis() + 600000);

        dummyProvider.insertChannel(providerChannel);

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

        final UniquidNode node = new DummyNode();

        Core core = new Core(dummyFactory, node) {

            @Override
            protected Function getFunction(FunctionRequestMessage inputMessage) {
                return null;
            }
        };

        /*final FunctionRequestMessage providerRequest = new FunctionRequestMessage();
        providerRequest.setUser("userAddress");
        providerRequest.setMethod(31);
        providerRequest.setParameters("params");

        try {

            ProviderChannel channel = core.getProvider(providerRequest);
            core.getBitmask(channel, providerRequest.getMethod());
            Assert.fail();

        } catch (Exception ex) {

            Assert.assertEquals("Sender not authorized!", ex.getMessage());

        }*/

    }

    @Test
    public void testCheckSenderAuthorized() throws Exception {

        byte[] b2 = {0, 0, 0, 0, 64};

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        String bitmaskToString = new String(Hex.encode(b2));

        ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", bitmaskToString);
        providerChannel.setUntil(System.currentTimeMillis() + 600000);

        dummyProvider.insertChannel(providerChannel);

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

        final UniquidNode node = new DummyNode();

        Core core = new Core(dummyFactory, node) {

            @Override
            protected Function getFunction(FunctionRequestMessage inputMessage) {
                return null;
            }
        };

        /*final FunctionRequestMessage providerRequest = new FunctionRequestMessage();
        providerRequest.setUser("userAddress");
        providerRequest.setMethod(30);
        providerRequest.setParameters("params");

        ProviderChannel channel = core.getProvider(providerRequest);
        Assert.assertNotNull(core.getBitmask(channel, providerRequest.getMethod()));
        Assert.assertTrue(Arrays.equals(b2, core.getBitmask(channel, providerRequest.getMethod())));
*/
    }

    @Test
    public void testCheckSenderAuthorized1() throws Exception {

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        byte[] b = {1,30,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        String bitmaskToString = new String(Hex.encode(b));

        ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", bitmaskToString);
        providerChannel.setUntil(System.currentTimeMillis() + 600000);

        dummyProvider.insertChannel(providerChannel);

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

        final UniquidNode node = new DummyNode();

        Core core = new Core(dummyFactory, node) {

            @Override
            protected Function getFunction(FunctionRequestMessage inputMessage) {
                return null;
            }
        };

        /*final FunctionRequestMessage providerRequest = new FunctionRequestMessage();
        providerRequest.setUser("userAddress");
        providerRequest.setMethod(30);
        providerRequest.setParameters("params");

        try {

            ProviderChannel channel = core.getProvider(providerRequest);
            Assert.assertTrue(Arrays.equals(b, core.getBitmask(channel, providerRequest.getMethod())));

        } catch (Exception ex) {

            Assert.fail();

        }*/

    }

    @Test
    public void testCheckSenderInvalidContractVersion() throws Exception {

        byte[] b2 = {2, 0, 0, 0, 64};

        final ProviderRegister dummyProvider = new DummyProviderRegister();

        String bitmaskToString = new String(Hex.encode(b2));

        ProviderChannel providerChannel = new ProviderChannel("providerAddress", "userAddress", bitmaskToString);
        providerChannel.setUntil(System.currentTimeMillis() + 600000);

        dummyProvider.insertChannel(providerChannel);

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

        final UniquidNode node = new DummyNode();

        Core core = new Core(dummyFactory, node) {

            @Override
            protected Function getFunction(FunctionRequestMessage inputMessage) {
                return null;
            }
        };

        /*final FunctionRequestMessage providerRequest = new FunctionRequestMessage();
        providerRequest.setUser("userAddress");
        providerRequest.setMethod(30);
        providerRequest.setParameters("params");

        try {

            ProviderChannel channel = core.getProvider(providerRequest);
            core.getBitmask(channel, providerRequest.getMethod());
            Assert.fail();

        } catch (Exception ex) {

            Assert.assertEquals("Invalid contract version!", ex.getMessage());

        }*/

    }

}
