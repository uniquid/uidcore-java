package com.uniquid.uniquid_core.connector;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ConnectorServiceFactory {

	// key: name (String), value: a RegisterFactory;
	private static final ConcurrentMap<String, ConnectorServiceFactory> connectorFactoryMap = new ConcurrentHashMap<String, ConnectorServiceFactory>();

	protected Map<String, Object> factoryConfiguration;

	public static ConnectorServiceFactory getFactory(Class<?> className, Map<String, Object> configuration) throws Exception {

		return getFactory(className.getName(), configuration);

	}

	public static ConnectorServiceFactory getFactory(String name, Map<String, Object> configuration) throws Exception {

		ConnectorServiceFactory registerFactory = connectorFactoryMap.get(name);

		if (registerFactory != null) {

			return registerFactory;

		} else {

			Class<?> clazz = Class.forName(name);

			Constructor<?> constructor = clazz.getConstructor(Map.class);

			ConnectorServiceFactory newInstance = (ConnectorServiceFactory) constructor.newInstance(configuration);
			ConnectorServiceFactory oldInstance = connectorFactoryMap.putIfAbsent(name, newInstance);
			return oldInstance == null ? newInstance : oldInstance;

		}

	}

	public abstract ConnectorService createConnectorService() throws Exception;

	public ConnectorServiceFactory(Map<String, Object> configuration) {

			factoryConfiguration = new HashMap<String, Object>(configuration);

		}

}
