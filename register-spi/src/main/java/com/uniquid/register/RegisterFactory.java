/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.register;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.transaction.TransactionManager;
import com.uniquid.register.user.UserRegister;

/**
 * Implementation of Factory Design Pattern for DAO (Data Access Object).
 *
 * Is used to delegate to an implementor class the creation of DAO concrete objects that
 * manage data toward a data source.
 */
public interface RegisterFactory {

    /**
     * Returns a ProviderRegister instance
     *
     * @return a ProviderRegister instance
     * @throws RegisterException in case of problem occurs
     */
    ProviderRegister getProviderRegister() throws RegisterException;

    /**
     * Returns a UserRegister instance
     *
     * @return a UserRegister instance
     * @throws RegisterException in case of problem occurs
     */
    UserRegister getUserRegister() throws RegisterException;

    /**
     * Return a transaction manager
     * @return the {@link TransactionManager}
     * @throws RegisterException in case a problem occurs
     */
    TransactionManager getTransactionManager() throws RegisterException;

}
