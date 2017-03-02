package com.uniquid.core.connector.tls;

import java.io.IOException;

import org.bouncycastle.crypto.tls.DefaultTlsServer;
import org.bouncycastle.crypto.tls.DefaultTlsSignerCredentials;
import org.bouncycastle.crypto.tls.HashAlgorithm;
import org.bouncycastle.crypto.tls.ProtocolVersion;
import org.bouncycastle.crypto.tls.SignatureAlgorithm;
import org.bouncycastle.crypto.tls.SignatureAndHashAlgorithm;
import org.bouncycastle.crypto.tls.TlsSignerCredentials;

/**
 * A {@link DefaultTlsServer} that uses TLS 1.2 and RSA for the certificate keys.
 */
public class RsaSignerTls12Server extends DefaultTlsServer {
    private CertificateData certificateData;

    public RsaSignerTls12Server(CertificateData certificateData) {
        this.certificateData = certificateData;
    }
    
    protected TlsSignerCredentials getRSASignerCredentials() throws IOException {
        SignatureAndHashAlgorithm signatureAndHashAlgorithm = new SignatureAndHashAlgorithm(
                HashAlgorithm.sha256, SignatureAlgorithm.rsa);
        return new DefaultTlsSignerCredentials(context, certificateData.getCertificate(),
                certificateData.getPrivateKeyParameter(), signatureAndHashAlgorithm);
    }
    
    protected org.bouncycastle.crypto.tls.ProtocolVersion getMaximumVersion() {
        return ProtocolVersion.TLSv12;
    };
}