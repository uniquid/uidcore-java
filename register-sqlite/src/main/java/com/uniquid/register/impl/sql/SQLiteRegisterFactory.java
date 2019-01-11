/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.register.impl.sql;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.transaction.TransactionManager;
import com.uniquid.register.user.UserRegister;
import org.flywaydb.core.Flyway;

/**
 * Concrete class implementation of {@code RegisterFactory} that uses SQLite as data store.
 */
public class SQLiteRegisterFactory implements RegisterFactory {

    protected TransactionAwareBasicDataSource dataSource;

    /**
     * Creates an instance from the connection string
     *
     * @param connectionString the database connection string
     * @throws RegisterException in case a problem occurs.
     */
    public SQLiteRegisterFactory(final String connectionString) throws RegisterException {

        if (connectionString == null) {
            throw new RegisterException("connectionString is null!");
        }

        dataSource = new TransactionAwareBasicDataSource();

        dataSource.setDriverClassName("org.sqlite.JDBC");

        dataSource.addConnectionProperty("foreign_keys", "ON");
        dataSource.addConnectionProperty("journal_mode", "WAL");
        dataSource.addConnectionProperty("transaction_mode", "IMMEDIATE");
        dataSource.addConnectionProperty("busy_timeout", "0");

        dataSource.setDefaultAutoCommit(true);

        dataSource.setUrl(connectionString);

        // Migrate database using Flyway
        // More info: https://flywaydb.org
        Flyway flyway = Flyway.configure().dataSource(connectionString, null, null).load();
        flyway.migrate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProviderRegister getProviderRegister() throws RegisterException {

        if (dataSource == null) throw new RegisterException("Datasource is null");

        return new SQLiteRegister(dataSource);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRegister getUserRegister() throws RegisterException {

        if (dataSource == null) throw new RegisterException("Datasource is null");

        return new SQLiteRegister(dataSource);

    }

    /**
     * Destroy the data source
     */
    public void close() throws RegisterException {

        try {

            dataSource.close();

            dataSource = null;

        } catch (Exception ex) {

            throw new RegisterException("Exception while closing dataSource", ex);

        }

    }

    @Override
    public TransactionManager getTransactionManager() throws RegisterException {

        return dataSource;

    }
}
