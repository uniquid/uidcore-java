package com.uniquid.core.connector.tls;

import java.util.Map;

import com.uniquid.core.connector.Connector;
import com.uniquid.core.connector.ConnectorFactory;

public class TLSConnectorFactory extends ConnectorFactory {

	private static final String PREFIX = "TLSConnectorFactory";
	public static final String PORT = PREFIX + ".port";
	
	private static TLSConnector INSTANCE;
	
	public TLSConnectorFactory(Map<String, Object> configuration) {
		super(configuration);
	}

	@Override
	public Connector createConnector() throws Exception {
		
		if (INSTANCE == null) {
			
			String port = (String) factoryConfiguration.get(PORT);
			
			INSTANCE = new TLSConnector(port);
			
		}
		
		return INSTANCE;
		
	}

}
