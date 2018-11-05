/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.register;

import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;
import org.junit.Assert;
import org.junit.Test;

public abstract class RegisterFactoryTest {

    public abstract RegisterFactory getRegisterFactory();

    @Test
    public void testGetProviderRegister() throws Exception {

        ProviderRegister providerRegister = getRegisterFactory().getProviderRegister();

        Assert.assertNotNull(providerRegister);

    }

    @Test
    public void testGetUserRegister() throws Exception {

        UserRegister userRegister = getRegisterFactory().getUserRegister();

        Assert.assertNotNull(userRegister);

    }

}
