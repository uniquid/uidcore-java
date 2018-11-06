/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.register.impl.sql;

import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.provider.ProviderRegisterTest;
import org.junit.BeforeClass;

public class SQLiteProviderRegisterTest extends ProviderRegisterTest {

    private static SQLiteRegisterFactory factory;

    @BeforeClass
    public static void createNewDatabase() throws Exception {

        factory = UniquidNodeDBUtils.initDB();

    }

    @Override
    protected ProviderRegister getProviderRegister() throws Exception {
        return factory.getProviderRegister();
    }


}
