package com.uniquid.register.impl.android;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

import android.content.Context;

/**
 * @author Beatrice Formai on 02/10/2017
 *         for Uniquid Inc.
 */
public class AndroidRegisterFactory implements RegisterFactory {

    private Register instance;

    public AndroidRegisterFactory(final Context context) throws RegisterException {

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
