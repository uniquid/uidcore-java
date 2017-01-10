package com.uniquid.uniquid_core.connector;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ConnectorFactory {

	// key: name (String), value: a RegisterFactory;
	private static final ConcurrentMap<String, ConnectorFactory> connectorFactoryMap = new ConcurrentHashMap<String, ConnectorFactory>();

	protected Map<String, Object> factoryConfiguration;

	public static ConnectorFactory getFactory(Class<?> className, Map<String, Object> configuration) throws Exception {

		return getFactory(className.getName(), configuration);

	}

	public static ConnectorFactory getFactory(String name, Map<String, Object> configuration) throws Exception {

		ConnectorFactory registerFactory = connectorFactoryMap.get(name);

		if (registerFactory != null) {

			return registerFactory;

		} else {

			Class<?> clazz = Class.forName(name);

			Constructor<?> constructor = clazz.getConstructor(Map.class);

			ConnectorFactory newInstance = (ConnectorFactory) constructor.newInstance(configuration);
			ConnectorFactory oldInstance = connectorFactoryMap.putIfAbsent(name, newInstance);
			return oldInstance == null ? newInstance : oldInstance;

		}

	}

	public abstract Connector createConnector() throws Exception;

	public ConnectorFactory(Map<String, Object> configuration) {

			factoryConfiguration = new HashMap<String, Object>(configuration);

		}

}
