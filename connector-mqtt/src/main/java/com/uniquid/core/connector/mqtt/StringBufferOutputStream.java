package com.uniquid.core.connector.mqtt;

import java.io.IOException;

import com.uniquid.core.provider.FunctionOutputStream;

/**
 * An implementation of an OutputStream that writes the data directly out to a
 * StringBuffer object. Useful for applications where an intermediate
 * ByteArrayOutputStream is required to append generated characters to a
 * StringBuffer;
 */
public class StringBufferOutputStream extends FunctionOutputStream {

	// the target buffer
	protected StringBuffer buffer;

	/**
	 * Create an output stream that writes to the target StringBuffer
	 *
	 * @param out
	 *            The wrapped output stream.
	 */
	public StringBufferOutputStream(StringBuffer out) {
		buffer = out;
	}

	// in order for this to work, we only need override the single character
	// form, as the others
	// funnel through this one by default.
	public void write(int ch) throws IOException {
		// just append the character
		buffer.append((char) ch);
	}
}
