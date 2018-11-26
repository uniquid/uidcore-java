/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.register.impl.sql;

import com.uniquid.register.user.UserRegister;
import com.uniquid.register.user.UserRegisterTest;
import org.junit.BeforeClass;

public class SQLiteUserRegisterTest extends UserRegisterTest {

    private static SQLiteRegisterFactory factory;

    @BeforeClass
    public static void createNewDatabase() throws Exception {

        factory = UniquidNodeDBUtils.initDB();

    }

    @Override
    protected UserRegister getUserRegister() throws Exception {
        return factory.getUserRegister();
    }

}
