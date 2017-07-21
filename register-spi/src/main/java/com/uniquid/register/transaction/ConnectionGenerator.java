package com.uniquid.register.transaction;

import java.sql.Connection;

public interface ConnectionGenerator {
	
	public Connection getConnection();

}
