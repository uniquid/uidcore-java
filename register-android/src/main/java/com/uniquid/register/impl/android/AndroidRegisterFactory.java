package com.uniquid.register.impl.android;

import android.content.Context;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

import java.util.Map;

/**
 * @author Beatrice Formai on 02/10/2017
 *         for Uniquid Inc.
 */
public class AndroidRegisterFactory extends RegisterFactory {

    private static final String PREFIX = "AndroidRegisterFactory";
    public static final String CONTEXT = PREFIX + ".context";

    private Register instance;

    public AndroidRegisterFactory(Map<String, Object> configuration) throws RegisterException {
        super(configuration);

        Context context = (Context) factoryConfiguration.get(CONTEXT);

        instance = new Register(context);
    }

	@Override
	public ProviderRegister getProviderRegister() throws RegisterException {
		return instance;
	}

	@Override
	public UserRegister getUserRegister() throws RegisterException {
		return instance;
	}
}
