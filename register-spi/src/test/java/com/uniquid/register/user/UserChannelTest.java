/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.register.user;

import org.junit.Assert;
import org.junit.Test;

public class UserChannelTest {

    @Test
    public void testEmptyConstructor() {

        UserChannel userChannel = new UserChannel();

        Assert.assertNull(userChannel.getProviderName());
        Assert.assertNull(userChannel.getProviderAddress());
        Assert.assertNull(userChannel.getUserAddress());
        Assert.assertNull(userChannel.getBitmask());
        Assert.assertNull(userChannel.getRevokeAddress());
        Assert.assertNull(userChannel.getRevokeTxId());
        Assert.assertEquals(0, userChannel.getSince());
        Assert.assertEquals(0, userChannel.getUntil());

    }

    @Test
    public void testConstructor() {

        String providerName = "providerName";
        String providerAddress = "providerAddress";
        String userAddress = "userAddress";
        String bitmask = "bitmask";

        UserChannel userChannel = new UserChannel(providerName, providerAddress, userAddress, bitmask);

        Assert.assertEquals(providerName, userChannel.getProviderName());
        Assert.assertEquals(providerAddress, userChannel.getProviderAddress());
        Assert.assertEquals(userAddress, userChannel.getUserAddress());
        Assert.assertEquals(bitmask, userChannel.getBitmask());
        Assert.assertNull(userChannel.getRevokeAddress());
        Assert.assertNull(userChannel.getRevokeTxId());
        Assert.assertEquals(0, userChannel.getSince());
        Assert.assertEquals(0, userChannel.getUntil());

        Assert.assertEquals("provider address: providerAddress; user address: userAddress; bitmask: bitmask; revoke address: null; revokeTxId: null; since: 0; until: 0; path: null", userChannel.toString());
        Assert.assertEquals(665269288, userChannel.hashCode());

    }

    @Test
    public void testProviderName() {

        UserChannel userChannel = new UserChannel();

        Assert.assertNull(userChannel.getProviderName());

        String providerName = "providerName";

        userChannel.setProviderName(providerName);

        Assert.assertEquals(providerName, userChannel.getProviderName());

    }

    @Test
    public void testProviderAddress() {

        UserChannel userChannel = new UserChannel();

        Assert.assertNull(userChannel.getProviderAddress());

        String providerAddress = "providerAddress";

        userChannel.setProviderAddress(providerAddress);

        Assert.assertEquals(providerAddress, userChannel.getProviderAddress());

    }

    @Test
    public void testUserAddress() {

        UserChannel userChannel = new UserChannel();

        Assert.assertNull(userChannel.getUserAddress());

        String userAddress = "userAddress";

        userChannel.setUserAddress(userAddress);

        Assert.assertEquals(userAddress, userChannel.getUserAddress());

    }

    @Test
    public void testBitmask() {

        UserChannel userChannel = new UserChannel();

        Assert.assertNull(userChannel.getBitmask());

        String bitmask = "bitmask";

        userChannel.setBitmask(bitmask);

        Assert.assertEquals(bitmask, userChannel.getBitmask());

    }

    @Test
    public void testRevokeAddress() {

        UserChannel userChannel = new UserChannel();

        Assert.assertNull(userChannel.getRevokeAddress());

        String revokeAddress = "revokeAddress";

        userChannel.setRevokeAddress(revokeAddress);

        Assert.assertEquals(revokeAddress, userChannel.getRevokeAddress());

    }

    @Test
    public void testRevokeTxId() {

        UserChannel userChannel = new UserChannel();

        Assert.assertNull(userChannel.getRevokeTxId());

        String revokeTxid = "revokeTxid";

        userChannel.setRevokeTxId(revokeTxid);

        Assert.assertEquals(revokeTxid, userChannel.getRevokeTxId());

    }

    @Test
    public void testEquals() {

        UserChannel userChannel1 = new UserChannel();

        UserChannel userChannel2 = new UserChannel();

        Assert.assertEquals(userChannel1, userChannel2);

        userChannel2.setProviderName("other");

        Assert.assertNotEquals(userChannel1, userChannel2);

        Assert.assertNotNull(userChannel1);

        Assert.assertEquals(userChannel1, userChannel1);

    }

    @Test
    public void testCompareTo() {

        UserChannel userChannel1 = new UserChannel();

        UserChannel userChannel2 = new UserChannel();

        Assert.assertEquals(0, userChannel1.compareTo(userChannel1));

        Assert.assertEquals(0, userChannel1.compareTo(userChannel2));

        UserChannel userChannel3 = new UserChannel("address0", "provider", "user", "bitmask");

        UserChannel userChannel4 = new UserChannel("address0", "provider", "user", "bitmask");

        UserChannel userChannel5 = new UserChannel("address1", "provider", "user", "bitmask");

        Assert.assertEquals(0, userChannel3.compareTo(userChannel3));

        Assert.assertEquals(0, userChannel3.compareTo(userChannel4));

        Assert.assertEquals(0, userChannel4.compareTo(userChannel3));

        Assert.assertEquals(1, userChannel3.compareTo(userChannel1));

        Assert.assertEquals(-1, userChannel1.compareTo(userChannel3));

        Assert.assertEquals(-1, userChannel4.compareTo(userChannel5));

        Assert.assertEquals(1, userChannel5.compareTo(userChannel4));

    }

}
