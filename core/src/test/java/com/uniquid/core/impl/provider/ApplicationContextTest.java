/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.core.impl.provider;

import com.uniquid.core.provider.impl.ApplicationContext;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationContextTest {

    @Test
    public void testConstructor() {

        ApplicationContext context = new ApplicationContext();

        Assert.assertNotNull(context);
        Assert.assertEquals("Uniquid Library", context.getServerInfo());
        Assert.assertNull(context.getAttribute(null));
        Assert.assertNotNull(context.getAttributeNames());

    }

    @Test
    public void testAttribute() {

        ApplicationContext context = new ApplicationContext();

        try {

            context.setAttribute(null, null);
            Assert.fail();

        } catch (IllegalArgumentException ex) {

            Assert.assertEquals("name attribute is null", ex.getMessage());

        }

        Object o = new Object();

        context.setAttribute("test", o);

        Assert.assertEquals(o, context.getAttribute("test"));

        context.setAttribute("test", null);

        Assert.assertNull(context.getAttribute("test"));

        context.setAttribute("test", o);

        Assert.assertEquals(o, context.getAttribute("test"));

        context.removeAttribute("test");

        Assert.assertNull(context.getAttribute("test"));

        context.setAttribute("test", o);

        Assert.assertEquals(o, context.getAttribute("test"));

        context.setAttributeReadOnly("test");

        Assert.assertEquals(o, context.getAttribute("test"));

        context.removeAttribute("test");

        Assert.assertEquals(o, context.getAttribute("test"));

    }

}
