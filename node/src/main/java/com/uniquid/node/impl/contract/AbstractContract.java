/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.node.impl.contract;

import com.uniquid.node.impl.UniquidNodeConfiguration;
import com.uniquid.node.impl.UniquidNodeStateContext;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of State pattern with some boilerplate code for
 * transactions callback
 */
public abstract class AbstractContract<T extends UniquidNodeConfiguration> implements ContractStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContract.class);

    protected UniquidNodeStateContext<T> uniquidNodeStateContext;

    public AbstractContract(UniquidNodeStateContext<T> uniquidNodeStateContext) {
        this.uniquidNodeStateContext = uniquidNodeStateContext;
    }

    @Override
    public void manageContractCreation(final Transaction tx) throws Exception {

        // Transaction already confirmed
        if (tx.getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)) {

            LOGGER.info("TX {} was included in a block. Checking for a contract", tx.getTxId().toString());

            doRealContract(tx);

            // DONE

        } else {

            final Listener listener = new Listener() {

                @Override
                public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {

                    try {

                        if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING)
                                && reason.equals(ChangeReason.TYPE)) {

                            LOGGER.info("TX {} was included in a block. Checking for a contract", tx.getTxId().toString());

                            doRealContract(tx);

                            tx.getConfidence().removeEventListener(this);

                            LOGGER.info("Contract Done!");

                        } else if (confidence.getConfidenceType().equals(TransactionConfidence.ConfidenceType.DEAD)
                                && reason.equals(ChangeReason.TYPE)) {

                            LOGGER.warn("Something bad happened! TX {} is DEAD!", tx.getTxId().toString());

                            tx.getConfidence().removeEventListener(this);

                        }

                    } catch (Exception ex) {

                        LOGGER.error("Exception while populating Register", ex);

                    }

                }

            };

            // Transaction not yet confirmed! Register callback!
            tx.getConfidence().addEventListener(listener);

        }
    }

    @Override
    public void manageContractRevocation(final Transaction tx) throws Exception {

        revokeRealContract(tx);

    }

    /**
     * Delegate to subclass the real contract creation
     *
     * @param tx transaction
     * @throws Exception in case a problem occurs
     */
    public abstract void doRealContract(final Transaction tx) throws Exception;

    /**
     * Delegate to subclass the real contract revocation
     *
     * @param tx transaction
     * @throws Exception in case a problem occurs
     */
    public abstract void revokeRealContract(final Transaction tx) throws Exception;

}
