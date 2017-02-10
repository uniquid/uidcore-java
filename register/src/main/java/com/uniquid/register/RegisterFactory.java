package com.uniquid.register;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

/**
 * Factory to create RegisterFactory
 * 
 * @author giuseppe
 *
 */
public abstract class RegisterFactory {

	// key: name (String), value: a RegisterFactory;
	private static final ConcurrentMap<String, RegisterFactory> registerFactoryMap = new ConcurrentHashMap<String, RegisterFactory>();

	protected Map<String, Object> factoryConfiguration;

	/**
	 * Retrieve the specified factory
	 * 
	 * @param className
	 * @param configuration
	 * @return
	 * @throws RegisterException
	 */
	public static RegisterFactory getFactory(Class<?> className, Map<String, Object> configuration)
			throws RegisterException {

		return getFactory(className.getName(), configuration);

	}

	/**
	 * Retrieve the specified factory
	 * 
	 * @param name
	 * @param configuration
	 * @return
	 * @throws RegisterException
	 */
	public static RegisterFactory getFactory(String name, Map<String, Object> configuration) throws RegisterException {

		RegisterFactory registerFactory = registerFactoryMap.get(name);

		if (registerFactory != null) {

			return registerFactory;

		} else {

			try {

				Class<?> clazz = Class.forName(name);

				Constructor<?> constructor = clazz.getConstructor(Map.class);

				RegisterFactory newInstance = (RegisterFactory) constructor.newInstance(configuration);
				RegisterFactory oldInstance = registerFactoryMap.putIfAbsent(name, newInstance);
				return oldInstance == null ? newInstance : oldInstance;

			} catch (Exception ex) {

				throw new RegisterException("Exception while creating factory " + name, ex);
			}

		}

	}

	/**
	 * Public default constructor
	 * 
	 * @param configuration
	 */
	public RegisterFactory(Map<String, Object> configuration) throws RegisterException {

		factoryConfiguration = new HashMap<String, Object>(configuration);

	}

	/**
	 * Returns a Provider Register
	 * 
	 * @return
	 * @throws RegisterException
	 */
	public abstract ProviderRegister getProviderRegister() throws RegisterException;

	/**
	 * Returns a User Register
	 * 
	 * @return
	 * @throws RegisterException
	 */
	public abstract UserRegister getUserRegister() throws RegisterException;

}
