/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.register.impl.sql;

import java.io.File;

public class UniquidNodeDBUtils {

    public static SQLiteRegisterFactory initDB() throws Exception {

        Class.forName("org.sqlite.JDBC");

        String url = "jdbc:sqlite:" + File.createTempFile("node", ".db");

        return new SQLiteRegisterFactory(url);

    }

}
