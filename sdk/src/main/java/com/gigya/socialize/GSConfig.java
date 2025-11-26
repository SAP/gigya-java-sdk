package com.gigya.socialize;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * GSConfig - Centralized configuration for Gigya SDK authentication and settings.
 * Supports configuration via constants, environment variables, or properties files.
 */
public class GSConfig {
    
    // Environment variable names
    public static final String ENV_API_KEY = "GIGYA_API_KEY";
    public static final String ENV_SECRET_KEY = "GIGYA_SECRET_KEY";
    public static final String ENV_USER_KEY = "GIGYA_USER_KEY";
    public static final String ENV_PRIVATE_KEY = "GIGYA_PRIVATE_KEY";
    public static final String ENV_API_DOMAIN = "GIGYA_API_DOMAIN";
    public static final String ENV_MTLS_CERT_PATH = "GIGYA_MTLS_CERT_PATH";
    public static final String ENV_MTLS_KEY_PATH = "GIGYA_MTLS_KEY_PATH";
    public static final String ENV_MTLS_CERT_PEM = "GIGYA_MTLS_CERT_PEM";
    public static final String ENV_MTLS_KEY_PEM = "GIGYA_MTLS_KEY_PEM";
    public static final String ENV_MTLS_PASSWORD = "GIGYA_MTLS_PASSWORD";
    
    // Property file keys
    public static final String PROP_API_KEY = "gigya.api.key";
    public static final String PROP_SECRET_KEY = "gigya.secret.key";
    public static final String PROP_USER_KEY = "gigya.user.key";
    public static final String PROP_PRIVATE_KEY = "gigya.private.key";
    public static final String PROP_API_DOMAIN = "gigya.api.domain";
    public static final String PROP_MTLS_CERT_PATH = "gigya.mtls.cert.path";
    public static final String PROP_MTLS_KEY_PATH = "gigya.mtls.key.path";
    public static final String PROP_MTLS_CERT_PEM = "gigya.mtls.cert.pem";
    public static final String PROP_MTLS_KEY_PEM = "gigya.mtls.key.pem";
    public static final String PROP_MTLS_PASSWORD = "gigya.mtls.password";
    
    // Default values
    public static final String DEFAULT_API_DOMAIN = "us1.gigya.com";
    public static final String DEFAULT_MTLS_PASSWORD = "changeit";
    
    // Configuration values
    private String apiKey;
    private String secretKey;
    private String userKey;
    private String privateKey;
    private String apiDomain;
    private String mtlsCertificatePath;
    private String mtlsPrivateKeyPath;
    private String mtlsCertificatePem;
    private String mtlsPrivateKeyPem;
    private char[] mtlsPassword;
    
    // Singleton instance
    private static GSConfig instance;
    private static Properties configProperties;
    
    /**
     * Private constructor for singleton pattern
     */
    private GSConfig() {
        loadConfiguration();
    }
    
    /**
     * Get the singleton instance
     */
    public static synchronized GSConfig getInstance() {
        if (instance == null) {
            instance = new GSConfig();
        }
        return instance;
    }
    
    /**
     * Create a new config instance with custom values
     */
    public static GSConfig create() {
        return new GSConfig();
    }
    
    /**
     * Load configuration from default properties file
     */
    public static void loadFromPropertiesFile(String filePath) throws IOException {
        configProperties = new Properties();
        configProperties.load(Files.newBufferedReader(Paths.get(filePath)));
        // Refresh singleton instance
        instance = null;
    }
    
    /**
     * Load configuration from properties object
     */
    public static void loadFromProperties(Properties properties) {
        configProperties = properties;
        // Refresh singleton instance
        instance = null;
    }
    
    private void loadConfiguration() {
        // Load in priority order: 1. Environment variables, 2. Properties, 3. Defaults
        this.apiKey = getConfigValue(ENV_API_KEY, PROP_API_KEY, null);
        this.secretKey = getConfigValue(ENV_SECRET_KEY, PROP_SECRET_KEY, null);
        this.userKey = getConfigValue(ENV_USER_KEY, PROP_USER_KEY, null);
        this.privateKey = getConfigValue(ENV_PRIVATE_KEY, PROP_PRIVATE_KEY, null);
        this.apiDomain = getConfigValue(ENV_API_DOMAIN, PROP_API_DOMAIN, DEFAULT_API_DOMAIN);
        this.mtlsCertificatePath = getConfigValue(ENV_MTLS_CERT_PATH, PROP_MTLS_CERT_PATH, null);
        this.mtlsPrivateKeyPath = getConfigValue(ENV_MTLS_KEY_PATH, PROP_MTLS_KEY_PATH, null);
        this.mtlsCertificatePem = getConfigValue(ENV_MTLS_CERT_PEM, PROP_MTLS_CERT_PEM, null);
        this.mtlsPrivateKeyPem = getConfigValue(ENV_MTLS_KEY_PEM, PROP_MTLS_KEY_PEM, null);
        
        String passwordStr = getConfigValue(ENV_MTLS_PASSWORD, PROP_MTLS_PASSWORD, DEFAULT_MTLS_PASSWORD);
        this.mtlsPassword = passwordStr.toCharArray();
    }
    
    private String getConfigValue(String envVar, String propKey, String defaultValue) {
        // 1. Check environment variable
        String value = System.getenv(envVar);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        
        // 2. Check properties
        if (configProperties != null) {
            value = configProperties.getProperty(propKey);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        
        // 3. Return default
        return defaultValue;
    }
    
    // Getters
    public String getApiKey() { return apiKey; }
    public String getSecretKey() { return secretKey; }
    public String getUserKey() { return userKey; }
    public String getPrivateKey() { return privateKey; }
    public String getApiDomain() { return apiDomain; }
    public String getMtlsCertificatePath() { return mtlsCertificatePath; }
    public String getMtlsPrivateKeyPath() { return mtlsPrivateKeyPath; }
    public String getMtlsCertificatePem() { return mtlsCertificatePem; }
    public String getMtlsPrivateKeyPem() { return mtlsPrivateKeyPem; }
    public char[] getMtlsPassword() { return mtlsPassword != null ? mtlsPassword.clone() : null; }
    
    // Setters for builder pattern
    public GSConfig setApiKey(String apiKey) { this.apiKey = apiKey; return this; }
    public GSConfig setSecretKey(String secretKey) { this.secretKey = secretKey; return this; }
    public GSConfig setUserKey(String userKey) { this.userKey = userKey; return this; }
    public GSConfig setPrivateKey(String privateKey) { this.privateKey = privateKey; return this; }
    public GSConfig setApiDomain(String apiDomain) { this.apiDomain = apiDomain; return this; }
    public GSConfig setMtlsCertificatePath(String path) { this.mtlsCertificatePath = path; return this; }
    public GSConfig setMtlsPrivateKeyPath(String path) { this.mtlsPrivateKeyPath = path; return this; }
    public GSConfig setMtlsCertificatePem(String pem) { this.mtlsCertificatePem = pem; return this; }
    public GSConfig setMtlsPrivateKeyPem(String pem) { this.mtlsPrivateKeyPem = pem; return this; }
    public GSConfig setMtlsPassword(char[] password) { this.mtlsPassword = password; return this; }
    
    // Validation methods
    public boolean hasBasicAuth() {
        return apiKey != null && secretKey != null;
    }
    
    public boolean hasJwtAuth() {
        return userKey != null && privateKey != null && apiKey != null;
    }
    
    public boolean hasAnonymousAuth() {
        return apiKey != null && secretKey == null && userKey == null;
    }
    
    public boolean hasMtlsAuth() {
        return apiKey != null && hasMtlsConfig();
    }
    
    public boolean hasMtlsConfig() {
        return (mtlsCertificatePem != null && mtlsPrivateKeyPem != null) ||
               (mtlsCertificatePath != null && mtlsPrivateKeyPath != null &&
                new File(mtlsCertificatePath).exists() && new File(mtlsPrivateKeyPath).exists());
    }
    
    public String loadMtlsCertificate() throws IOException {
        if (mtlsCertificatePem != null && !mtlsCertificatePem.trim().isEmpty()) {
            return mtlsCertificatePem;
        }
        if (mtlsCertificatePath != null && new File(mtlsCertificatePath).exists()) {
            return new String(Files.readAllBytes(Paths.get(mtlsCertificatePath)), Charset.forName("UTF-8"));
        }
        throw new IllegalStateException("mTLS certificate not configured or file not found");
    }
    
    public String loadMtlsPrivateKey() throws IOException {
        if (mtlsPrivateKeyPem != null && !mtlsPrivateKeyPem.trim().isEmpty()) {
            return mtlsPrivateKeyPem;
        }
        if (mtlsPrivateKeyPath != null && new File(mtlsPrivateKeyPath).exists()) {
            return new String(Files.readAllBytes(Paths.get(mtlsPrivateKeyPath)), Charset.forName("UTF-8"));
        }
        throw new IllegalStateException("mTLS private key not configured or file not found");
    }
}
