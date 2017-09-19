package com.uniquid.node.impl.utils;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that contains useful methods for managing wallets
 */
public abstract class WalletUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(WalletUtils.class.getName());

	private static final int OP_RETURN_INDEX = 1;

	/**
	 * Check if a Transaction has a valid (Uniquid) OP_RETURN
	 */
	public static boolean isValidOpReturn(Transaction tx) throws Exception {

		byte[] opreturn = getOpReturnAsByteArray(tx);

		if (opreturn != null && opreturn.length == 80) {

			return true;

		} else {

			return false;

		}
	}

	/**
	 * Retrieve OP_RETURN from a Transaction as Hex String
	 */
	public static String getOpReturn(Transaction tx) throws Exception {

		byte[] opreturn = getOpReturnAsByteArray(tx);

		if (opreturn != null) {

			return org.bitcoinj.core.Utils.HEX.encode(opreturn);

		}

		return null;

	}

	/**
	 * Retrieve OP_RETURN from a Transaction as byte array
	 * @param tx
	 * @return
	 * @throws Exception
	 */
	public static byte[] getOpReturnAsByteArray(Transaction tx) throws Exception {

		List<TransactionOutput> to = tx.getOutputs();

		if (to.size() == 4) {

			Script script = to.get(OP_RETURN_INDEX).getScriptPubKey();

			if (script.isOpReturn()) {

				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

				script.getChunks().get(1).write(byteArrayOutputStream);

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
