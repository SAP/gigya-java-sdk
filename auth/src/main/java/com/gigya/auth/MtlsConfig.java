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

    private final String certificatePem;
    private final String certificatePath;

    private final String privateKeyPem;
    private final String privateKeyPath;

    private final char[] keyStorePassword;

    public MtlsConfig(
            String certificatePem,
            String certificatePath,
            String privateKeyPem,
            String privateKeyPath,
            char[] keyStorePassword
    ) {
        this.certificatePem = certificatePem;
        this.certificatePath = certificatePath;
        this.privateKeyPem = privateKeyPem;
        this.privateKeyPath = privateKeyPath;
        this.keyStorePassword = (keyStorePassword != null)
                ? keyStorePassword.clone()
                : "changeit".toCharArray();

        validate();
    }

    // ---------- STATIC FACTORY METHODS ---------- //

    /**
     * Use PEM strings, default password
     */
    public static MtlsConfig fromPem(String certPem, String keyPem) {
        return new MtlsConfig(certPem, null, keyPem, null, null);
    }

    /**
     * Use PEM strings + custom password
     */
    public static MtlsConfig fromPem(String certPem, String keyPem, char[] password) {
        return new MtlsConfig(certPem, null, keyPem, null, password);
    }

    /**
     * Use file paths, default password
     */
    public static MtlsConfig fromFiles(String certPath, String keyPath) {
        return new MtlsConfig(null, certPath, null, keyPath, null);
    }

    /**
     * Use file paths + custom password
     */
    public static MtlsConfig fromFiles(String certPath, String keyPath, char[] password) {
        return new MtlsConfig(null, certPath, null, keyPath, password);
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
