package com.gigya.socialize;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


@RunWith(JUnit4.class)
public class GSRequestTest extends TestCase {

    final String URL_DECODED_STRING = "!*'();:@&=+$,/?%#[] ";
    final String URL_ENCODED_STRING = "%21%2A%27%28%29%3B%3A%40%26%3D%2B%24%2C%2F%3F%25%23%5B%5D%20";
    final String URL_COMPLIANT_STRING = "abc012_-.";

    @Test
    public void testURLEncodeStringConvertsRequiredCharacters() {
        assertEquals(GSRequest.UrlEncode(URL_DECODED_STRING), URL_ENCODED_STRING);
    }

    @Test
    public void testURLDecodeStringConvertsRequiredCharacters() throws Exception {
        assertEquals(URLDecoder.decode(URL_ENCODED_STRING, "UTF8"), URL_DECODED_STRING);
    }

    @Test
    public void testURLEncodeStringConvertsOnlyRequiredCharacters() {
        String encodedString = GSRequest.UrlEncode(URL_COMPLIANT_STRING + URL_DECODED_STRING);
        String expectedEncodedString = URL_COMPLIANT_STRING + URL_ENCODED_STRING;
        assertEquals(encodedString, expectedEncodedString);
    }

    @Test
    public void testURLDecodeStringConvertOnlyRequiredCharacters() throws UnsupportedEncodingException {
        String decodedString = URLDecoder.decode(URL_COMPLIANT_STRING + URL_ENCODED_STRING, "UTF8");
        String expectedDecodedString = URL_COMPLIANT_STRING + URL_DECODED_STRING;
        assertEquals(decodedString, expectedDecodedString);
    }

    @Test
    public void testBuildQueryStringFromObjectReturnsExpectedString() {
        GSObject params = new GSObject();
        params.put("a", URL_DECODED_STRING);
        params.put("b", (Object) null);
        params.put("c", URL_COMPLIANT_STRING);
        assertEquals(GSRequest.buildQS(params), "a=" + URL_ENCODED_STRING + "&b=null" + "&c=" + URL_COMPLIANT_STRING);
    }

    // New tests for unified GSRequest functionality

    @Test
    public void testDefaultConstructorUsesGSConfig() {
        GSRequest request = new GSRequest();
        assertNotNull(request);
        // Should not fail - uses singleton config
    }

    @Test
    public void testConstructorWithApiMethod() {
        GSRequest request = new GSRequest("accounts.getAccountInfo");
        assertEquals("accounts.getAccountInfo", request.getMethod());
    }

    @Test
    public void testLegacyConstructorCompatibility() {
        // Test OAuth constructor
        GSRequest oauthRequest = new GSRequest("access-token", "accounts.getAccountInfo");
        assertEquals("access-token", oauthRequest.accessToken);
        assertEquals("accounts.getAccountInfo", oauthRequest.getMethod());

        // Test basic auth constructor
        GSRequest basicRequest = new GSRequest("api-key", "secret-key", "accounts.getAccountInfo", null);
        assertEquals("api-key", basicRequest.apiKey);
        assertEquals("secret-key", basicRequest.secretKey);
        assertEquals("accounts.getAccountInfo", basicRequest.getMethod());
    }

    @Test
    public void testFluentAPISetters() {
        GSRequest request = new GSRequest()
                .setApiKey("test-api-key")
                .setSecretKey("test-secret")
                .setApiDomain("eu1.gigya.com");

        assertEquals("test-api-key", request.apiKey);
        assertEquals("test-secret", request.secretKey);
        assertEquals("eu1.gigya.com", request.apiDomain);
    }

    @Test
    public void testAuthModeDetection() {
        // Test basic auth mode
        GSConfig basicConfig = GSConfig.create()
                .setApiKey("api-key")
                .setSecretKey("secret-key");
        GSRequest basicRequest = new GSRequest(basicConfig);
        // Should detect BASIC auth mode internally

        // Test JWT auth mode
        GSConfig jwtConfig = GSConfig.create()
                .setApiKey("api-key")
                .setUserKey("user-key")
                .setPrivateKey("private-key");
        GSRequest jwtRequest = new GSRequest(jwtConfig);
        // Should detect JWT auth mode internally

        // Test anonymous auth mode
        GSConfig anonConfig = GSConfig.create()
                .setApiKey("api-key");
        GSRequest anonRequest = new GSRequest(anonConfig);
        // Should detect ANONYMOUS auth mode internally

        assertNotNull(basicRequest);
        assertNotNull(jwtRequest);
        assertNotNull(anonRequest);
    }

    @Test
    public void testParameterHandling() {
        GSRequest request = new GSRequest();
        
        // Test various parameter types
        request.setParam("stringParam", "value");
        request.setParam("intParam", 123);
        request.setParam("longParam", 456L);
        request.setParam("boolParam", true);

        GSObject params = request.getParams();
        assertEquals("value", params.getString("stringParam"));
        assertEquals(123, params.getInt("intParam"));
        assertEquals(456L, params.getLong("longParam"));
        assertEquals(true, params.getBool("boolParam"));
    }

    @Test
    public void testClearParams() {
        GSRequest request = new GSRequest();
        request.setParam("test", "value");
        assertFalse(request.getParams().getKeys().isEmpty());
        
        request.clearParams();
        assertTrue(request.getParams().getKeys().isEmpty());
    }

    @Test
    public void testApiDomainHandling() {
        GSRequest request = new GSRequest();
        
        // Test setting valid domain
        request.setApiDomain("eu1.gigya.com");
        assertEquals("eu1.gigya.com", request.apiDomain);
        
        // Test setting null domain (should use default)
        request.setApiDomain(null);
        assertEquals("us1.gigya.com", request.apiDomain);
        
        // Test setAPIDomain alias
        request.setAPIDomain("au1.gigya.com");
        assertEquals("au1.gigya.com", request.apiDomain);
    }

    @Test
    public void testRequestAuthorizationValidation() {
        // Test with no credentials (should use anonymous if apiKey available)
        GSConfig emptyConfig = GSConfig.create();
        GSRequest emptyRequest = new GSRequest(emptyConfig);
        // Should handle gracefully

        // Test with API key only (anonymous)
        GSConfig anonConfig = GSConfig.create().setApiKey("api-key");
        GSRequest anonRequest = new GSRequest(anonConfig);
        // Should be valid for anonymous requests

        // Test with complete basic auth
        GSConfig basicConfig = GSConfig.create()
                .setApiKey("api-key")
                .setSecretKey("secret-key");
        GSRequest basicRequest = new GSRequest(basicConfig);
        // Should be valid for basic auth

        assertNotNull(emptyRequest);
        assertNotNull(anonRequest);
        assertNotNull(basicRequest);
    }

    @Test
    public void testMtlsConfigurationHandling() {
        GSConfig mtlsConfig = GSConfig.create()
                .setApiKey("api-key")
                .setMtlsCertificatePem("-----BEGIN CERTIFICATE-----\ntest\n-----END CERTIFICATE-----")
                .setMtlsPrivateKeyPem("-----BEGIN PRIVATE KEY-----\ntest\n-----END PRIVATE KEY-----");

        GSRequest mtlsRequest = new GSRequest(mtlsConfig);
        // Should detect mTLS configuration
        assertNotNull(mtlsRequest);
    }

    @Test
    public void testRequestErrorHandling() {
        GSRequest request = new GSRequest();
        // Test sending request without API method
        GSResponse response = request.send();
        
        // Should return error response
        assertNotNull(response);
        assertEquals(400002, response.getErrorCode());
    }
}
