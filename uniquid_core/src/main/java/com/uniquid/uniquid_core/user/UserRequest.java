package com.uniquid.uniquid_core.user;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.uniquid.uniquid_core.OutputMessage;

public interface UserRequest extends OutputMessage<Object> {

	public static final String SENDER = "SENDER";
	public static final String METHOD = "METHOD";
	public static final String PARAMS = "PARAMS";
	public static final String ID = "ID";

	public OutputStream getOutputStream() throws IOException;

	public PrintWriter getWriter() throws IOException;

}
