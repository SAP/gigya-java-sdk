package com.gigya.auth;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * MtlsConfig - Configuration holder for mutual TLS (mTLS) settings.
 * Supports both file paths and in-memory PEM content.
 */
public class MtlsConfig {

    private String certificatePath;
    private String privateKeyPath;

    private String certificatePem;
    private String privateKeyPem;

    private char[] keyStorePassword;

    public MtlsConfig() {}

    public MtlsConfig setCertificatePath(String path) {
        this.certificatePath = path;
        return this;
    }

    public MtlsConfig setPrivateKeyPath(String path) {
        this.privateKeyPath = path;
        return this;
    }

    public MtlsConfig setCertificatePem(String pem) {
        this.certificatePem = pem;
        return this;
    }

    public MtlsConfig setPrivateKeyPem(String pem) {
        this.privateKeyPem = pem;
        return this;
    }

    public MtlsConfig setKeyStorePassword(char[] keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
        return this;
    }

    public static MtlsConfig fromDefaultLocation() {
        MtlsConfig cfg = new MtlsConfig();
        Path base = Paths.get("config", "mtls");
        cfg.certificatePath = base.resolve("client.pem").toString();
        cfg.privateKeyPath = base.resolve("client.key").toString();
        return cfg;
    }

    public char[] getPassword() {
        return keyStorePassword != null ? keyStorePassword.clone() : "changeit".toCharArray();
    }

    public String loadCertificate() throws IOException {
        return loadFromPemOrFile(certificatePem, certificatePath, "Certificate");
    }

    public String loadPrivateKey() throws IOException {
        return loadFromPemOrFile(privateKeyPem, privateKeyPath, "Private key");
    }

    private String loadFromPemOrFile(String pem, String path, String resourceName) throws IOException {
        if (pem != null && pem.length() > 0) return pem;
        if (path != null && new File(path).exists())
            return new String(Files.readAllBytes(Paths.get(path)), Charset.forName("UTF-8"));
        throw new IllegalStateException(resourceName + " PEM not provided or file not found");
    }

    public void validate() {
        if (!isValueOrFileProvided(certificatePem, certificatePath)) {
            throw new IllegalStateException("mTLS certificate missing (no PEM or file path provided)");
        }

        if (!isValueOrFileProvided(privateKeyPem, privateKeyPath)) {
            throw new IllegalStateException("mTLS private key missing (no PEM or file path provided)");
        }
    }

    private boolean isValueOrFileProvided(String pemValue, String filePath) {
        return (pemValue != null && pemValue.length() > 0) ||
                (filePath != null && new File(filePath).exists());
    }
}

