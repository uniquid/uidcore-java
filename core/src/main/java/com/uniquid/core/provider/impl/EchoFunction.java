/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.core.provider.impl;

import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;

import java.io.IOException;

/**
 * {@link Function} designed to echo with the content received from the User 
 */
public class EchoFunction extends GenericFunction {

    @Override
    public void service(FunctionRequestMessage inputMessage, FunctionResponseMessage outputMessage, byte[] payload)
            throws FunctionException, IOException {

        outputMessage.setResult("UID_echo: " + inputMessage.getParameters());

    }

}
