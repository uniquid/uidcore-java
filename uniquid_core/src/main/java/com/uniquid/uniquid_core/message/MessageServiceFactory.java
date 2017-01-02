package com.uniquid.uniquid_core.message;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class MessageServiceFactory {

	// key: name (String), value: a RegisterFactory;
	private static final ConcurrentMap<String, MessageServiceFactory> registerFactoryMap = new ConcurrentHashMap<String, MessageServiceFactory>();

	protected Map<String, Object> factoryConfiguration;

	public static MessageServiceFactory getFactory(Class<?> className, Map<String, Object> configuration) throws Exception {

		return getFactory(className.getName(), configuration);

	}

	public static MessageServiceFactory getFactory(String name, Map<String, Object> configuration) throws Exception {

		MessageServiceFactory registerFactory = registerFactoryMap.get(name);

		if (registerFactory != null) {

			return registerFactory;

		} else {

			Class<?> clazz = Class.forName(name);

			Constructor<?> constructor = clazz.getConstructor(Map.class);

			MessageServiceFactory newInstance = (MessageServiceFactory) constructor.newInstance(configuration);
			MessageServiceFactory oldInstance = registerFactoryMap.putIfAbsent(name, newInstance);
			return oldInstance == null ? newInstance : oldInstance;

		}

	}

	public abstract MessageService createMessageService() throws Exception;

	public MessageServiceFactory(Map<String, Object> configuration) {

			factoryConfiguration = new HashMap<String, Object>(configuration);

		}

}
