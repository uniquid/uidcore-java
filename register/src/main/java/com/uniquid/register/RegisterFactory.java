package com.uniquid.register;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

/**
 * Factory to create RegisterFactory
 * @author giuseppe
 *
 */
public abstract class RegisterFactory {

	// key: name (String), value: a RegisterFactory;
	private static final ConcurrentMap<String, RegisterFactory> registerFactoryMap = new ConcurrentHashMap<String, RegisterFactory>();

	protected Map<String, Object> factoryConfiguration;

	public static RegisterFactory getFactory(Class<?> className, Map<String, Object> configuration) throws Exception {

		return getFactory(className.getName(), configuration);

	}
	
	public static RegisterFactory getFactory(String name, Map<String, Object> configuration) throws Exception {

		RegisterFactory registerFactory = registerFactoryMap.get(name);

		if (registerFactory != null) {

			return registerFactory;

		} else {

			Class<?> clazz = Class.forName(name);

			Constructor<?> constructor = clazz.getConstructor(Map.class);

			RegisterFactory newInstance = (RegisterFactory) constructor.newInstance(configuration);
			RegisterFactory oldInstance = registerFactoryMap.putIfAbsent(name, newInstance);
			return oldInstance == null ? newInstance : oldInstance;

		}

	}
	
	public abstract ProviderRegister createProviderRegister() throws Exception;
	
	public abstract UserRegister createUserRegister() throws Exception;
	
	public RegisterFactory(Map<String, Object> configuration) {

		factoryConfiguration = new HashMap<String, Object>(configuration);

	}

}
