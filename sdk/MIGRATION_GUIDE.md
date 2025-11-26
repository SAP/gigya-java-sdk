# GSRequest Unification Migration Guide

This guide explains how to migrate from multiple GSRequest classes to the new unified GSRequest class that supports all authentication methods through configuration.

## Overview

The Gigya Java SDK has been refactored to use a single `GSRequest` class instead of multiple specialized classes:

- `GSRequest` (original) - Now supports all authentication methods
- `GSAuthRequest` (auth module) - **Replaced by unified GSRequest**
- `GSAuthMtlsRequest` (auth module) - **Replaced by unified GSRequest**  
- `GSAnonymousRequest` (auth module) - **Replaced by unified GSRequest**

## New Configuration System

Authentication is now configured through `GSConfig` which supports multiple configuration sources:

1. **Environment Variables** (highest priority)
2. **Properties Files**
3. **Programmatic Configuration**
4. **Constructor Parameters** (for backward compatibility)

### Environment Variables

```bash
export GIGYA_API_KEY="your-api-key"
export GIGYA_SECRET_KEY="your-secret-key"
export GIGYA_USER_KEY="your-user-key"
export GIGYA_PRIVATE_KEY="your-private-key"
export GIGYA_API_DOMAIN="eu1.gigya.com"
export GIGYA_MTLS_CERT_PATH="/path/to/client.pem"
export GIGYA_MTLS_KEY_PATH="/path/to/client.key"
```

### Properties File Configuration

```properties
# gigya.properties
gigya.api.key=your-api-key
gigya.secret.key=your-secret-key
gigya.user.key=your-user-key
gigya.private.key=your-private-key
gigya.api.domain=eu1.gigya.com
gigya.mtls.cert.path=/path/to/client.pem
gigya.mtls.key.path=/path/to/client.key
```

Load properties:
```java
GSConfig.loadFromPropertiesFile("gigya.properties");
```

## Migration Examples

### 1. Basic Authentication (API Key + Secret)

**Before:**
```java
GSRequest request = new GSRequest("api-key", "secret-key", "accounts.getAccountInfo");
```

**After (Option A - Using GSConfig):**
```java
// Set up configuration once
GSConfig config = GSConfig.create()
    .setApiKey("api-key")
    .setSecretKey("secret-key");

GSRequest request = new GSRequest(config, "accounts.getAccountInfo");
```

**After (Option B - Global configuration):**
```java
// Set up global configuration once
GSConfig.loadFromProperties(properties);

// Use anywhere in your application
GSRequest request = new GSRequest("accounts.getAccountInfo");
```

**After (Option C - Legacy compatibility):**
```java
// Still works - no changes needed
GSRequest request = new GSRequest("api-key", "secret-key", "accounts.getAccountInfo");
```

### 2. JWT Authentication (User Key + Private Key)

**Before:**
```java
GSAuthRequest request = new GSAuthRequest("user-key", "private-key", "api-key", "accounts.getAccountInfo");
```

**After:**
```java
GSConfig config = GSConfig.create()
    .setApiKey("api-key")
    .setUserKey("user-key")
    .setPrivateKey("private-key");

GSRequest request = new GSRequest(config, "accounts.getAccountInfo");
```

### 3. Anonymous Authentication (API Key Only)

**Before:**
```java
GSAnonymousRequest request = new GSAnonymousRequest("api-key", "us1.gigya.com", "accounts.getSchema");
```

**After:**
```java
GSConfig config = GSConfig.create()
    .setApiKey("api-key")
    .setApiDomain("us1.gigya.com");

GSRequest request = new GSRequest(config, "accounts.getSchema");
```

### 4. mTLS Authentication

**Before:**
```java
MtlsConfig mtlsConfig = new MtlsConfig()
    .setCertificatePath("client.pem")
    .setPrivateKeyPath("client.key");
    
GSAuthMtlsRequest request = new GSAuthMtlsRequest("api-key", "accounts.login", mtlsConfig);
```

**After:**
```java
GSConfig config = GSConfig.create()
    .setApiKey("api-key")
    .setMtlsCertificatePath("client.pem")
    .setMtlsPrivateKeyPath("client.key");

GSRequest request = new GSRequest(config, "accounts.login");
```

### 5. OAuth Token Authentication

**Before:**
```java
GSRequest request = new GSRequest("access-token", "accounts.getAccountInfo");
```

**After:**
```java
// No changes needed - still works as before
GSRequest request = new GSRequest("access-token", "accounts.getAccountInfo");

// Or with config
GSConfig config = GSConfig.create();
GSRequest request = new GSRequest(config);
request.setAccessToken("access-token");
request.setMethod("accounts.getAccountInfo");
```

## Authentication Modes

The unified GSRequest automatically detects the authentication mode based on available credentials:

1. **OAUTH** - When `accessToken` is provided
2. **MTLS** - When API key and mTLS certificates are available
3. **JWT** - When `userKey`, `privateKey`, and `apiKey` are available
4. **BASIC** - When `apiKey` and `secretKey` are available  
5. **ANONYMOUS** - When only `apiKey` is available

## Fluent API

The new GSRequest supports a fluent API for better developer experience:

```java
GSRequest request = new GSRequest()
    .setApiKey("api-key")
    .setSecretKey("secret-key")
    .setApiDomain("eu1.gigya.com");
    
request.setMethod("accounts.getAccountInfo");
request.setParam("UID", "user-id");
GSResponse response = request.send();
```

## Best Practices

### 1. Use Global Configuration

Set up configuration once at application startup:

```java
// At application startup
Properties props = loadPropertiesFromFile("gigya.properties");
GSConfig.loadFromProperties(props);

// Anywhere in your application
GSRequest request = new GSRequest("accounts.getAccountInfo");
```

### 2. Environment-Specific Configuration

Use different configurations for different environments:

```java
String environment = System.getProperty("env", "development");
String configFile = "gigya-" + environment + ".properties";
GSConfig.loadFromPropertiesFile(configFile);
```

### 3. Custom Configuration per Request

For special cases where you need different credentials:

```java
GSConfig specialConfig = GSConfig.create()
    .setApiKey("special-api-key")
    .setSecretKey("special-secret");
    
GSRequest request = new GSRequest(specialConfig, "admin.getUsers");
```

## Error Handling

The unified GSRequest provides better error messages for authentication issues:

```java
GSRequest request = new GSRequest("invalid.method");
GSResponse response = request.send();

if (response.getErrorCode() != 0) {
    System.out.println("Error: " + response.getErrorMessage());
    // Error: API method not specified (400002)
    // Error: Insufficient authentication credentials (400002)
}
```

## Backward Compatibility

All existing GSRequest constructors continue to work without any changes. You can migrate incrementally:

1. **Phase 1**: Continue using existing code
2. **Phase 2**: Set up GSConfig for new requests
3. **Phase 3**: Gradually migrate existing requests to use GSConfig
4. **Phase 4**: Remove auth module dependencies

## Dependencies

After migration, you can remove the auth module dependency from your `build.gradle`:

```gradle
dependencies {
    // Remove this line after migration
    // implementation project(':auth')
    
    // Keep the main SDK
    implementation project(':sdk')
}
```

## Configuration Reference

### GSConfig Methods

```java
// Builder pattern setters
GSConfig setApiKey(String apiKey)
GSConfig setSecretKey(String secretKey)
GSConfig setUserKey(String userKey)
GSConfig setPrivateKey(String privateKey)
GSConfig setApiDomain(String apiDomain)
GSConfig setMtlsCertificatePath(String path)
GSConfig setMtlsPrivateKeyPath(String path)
GSConfig setMtlsCertificatePem(String pem)
GSConfig setMtlsPrivateKeyPem(String pem)
GSConfig setMtlsPassword(char[] password)

// Validation methods
boolean hasBasicAuth()
boolean hasJwtAuth()
boolean hasAnonymousAuth()
boolean hasMtlsAuth()
boolean hasMtlsConfig()

// Loading methods
String loadMtlsCertificate() throws IOException
String loadMtlsPrivateKey() throws IOException
```

### Environment Variables

| Variable | Property | Description |
|----------|----------|-------------|
| `GIGYA_API_KEY` | `gigya.api.key` | Site API key |
| `GIGYA_SECRET_KEY` | `gigya.secret.key` | Site secret key |
| `GIGYA_USER_KEY` | `gigya.user.key` | User key for JWT auth |
| `GIGYA_PRIVATE_KEY` | `gigya.private.key` | Private key for JWT auth |
| `GIGYA_API_DOMAIN` | `gigya.api.domain` | API domain (us1.gigya.com, eu1.gigya.com, etc.) |
| `GIGYA_MTLS_CERT_PATH` | `gigya.mtls.cert.path` | Path to client certificate |
| `GIGYA_MTLS_KEY_PATH` | `gigya.mtls.key.path` | Path to private key |
| `GIGYA_MTLS_CERT_PEM` | `gigya.mtls.cert.pem` | Client certificate PEM content |
| `GIGYA_MTLS_KEY_PEM` | `gigya.mtls.key.pem` | Private key PEM content |
| `GIGYA_MTLS_PASSWORD` | `gigya.mtls.password` | Keystore password |
