package com.uniquid.register.impl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

import com.uniquid.register.transaction.ConnectionGenerator;

public class TransactionableQueryRunner extends QueryRunner {
	
	private ConnectionGenerator connectionGenerator;

	/**
	 * Constructor for QueryRunner that takes a <code>DataSource</code> to use.
	 *
	 * Methods that do not take a <code>Connection</code> parameter will
	 * retrieve connections from this <code>DataSource</code>.
	 *
	 * @param ds
	 *            The <code>DataSource</code> to retrieve connections from.
	 */
	public TransactionableQueryRunner(DataSource ds, ConnectionGenerator connectionGenerator) {
		super(ds);
		
		this.connectionGenerator = connectionGenerator;
	}

	/**
	 * Factory method that creates and initializes a <code>Connection</code>
	 * object. <code>QueryRunner</code> methods always call this method to
	 * retrieve connections from its DataSource. Subclasses can override this
	 * method to provide special <code>Connection</code> configuration if
	 * needed. This implementation simply calls <code>ds.getConnection()</code>.
	 *
	 * @return An initialized <code>Connection</code>.
	 * @throws SQLException
	 *             if a database access error occurs
	 * @since DbUtils 1.1
	 */
	protected Connection prepareConnection() throws SQLException {

		// First check if there is a connection inside the threadlocal
		Connection connection = connectionGenerator.getConnection();
		
		if (connection != null) {
			return connection;
		}
		
		// if not delegate to superclass
		return super.prepareConnection();
		
	}

}
