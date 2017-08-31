/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uniquid.register.impl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes SQL queries with pluggable strategies for handling
 * <code>ResultSet</code>s. This class is thread safe.
 *
 * @see ResultSetHandler
 */
public class TransactionAwareQueryRunner extends QueryRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionAwareQueryRunner.class.getName());
	
	
	/**
	 * Constructor for QueryRunner.
	 */
	public TransactionAwareQueryRunner() {
		super();
	}

	/**
	 * Constructor for QueryRunner that controls the use of
	 * <code>ParameterMetaData</code>.
	 *
	 * @param pmdKnownBroken
	 *            Some drivers don't support
	 *            {@link java.sql.ParameterMetaData#getParameterType(int) }; if
	 *            <code>pmdKnownBroken</code> is set to true, we won't even try
	 *            it; if false, we'll try it, and if it breaks, we'll remember
	 *            not to use it again.
	 */
	public TransactionAwareQueryRunner(boolean pmdKnownBroken) {
		super(pmdKnownBroken);
	}

	/**
	 * Constructor for QueryRunner that takes a <code>DataSource</code> to use.
	 *
	 * Methods that do not take a <code>Connection</code> parameter will
	 * retrieve connections from this <code>DataSource</code>.
	 *
	 * @param ds
	 *            The <code>DataSource</code> to retrieve connections from.
	 */
	public TransactionAwareQueryRunner(DataSource ds) {
		super(ds);
	}

	/**
	 * Constructor for QueryRunner that takes a <code>DataSource</code> and
	 * controls the use of <code>ParameterMetaData</code>. Methods that do not
	 * take a <code>Connection</code> parameter will retrieve connections from
	 * this <code>DataSource</code>.
	 *
	 * @param ds
	 *            The <code>DataSource</code> to retrieve connections from.
	 * @param pmdKnownBroken
	 *            Some drivers don't support
	 *            {@link java.sql.ParameterMetaData#getParameterType(int) }; if
	 *            <code>pmdKnownBroken</code> is set to true, we won't even try
	 *            it; if false, we'll try it, and if it breaks, we'll remember
	 *            not to use it again.
	 */
	public TransactionAwareQueryRunner(DataSource ds, boolean pmdKnownBroken) {
		super(ds, pmdKnownBroken);
	}

	/**
	 * Close a <code>Connection</code>. This implementation avoids closing if
	 * null and does <strong>not</strong> suppress any exceptions. Subclasses
	 * can override to provide special handling like logging.
	 *
	 * @param conn
	 *            Connection to close
	 * @throws SQLException
	 *             if a database access error occurs
	 * @since DbUtils 1.1
	 */
	protected void close(Connection conn) throws SQLException {
		
		LOGGER.debug("Closing connection " + conn);
		
		if (conn instanceof TransactionAwareConnection) {
			
			TransactionAwareConnection txConn = (TransactionAwareConnection) conn;
			
			if (txConn.inTransaction()) {
				
				LOGGER.debug("We are in a transaction! Avoid to close connection");
				
			} else {
				
				super.close(conn);
				
			}

		} else {
			
			super.close(conn);
		
		}
	}
}
