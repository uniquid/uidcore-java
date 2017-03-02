package com.uniquid.core.connector.tls;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.tls.Certificate;
import org.bouncycastle.crypto.tls.DefaultTlsSignerCredentials;

/**
 * Container for the certificate data needed by {@link DefaultTlsSignerCredentials}
 */
public class CertificateData {
    private Certificate certificate;
    private AsymmetricKeyParameter privateKeyParameter;

    public CertificateData(Certificate certificate,AsymmetricKeyParameter privateKeyParameter) {
        this.certificate = certificate;
        this.privateKeyParameter = privateKeyParameter;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public AsymmetricKeyParameter getPrivateKeyParameter() {
        return privateKeyParameter;
    }
}