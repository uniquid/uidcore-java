/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.node.impl.contract;

import org.bitcoinj.core.Transaction;

/**
 * Implementation of Strategy Design Pattern
 */
public interface ContractStrategy {

    /**
     * Defines the creation of a contract
     *
     * @param tx the {@link Transaction} to manage
     * @throws Exception in case a problem occurs
     */
    void manageContractCreation(final Transaction tx) throws Exception;

    /**
     * Defines the revocation of a contract
     *
     * @param tx the {@link Transaction} to manage
     * @throws Exception in case a problem occurs
     */
    void manageContractRevocation(final Transaction tx) throws Exception;

}
