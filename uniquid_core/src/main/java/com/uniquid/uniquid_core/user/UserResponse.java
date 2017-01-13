package com.uniquid.uniquid_core.user;

import java.io.InputStream;

import com.uniquid.uniquid_core.InputMessage;

public interface UserResponse extends InputMessage<Object> {

	public static final String SENDER = "SENDER";
	public static final String RESULT = "RESULT";
	public static final String ERROR = "ERROR";
	public static final String ID = "ID";

	/**
	 * Retrieves the body of the request as binary data using a
	 * ServletInputStream.
	 * 
	 * @return
	 */
	InputStream getInputStream();

}
