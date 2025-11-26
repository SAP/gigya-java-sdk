package com.gigya.socialize;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Properties;

@RunWith(JUnit4.class)
public class GSConfigTest extends TestCase {

    @Test
    public void testDefaultConfiguration() {
        GSConfig config = GSConfig.create();
        assertEquals("us1.gigya.com", config.getApiDomain());
        assertNotNull(config.getMtlsPassword());
        assertEquals("changeit", new String(config.getMtlsPassword()));
    }

    @Test
    public void testBuilderPattern() {
        GSConfig config = GSConfig.create()
                .setApiKey("test-api-key")
                .setSecretKey("test-secret")
                .setApiDomain("eu1.gigya.com")
                .setUserKey("test-user")
                .setPrivateKey("test-private-key");

        assertEquals("test-api-key", config.getApiKey());
        assertEquals("test-secret", config.getSecretKey());
        assertEquals("eu1.gigya.com", config.getApiDomain());
        assertEquals("test-user", config.getUserKey());
        assertEquals("test-private-key", config.getPrivateKey());
    }

    @Test
    public void testAuthenticationModeDetection() {
        // Test Basic Auth
        GSConfig basicConfig = GSConfig.create()
                .setApiKey("api-key")
                .setSecretKey("secret-key");
        assertTrue(basicConfig.hasBasicAuth());
        assertFalse(basicConfig.hasJwtAuth());
        assertFalse(basicConfig.hasAnonymousAuth());

        // Test JWT Auth
        GSConfig jwtConfig = GSConfig.create()
                .setApiKey("api-key")
                .setUserKey("user-key")
                .setPrivateKey("private-key");
        assertTrue(jwtConfig.hasJwtAuth());
        assertFalse(jwtConfig.hasBasicAuth());
        assertFalse(jwtConfig.hasAnonymousAuth());

        // Test Anonymous Auth
        GSConfig anonConfig = GSConfig.create()
                .setApiKey("api-key");
        assertTrue(anonConfig.hasAnonymousAuth());
        assertFalse(anonConfig.hasBasicAuth());
        assertFalse(anonConfig.hasJwtAuth());
    }

    @Test
    public void testPropertiesConfiguration() {
        Properties props = new Properties();
        props.setProperty("gigya.api.key", "prop-api-key");
        props.setProperty("gigya.secret.key", "prop-secret");
        props.setProperty("gigya.api.domain", "au1.gigya.com");

        GSConfig.loadFromProperties(props);
        GSConfig config = GSConfig.getInstance();

        assertEquals("prop-api-key", config.getApiKey());
        assertEquals("prop-secret", config.getSecretKey());
        assertEquals("au1.gigya.com", config.getApiDomain());

        // Clean up singleton for other tests
        GSConfig.loadFromProperties(new Properties());
    }

    @Test
    public void testMtlsConfiguration() {
        GSConfig config = GSConfig.create()
                .setApiKey("api-key")
                .setMtlsCertificatePem("-----BEGIN CERTIFICATE-----\ntest\n-----END CERTIFICATE-----")
                .setMtlsPrivateKeyPem("-----BEGIN PRIVATE KEY-----\ntest\n-----END PRIVATE KEY-----");

        assertTrue(config.hasMtlsAuth());
        assertTrue(config.hasMtlsConfig());
    }
}
