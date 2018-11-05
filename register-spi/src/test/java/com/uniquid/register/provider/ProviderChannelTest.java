/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.register.provider;

import org.junit.Assert;
import org.junit.Test;

public class ProviderChannelTest {

    @Test
    public void testEmptyConstructor() {

        ProviderChannel providerChannel = new ProviderChannel();

        Assert.assertNull(providerChannel.getProviderAddress());
        Assert.assertNull(providerChannel.getUserAddress());
        Assert.assertNull(providerChannel.getBitmask());
        Assert.assertNull(providerChannel.getRevokeAddress());
        Assert.assertNull(providerChannel.getRevokeTxId());

    }

    @Test
    public void testContructor() {

        String providerAddress = "providerAddress";
        String userAddress = "userAddress";
        String bitmask = "bitmask";

        ProviderChannel providerChannel = new ProviderChannel(providerAddress, userAddress, bitmask);

        Assert.assertEquals(providerAddress, providerChannel.getProviderAddress());
        Assert.assertEquals(userAddress, providerChannel.getUserAddress());
        Assert.assertEquals(bitmask, providerChannel.getBitmask());
        Assert.assertNull(providerChannel.getRevokeAddress());
        Assert.assertNull(providerChannel.getRevokeTxId());

        Assert.assertEquals("Provider address providerAddress; user address userAddress; bitmask bitmask; revoke address null; revokeTxId null; creation time 0; since 0; until 0; path null", providerChannel.toString());

        Assert.assertEquals(-347432128, providerChannel.hashCode());
    }

    @Test
    public void testProviderAddress() {

        ProviderChannel providerChannel = new ProviderChannel();

        Assert.assertNull(providerChannel.getProviderAddress());

        String providerAddress = "providerAddress";

        providerChannel.setProviderAddress(providerAddress);

        Assert.assertEquals(providerAddress, providerChannel.getProviderAddress());

    }

    @Test
    public void testUserAddress() {

        ProviderChannel providerChannel = new ProviderChannel();

        Assert.assertNull(providerChannel.getUserAddress());

        String userAddress = "userAddress";

        providerChannel.setUserAddress(userAddress);

        Assert.assertEquals(userAddress, providerChannel.getUserAddress());

    }

    @Test
    public void testBitmask() {

        ProviderChannel providerChannel = new ProviderChannel();

        Assert.assertNull(providerChannel.getBitmask());

        String bitmask = "bitmask";

        providerChannel.setBitmask(bitmask);

        Assert.assertEquals(bitmask, providerChannel.getBitmask());

    }

    @Test
    public void testRevokeAddress() {

        ProviderChannel providerChannel = new ProviderChannel();

        Assert.assertNull(providerChannel.getRevokeAddress());

        String revokeAddress = "revokeAddress";

        providerChannel.setRevokeAddress(revokeAddress);

        Assert.assertEquals(revokeAddress, providerChannel.getRevokeAddress());

    }

    @Test
    public void testRevokeTxId() {

        ProviderChannel providerChannel = new ProviderChannel();

        Assert.assertNull(providerChannel.getRevokeTxId());

        String revokeTxid = "revokeTxid";

        providerChannel.setRevokeTxId(revokeTxid);

        Assert.assertEquals(revokeTxid, providerChannel.getRevokeTxId());

    }

    @Test
    public void testCreationTime() {

        ProviderChannel providerChannel = new ProviderChannel();

        long creationTime = System.currentTimeMillis();

        Assert.assertEquals(0, providerChannel.getCreationTime());

        providerChannel.setCreationTime(creationTime);

        Assert.assertEquals(creationTime, providerChannel.getCreationTime());

    }

    @Test
    public void testEquals() {

        ProviderChannel providerChannel1 = new ProviderChannel();

        ProviderChannel providerChannel2 = new ProviderChannel();

        Assert.assertEquals(providerChannel1, providerChannel1);

        Assert.assertEquals(providerChannel1, providerChannel2);

        providerChannel2.setUserAddress("123");

        Assert.assertNotEquals(providerChannel1, providerChannel2);

        Assert.assertNotEquals(null, providerChannel1);

    }

}
