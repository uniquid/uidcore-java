package com.uniquid.core.connector.tls;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;

import org.bouncycastle.crypto.tls.TlsServerProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.core.InputMessage;
import com.uniquid.core.OutputMessage;
import com.uniquid.core.connector.Connector;
import com.uniquid.core.connector.ConnectorException;
import com.uniquid.core.connector.EndPoint;

public class TLSConnector implements Connector<JSONMessage> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TLSConnector.class.getName());

	private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
	private static final long TEN_YEARS_IN_MILLIS = 10l * 365 * ONE_DAY_IN_MILLIS;
	
	/**
     * The http response for all given queries.
     */
    private static final String MESSAGE = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: text/html; charset=UTF-8\r\n"
            + "\r\n" +
            "<html>\n" + 
            "<head>\n" + 
            "  <title>An Example Page</title>\n" + 
            "</head>\n" + 
            "<body>\n" + 
            "  Hello World, this is a very simple HTML document.\n" + 
            "</body>\n" + 
            "</html>";

	private String port;
	private CertificateData certificateData;

	public TLSConnector(String port) {

		this.port = port;

	}

	@Override
	public void start() throws ConnectorException {

		try {

			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			CertificateDataBuilder certBuilder = new CertificateDataBuilder();
			certBuilder.createSubjectKeyPair().loadIssuerCertificate().loadIssuerPrivateKey();
			certBuilder.setHostname("localhost");
			certBuilder.setNotAfter(new Date(System.currentTimeMillis() + TEN_YEARS_IN_MILLIS));
			certBuilder.setNotBefore(new Date(System.currentTimeMillis() - ONE_DAY_IN_MILLIS));
			certBuilder.setSerial(BigInteger.valueOf(System.currentTimeMillis()));
			certificateData = certBuilder.build();

		} catch (Exception ex) {

			throw new ConnectorException("Exception", ex);

		}

	}

	@Override
	public void stop() throws ConnectorException {

	}

	@Override
	public EndPoint<JSONMessage> accept() throws ConnectorException {

		try {

			ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));

			final Socket socket = serverSocket.accept();

			TlsServerProtocol sslServer = new TlsServerProtocol(socket.getInputStream(), socket.getOutputStream(),
					new SecureRandom());
			sslServer.accept(new RsaSignerTls12Server(certificateData));
			InputStream inputStream = sslServer.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				if ("".equals(line.trim())) {
					break;
				}
			}
			OutputStream outputStream = sslServer.getOutputStream();
			outputStream.write(MESSAGE.getBytes(StandardCharsets.UTF_8));
			outputStream.flush();
			socket.close();
			
			return null;
				
		} catch (Exception ex) {

			throw new ConnectorException("Exception", ex);

		}

	}

	@Override
	public OutputMessage<JSONMessage> createOutputMessage() throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputMessage<JSONMessage> sendOutputMessage(OutputMessage<JSONMessage> outputMessage, long timeout)
			throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

}
