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
 * Dummy state to be used when new node is created.
 */
public class CreatedState implements UniquidNodeState {

    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx) {
        throw new IllegalStateException();
    }

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx) {
        throw new IllegalStateException();
    }

    @Override
    public com.uniquid.node.UniquidNodeState getNodeState() {
        return com.uniquid.node.UniquidNodeState.CREATED;
    }

}
