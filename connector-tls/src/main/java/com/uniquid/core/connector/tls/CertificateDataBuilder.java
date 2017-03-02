package com.uniquid.core.connector.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.tls.Certificate;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

/**
 * Builds dynamically a site certificate signed by fixed root certificate.
 * All methods, except the {@link CertificateDataBuilder#build()}, provide
 * a required data piece for the certificate and have to be called once.
 */
public class CertificateDataBuilder {
    private KeyPair subjectKeyPair = null;
    private PrivateKey issuerPrivateKey = null;
    private org.bouncycastle.asn1.x509.Certificate issuerCertificate = null;
    private String hostname = null;
    private Date notBefore = null;
    private Date notAfter = null;
    private BigInteger serial = null;
    
    public CertificateDataBuilder createSubjectKeyPair() {
        KeyPairGenerator kpGen = null;
        try {
            kpGen = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
        kpGen.initialize(2048, new SecureRandom());
        subjectKeyPair = kpGen.generateKeyPair();
        return this;
    }
    
    private PemObject loadPemResource(String resource) throws IOException {
        InputStream inputStream = CertificateDataBuilder.class.getResourceAsStream(resource);
        try (PemReader p = new PemReader(new InputStreamReader(inputStream));) {
            PemObject o = p.readPemObject();
            return o;
        }
    }
    
    public CertificateDataBuilder loadIssuerPrivateKey() throws IOException {
        PemObject o = loadPemResource("/rootCA.key");
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        KeySpec privateKeySpec = new PKCS8EncodedKeySpec(o.getContent());
        try {
            issuerPrivateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        return this;
    }
    
    public CertificateDataBuilder loadIssuerCertificate() throws IOException {
        PemObject pem = loadPemResource("/rootCA.crt");
        if (pem.getType().endsWith("CERTIFICATE"))
        {
            issuerCertificate = org.bouncycastle.asn1.x509.Certificate.getInstance(pem.getContent());
        } else {
            throw new RuntimeException("Failed to laod root certificate.");
        }
        return this;
    }
    
    public CertificateDataBuilder setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }
    public CertificateDataBuilder setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
        return this;
    }
    public CertificateDataBuilder setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
        return this;
    }
    public CertificateDataBuilder setSerial(BigInteger serial) {
        this.serial = serial;
        return this;
    }
    
    public CertificateData build() throws IOException {
        if (subjectKeyPair == null || issuerPrivateKey == null || issuerCertificate == null
                || hostname == null || notBefore == null || notAfter == null || serial == null) {
            throw new IllegalStateException("Builder not initialized");
        }
        X500Principal subject = new X500Principal("CN=" + hostname);
        X500Principal issuer = new X500Principal(issuerCertificate.getSubject().getEncoded());
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer, serial,
                notBefore, notAfter, subject, subjectKeyPair.getPublic());
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        ContentSigner sigGen;
        try {
            sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(
                    BouncyCastleProvider.PROVIDER_NAME).build(issuerPrivateKey);
        } catch (OperatorCreationException e) {
            throw new RuntimeException(e);
        }
        org.bouncycastle.asn1.x509.Certificate subjectCertificate = certBuilder.build(sigGen)
                .toASN1Structure();
        Certificate cCert = new Certificate(new org.bouncycastle.asn1.x509.Certificate[] {
                subjectCertificate, issuerCertificate });
        AsymmetricKeyParameter privateKeyParameter = PrivateKeyFactory.createKey(subjectKeyPair
                .getPrivate().getEncoded());
        return new CertificateData(cCert, privateKeyParameter);
    }
}
