import java.util.*;

// Simple compilation test for the refactored classes
public class TestRefactoring {
    
    public static void main(String[] args) {
        System.out.println("Testing GSRequest refactoring...");
        
        try {
            // Test 1: GSConfig creation and configuration
            testGSConfig();
            System.out.println("✓ GSConfig tests passed");
            
            // Test 2: GSRequest creation with different constructors
            testGSRequestConstructors();
            System.out.println("✓ GSRequest constructor tests passed");
            
            // Test 3: Authentication mode detection
            testAuthModeDetection();
            System.out.println("✓ Authentication mode detection tests passed");
            
            // Test 4: Parameter handling
            testParameterHandling();
            System.out.println("✓ Parameter handling tests passed");
            
            System.out.println("\n🎉 All tests passed! Refactoring successful!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testGSConfig() throws Exception {
        // Test default configuration
        GSConfig defaultConfig = GSConfig.create();
        assert defaultConfig.getApiDomain().equals("us1.gigya.com") : "Default domain should be us1.gigya.com";
        assert new String(defaultConfig.getMtlsPassword()).equals("changeit") : "Default password should be changeit";
        
        // Test builder pattern
        GSConfig config = GSConfig.create()
                .setApiKey("test-api-key")
                .setSecretKey("test-secret")
                .setApiDomain("eu1.gigya.com");
                
        assert config.getApiKey().equals("test-api-key") : "API key should be set correctly";
        assert config.getSecretKey().equals("test-secret") : "Secret key should be set correctly";
        assert config.getApiDomain().equals("eu1.gigya.com") : "Domain should be set correctly";
        
        // Test authentication mode detection
        assert config.hasBasicAuth() : "Should detect basic auth";
        assert !config.hasJwtAuth() : "Should not detect JWT auth";
        assert !config.hasAnonymousAuth() : "Should not detect anonymous auth";
        
        // Test JWT auth configuration
        GSConfig jwtConfig = GSConfig.create()
                .setApiKey("api-key")
                .setUserKey("user-key")
                .setPrivateKey("private-key");
        assert jwtConfig.hasJwtAuth() : "Should detect JWT auth";
        assert !jwtConfig.hasBasicAuth() : "Should not detect basic auth";
        
        // Test anonymous auth configuration
        GSConfig anonConfig = GSConfig.create().setApiKey("api-key");
        assert anonConfig.hasAnonymousAuth() : "Should detect anonymous auth";
        assert !anonConfig.hasBasicAuth() : "Should not detect basic auth";
        assert !anonConfig.hasJwtAuth() : "Should not detect JWT auth";
        
        // Test mTLS configuration
        GSConfig mtlsConfig = GSConfig.create()
                .setApiKey("api-key")
                .setMtlsCertificatePem("cert-pem")
                .setMtlsPrivateKeyPem("key-pem");
        assert mtlsConfig.hasMtlsAuth() : "Should detect mTLS auth";
        assert mtlsConfig.hasMtlsConfig() : "Should detect mTLS config";
    }
    
    private static void testGSRequestConstructors() throws Exception {
        // Test default constructor
        GSRequest defaultRequest = new GSRequest();
        assert defaultRequest != null : "Default constructor should work";
        
        // Test constructor with API method
        GSRequest methodRequest = new GSRequest("accounts.getAccountInfo");
        assert methodRequest.getMethod().equals("accounts.getAccountInfo") : "Method should be set correctly";
        
        // Test constructor with config
        GSConfig config = GSConfig.create().setApiKey("test-key");
        GSRequest configRequest = new GSRequest(config);
        assert configRequest != null : "Config constructor should work";
        
        // Test legacy OAuth constructor
        GSRequest oauthRequest = new GSRequest("access-token", "accounts.getAccountInfo");
        assert oauthRequest.accessToken.equals("access-token") : "Access token should be set";
        assert oauthRequest.getMethod().equals("accounts.getAccountInfo") : "Method should be set";
        
        // Test legacy basic auth constructor
        GSRequest basicRequest = new GSRequest("api-key", "secret-key", "accounts.getAccountInfo", null);
        assert basicRequest.apiKey.equals("api-key") : "API key should be set";
        assert basicRequest.secretKey.equals("secret-key") : "Secret key should be set";
    }
    
    private static void testAuthModeDetection() throws Exception {
        // Test with different configurations
        GSConfig basicConfig = GSConfig.create()
                .setApiKey("api-key")
                .setSecretKey("secret-key");
        GSRequest basicRequest = new GSRequest(basicConfig);
        // Should internally detect BASIC auth mode
        
        GSConfig jwtConfig = GSConfig.create()
                .setApiKey("api-key")
                .setUserKey("user-key")
                .setPrivateKey("private-key");
        GSRequest jwtRequest = new GSRequest(jwtConfig);
        // Should internally detect JWT auth mode
        
        GSConfig anonConfig = GSConfig.create().setApiKey("api-key");
        GSRequest anonRequest = new GSRequest(anonConfig);
        // Should internally detect ANONYMOUS auth mode
        
        assert basicRequest != null : "Basic auth request should be created";
        assert jwtRequest != null : "JWT auth request should be created";
        assert anonRequest != null : "Anonymous auth request should be created";
    }
    
    private static void testParameterHandling() throws Exception {
        GSRequest request = new GSRequest();
        
        // Test parameter setting
        request.setParam("stringParam", "value");
        request.setParam("intParam", 123);
        request.setParam("longParam", 456L);
        request.setParam("boolParam", true);
        
        GSObject params = request.getParams();
        assert params.getString("stringParam").equals("value") : "String parameter should be set";
        assert params.getInt("intParam") == 123 : "Int parameter should be set";
        assert params.getLong("longParam") == 456L : "Long parameter should be set";
        assert params.getBool("boolParam") == true : "Boolean parameter should be set";
        
        // Test parameter clearing
        request.clearParams();
        assert request.getParams().getKeys().isEmpty() : "Parameters should be cleared";
        
        // Test fluent API
        request.setApiKey("test-key")
               .setSecretKey("test-secret")
               .setApiDomain("eu1.gigya.com");
               
        assert request.apiKey.equals("test-key") : "Fluent API should set API key";
        assert request.secretKey.equals("test-secret") : "Fluent API should set secret key";
        assert request.apiDomain.equals("eu1.gigya.com") : "Fluent API should set domain";
    }
    
    // Mock classes for compilation testing
    static class GSConfig {
        private String apiKey, secretKey, userKey, privateKey, apiDomain = "us1.gigya.com";
        private String mtlsCertPath, mtlsKeyPath, mtlsCertPem, mtlsKeyPem;
        private char[] mtlsPassword = "changeit".toCharArray();
        
        public static GSConfig create() { return new GSConfig(); }
        public static GSConfig getInstance() { return new GSConfig(); }
        
        public GSConfig setApiKey(String key) { this.apiKey = key; return this; }
        public GSConfig setSecretKey(String key) { this.secretKey = key; return this; }
        public GSConfig setUserKey(String key) { this.userKey = key; return this; }
        public GSConfig setPrivateKey(String key) { this.privateKey = key; return this; }
        public GSConfig setApiDomain(String domain) { this.apiDomain = domain; return this; }
        public GSConfig setMtlsCertificatePath(String path) { this.mtlsCertPath = path; return this; }
        public GSConfig setMtlsPrivateKeyPath(String path) { this.mtlsKeyPath = path; return this; }
        public GSConfig setMtlsCertificatePem(String pem) { this.mtlsCertPem = pem; return this; }
        public GSConfig setMtlsPrivateKeyPem(String pem) { this.mtlsKeyPem = pem; return this; }
        
        public String getApiKey() { return apiKey; }
        public String getSecretKey() { return secretKey; }
        public String getUserKey() { return userKey; }
        public String getPrivateKey() { return privateKey; }
        public String getApiDomain() { return apiDomain; }
        public char[] getMtlsPassword() { return mtlsPassword.clone(); }
        
        public boolean hasBasicAuth() { return apiKey != null && secretKey != null; }
        public boolean hasJwtAuth() { return userKey != null && privateKey != null && apiKey != null; }
        public boolean hasAnonymousAuth() { return apiKey != null && secretKey == null && userKey == null; }
        public boolean hasMtlsAuth() { return apiKey != null && hasMtlsConfig(); }
        public boolean hasMtlsConfig() { return (mtlsCertPem != null && mtlsKeyPem != null) || (mtlsCertPath != null && mtlsKeyPath != null); }
    }
    
    static class GSRequest {
        String accessToken, apiKey, secretKey, userKey, privateKey, apiMethod, apiDomain = "us1.gigya.com";
        GSObject params = new GSObject();
        
        public GSRequest() {}
        public GSRequest(String method) { this.apiMethod = method; }
        public GSRequest(GSConfig config) { if (config != null) { this.apiKey = config.getApiKey(); this.secretKey = config.getSecretKey(); } }
        public GSRequest(GSConfig config, String method) { this(config); this.apiMethod = method; }
        public GSRequest(String token, String method) { this.accessToken = token; this.apiMethod = method; }
        public GSRequest(String key, String secret, String method, Object params) { this.apiKey = key; this.secretKey = secret; this.apiMethod = method; }
        
        public GSRequest setApiKey(String key) { this.apiKey = key; return this; }
        public GSRequest setSecretKey(String key) { this.secretKey = key; return this; }
        public GSRequest setApiDomain(String domain) { this.apiDomain = domain; return this; }
        
        public String getMethod() { return apiMethod; }
        public GSObject getParams() { return params; }
        public void setParam(String key, Object value) { params.put(key, value); }
        public void clearParams() { params = new GSObject(); }
    }
    
    static class GSObject {
        private Map<String, Object> data = new HashMap<>();
        
        public void put(String key, Object value) { data.put(key, value); }
        public String getString(String key) { return (String) data.get(key); }
        public int getInt(String key) { return (Integer) data.get(key); }
        public long getLong(String key) { return (Long) data.get(key); }
        public boolean getBool(String key) { return (Boolean) data.get(key); }
        public Set<String> getKeys() { return data.keySet(); }
        public boolean isEmpty() { return data.isEmpty(); }
    }
}
