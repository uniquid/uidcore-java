/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.register.transaction;

public interface TransactionManager {

    void startTransaction() throws TransactionException;

    void commitTransaction() throws TransactionException;

    void rollbackTransaction() throws TransactionException;

    boolean insideTransaction();

}
