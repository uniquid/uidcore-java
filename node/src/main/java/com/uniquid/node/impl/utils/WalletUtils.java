/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.node.impl.utils;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class that contains useful methods for managing wallets
 */
public abstract class WalletUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletUtils.class.getName());

    private static final int OP_RETURN_INDEX = 1;

    /**
     * Check if a Transaction has a valid (Uniquid) OP_RETURN
     */
    public static boolean isValidOpReturn(Transaction tx) {

        byte[] opreturn = getOpReturnAsByteArray(tx);

        return (opreturn != null && opreturn.length == 80);
    }

    /**
     * Retrieve OP_RETURN from a Transaction as Hex String
     */
    public static String getOpReturn(Transaction tx) {

        byte[] opreturn = getOpReturnAsByteArray(tx);

        if (opreturn != null) {

            return org.bitcoinj.core.Utils.HEX.encode(opreturn);

        }

        return null;

    }

    /**
     * Retrieve OP_RETURN from a Transaction as byte array
     * @param tx the transaction
     * @return the OP_RETURN
     */
    public static byte[] getOpReturnAsByteArray(Transaction tx) {

        List<TransactionOutput> to = tx.getOutputs();

        if (to.size() == 4) {

            Script script = to.get(OP_RETURN_INDEX).getScriptPubKey();

            if (ScriptPattern.isOpReturn(script)) {

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                try {

                    script.getChunks().get(1).write(byteArrayOutputStream);

                } catch (IOException ex) {

                    LOGGER.error("Exception while writing to ByteArrayOutputStream", ex);

                    return null;

                }

                // FIRST BYTE IS OPCODE
                // SECOND BYTE IS TOTAL LENGHT OF OPRETURN
                // THIRD BYTE IS FIRST ELEMENT OP OPRETURN

                byte[] opreturn = byteArrayOutputStream.toByteArray();

                return Arrays.copyOfRange(opreturn, 2, opreturn[1] + 2);

            }

            return null;

        } else {

            return null;

        }

    }

}
