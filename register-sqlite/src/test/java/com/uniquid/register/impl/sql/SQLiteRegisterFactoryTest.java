/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.register.impl.sql;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.RegisterFactoryTest;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

public class SQLiteRegisterFactoryTest extends RegisterFactoryTest {

    private static SQLiteRegisterFactory factory;

    @BeforeClass
    public static void createNewDatabase() throws Exception {

        factory = UniquidNodeDBUtils.initDB();

    }

    @AfterClass
    public static void testClose() throws Exception {

        factory.close();

        try {
            ProviderRegister providerRegister = factory.getProviderRegister();
        } catch (RegisterException ex) {
            Assert.assertEquals("Datasource is null", ex.getLocalizedMessage());
        }

        try {
            UserRegister userRegister = factory.getUserRegister();
        } catch (RegisterException ex) {
            Assert.assertEquals("Datasource is null", ex.getLocalizedMessage());
        }

        try {
            factory.close();
        } catch (RegisterException ex) {
            Assert.assertEquals("Exception while closing dataSource", ex.getLocalizedMessage());
        }

    }

    @Override
    public RegisterFactory getRegisterFactory() {
        return factory;
    }

}
