/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.core.provider.impl;

import com.uniquid.core.Core;
import com.uniquid.core.provider.Function;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.node.UniquidNode;
import org.bitcoinj.core.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Function} designed to manage Contract signing from Orchestrator 
 */
public class ContractFunction extends GenericFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractFunction.class);

    @Override
    public void service(FunctionRequestMessage inputMessage, FunctionResponseMessage outputMessage, byte[] payload)
            throws FunctionException, IOException {

        String params = inputMessage.getParameters();
        String serializedTx;

        List<String> path = new ArrayList<>();

        LOGGER.trace("Received input {}", inputMessage);

        try {

            JSONObject jsonMessage = new JSONObject(params);

            serializedTx = jsonMessage.getString("tx");

            JSONArray paths = jsonMessage.getJSONArray("paths");

            for (int i = 0; i < paths.length(); i++) {

                path.add(paths.getString(i));

            }


        } catch (JSONException ex) {

            throw new FunctionException("Problem with input JSON", ex);

        }

        try {

            LOGGER.info("Signing on path {}", path);

            UniquidNode spvNode = (UniquidNode) getFunctionContext().getAttribute(Core.NODE_ATTRIBUTE);

            Transaction tx = spvNode.createTransaction(serializedTx);

            Transaction signedTx = spvNode.signTransaction(tx, path);

            serializedTx = Hex.toHexString(signedTx.bitcoinSerialize());

            LOGGER.info("Broadcasting TX");

            String txid = spvNode.broadCastTransaction(serializedTx);

            outputMessage.setResult("0 - " + txid);

        } catch (Exception ex) {

            outputMessage.setResult("-1 - " + ex.getMessage());

        }

    }

}
