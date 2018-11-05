/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.core.impl;

import com.uniquid.core.provider.impl.ApplicationContext;
import org.junit.Assert;
import org.junit.Test;

public class UniquidSimplifierTest {

    @Test
    public void testConstructor() {

        ApplicationContext applicationContext = new ApplicationContext();

        Assert.assertNotNull(applicationContext);

    }

    @Test
    public void testServerInfo() {

        ApplicationContext applicationContext = new ApplicationContext();

        Assert.assertEquals("Uniquid Library", applicationContext.getServerInfo());

    }

    @Test
    public void testAttributeNames() {

        ApplicationContext applicationContext = new ApplicationContext();

        Assert.assertNotNull(applicationContext.getAttributeNames());

    }

}
