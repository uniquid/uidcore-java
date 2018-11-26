/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.node.impl.state;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;

/**
 * Implementation of State Design pattern: methods that are influenced by a particular state are defined in this
 * interface.
 */
public interface UniquidNodeState {

    /**
     * Allow to implement logic to send coins to blockchain
     * @param wallet
     * @param tx
     */
    public void onCoinsSent(final Wallet wallet, final Transaction tx);

    /**
     * Allow to implement logic to receive from from blockchain
     * @param wallet
     * @param tx
     */
    public void onCoinsReceived(final Wallet wallet, final Transaction tx);

    /**
     * Returns the {@link com.uniquid.node.UniquidNodeState} related to this state.
     * @return
     */
    public com.uniquid.node.UniquidNodeState getNodeState();

}