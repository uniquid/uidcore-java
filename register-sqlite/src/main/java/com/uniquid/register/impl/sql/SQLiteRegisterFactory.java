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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Concrete class implementation of {@code RegisterFactory} that uses SQLite as data store.
 */
public class SQLiteRegisterFactory implements RegisterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteRegisterFactory.class);

    protected TransactionAwareBasicDataSource dataSource;

    /**
     * Creates an instance from the connection string
     *
     * @param connectionString the database connection string
     * @throws RegisterException in case a problem occurs.
     */
    public SQLiteRegisterFactory(final String connectionString) throws RegisterException {

        if (connectionString == null) throw new RegisterException("connectionString is null!");

        try {

            dataSource = new TransactionAwareBasicDataSource();

            dataSource.setDriverClassName("org.sqlite.JDBC");

            dataSource.addConnectionProperty("foreign_keys", "ON");
            dataSource.addConnectionProperty("journal_mode", "WAL");
            dataSource.addConnectionProperty("transaction_mode", "IMMEDIATE");
            dataSource.addConnectionProperty("busy_timeout", "0");

            dataSource.setDefaultAutoCommit(true);

            dataSource.setUrl(connectionString);

            Connection connection = dataSource.getConnection();

            if (!tableExist(connection, "provider_channel")) {

                LOGGER.info("Found empty database! Creating needed tables");

                initDb(connection);

            }

            connection.close();

        } catch (Exception ex) {

            throw new RegisterException("Exception while creating register", ex);

        }
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

    /**
     * Initialize the database
     * @param connection the connection to the database
     * @throws RegisterException in case a problem occurs
     */
    protected void initDb(Connection connection) throws RegisterException {

        try {

            Statement statement = connection.createStatement();

            statement.executeUpdate(SQLiteRegister.CREATE_PROVIDER_TABLE);
            statement.executeUpdate(SQLiteRegister.CREATE_USER_TABLE);

            statement.close();

        } catch (Exception ex) {

            throw new RegisterException("Exception while creating node tables", ex);

        }

    }

    /**
     * This is an utility method that checks if a table exists in the database
     * @param conn the connection to the database
     * @param tableName name of table to check
     * @return true or false
     * @throws SQLException in case a problem occurs
     */
    private static boolean tableExist(Connection conn, String tableName) throws SQLException {

        boolean tExists = false;
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(tableName)) {
                    tExists = true;
                    break;
                }
            }
        }
        return tExists;
    }

}
