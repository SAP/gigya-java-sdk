package com.gigya.auth;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RunWith(JUnit4.class)
public class GSAuthMtlsRequestTest extends TestCase {

    private static final String TEST_API_KEY = "test-api-key-123";
    private static final String TEST_API_METHOD = "accounts.getAccountInfo";

    private static final String TEST_CERT_PEM = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDXTCCAkWgAwIBAgIJAKJ4hf8sHJK5MA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNV\n" +
            "BAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBX\n" +
            "7kL0mP1fN8kL2vN9pL1mQwIDAQABo1AwTjAdBgNVHQ4EFgQU7q8P5C8R5n5X2L8y\n" +
            "N3mK0fO1qL9MB8GA1UdIwQYMBaAFO6vD+QvEeZ+V9i/Mjd5itHztas/MA0GCSqG\n" +
            "SIb3DQEBCwUAA4IBAQCp0fN6kL8mP0fN7kL1vN8pL0mQ0dK3nN8kP0fN9mL1vN0q\n" +
            "L8nP1fN6kL9mP0fN7kL1vN8pL0mQ0dK4nN9kP1fN0mL2vN1qL9nP2fN7kL0mP1fN\n" +
            "-----END CERTIFICATE-----";

    private static final String TEST_KEY_PEM = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDp1pbG5043FME/\n" +
            "D6c74xT8yiLJCA2QOXAmJSTSEs+J12Qmdxm7weVypjPY29cY/AnwXtU1rlilkH2P\n" +
            "HMtDR0omHfEHDzE3VxCYfigXaMto/qXNR8P/SE3eQ+2movSZWxadj2NOTvFnxof2\n" +
            "83YvWoXyc32QvSY/x83eT0vWdXwf1s3mY/2Q3gvu83qkvyZDR87ic3uQ/183yYvS\n" +
            "6B2YOtoyOWOpjdmtmSCKc+RwjsvgGhbrVlXh6pIPq05FmpU0xcpqYmKBOyTPUA7L\n" +
            "DvVDnIU/c1mMxnmEv1dI+IRcBCgmQS+dCQKBgGYC6IV5HIDf3yR7MUFt2t6jyJA2\n" +
            "+7sCAM1qu+nyCWs+JLMNov67WxCr3vv+WV4vvF+cHSZqArQka43sooSxghCHDO05\n" +
            "ehmWBOVm978TecJ2FxPf2V3tJFJEVh0t9hiOyXuz+sFuOahseLFPcUl+XnQesBv1\n" +
            "aFabfm93kPG9dtra\n" +
            "-----END PRIVATE KEY-----";

    private Path tempCertFile;
    private Path tempKeyFile;

    @Before
    public void setUp() throws IOException {
        // Create temporary certificate files for testing
        tempCertFile = Files.createTempFile("test-cert", ".pem");
        tempKeyFile = Files.createTempFile("test-key", ".pem");

        Files.write(tempCertFile, TEST_CERT_PEM.getBytes());
        Files.write(tempKeyFile, TEST_KEY_PEM.getBytes());
    }

    @After
    public void tearDown() throws IOException {
        // Clean up temporary files
        if (tempCertFile != null && Files.exists(tempCertFile)) {
            Files.delete(tempCertFile);
        }
        if (tempKeyFile != null && Files.exists(tempKeyFile)) {
            Files.delete(tempKeyFile);
        }
    }

    @Test
    public void testConstructorWithMtlsConfigFromFiles() {
        MtlsConfig config = MtlsConfig.fromFiles(
                tempCertFile.toString(),
                tempKeyFile.toString()
        );

        GSAuthMtlsRequest request = new GSAuthMtlsRequest(TEST_API_KEY, TEST_API_METHOD, config);

        assertNotNull("Request should not be null", request);
    }

    @Test
    public void testConstructorWithMtlsConfigFromPem() {
        MtlsConfig config = MtlsConfig.fromPem(TEST_CERT_PEM, TEST_KEY_PEM);

        GSAuthMtlsRequest request = new GSAuthMtlsRequest(TEST_API_KEY, TEST_API_METHOD, config);

        assertNotNull("Request should not be null", request);
    }

    @Test
    public void testConstructorWithNullConfigThrowsException() {
        try {
            new GSAuthMtlsRequest(TEST_API_KEY, TEST_API_METHOD, null);
            fail("Should throw IllegalArgumentException when config is null");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should mention MtlsConfig",
                    e.getMessage().contains("MtlsConfig"));
        }
    }

    @Test
    public void testMultipleRequestsWithSameConfig() {
        MtlsConfig config = MtlsConfig.fromPem(TEST_CERT_PEM, TEST_KEY_PEM);

        // Create multiple requests with the same config
        GSAuthMtlsRequest request1 = new GSAuthMtlsRequest(TEST_API_KEY, "accounts.login", config);
        GSAuthMtlsRequest request2 = new GSAuthMtlsRequest(TEST_API_KEY, "accounts.getAccountInfo", config);
        GSAuthMtlsRequest request3 = new GSAuthMtlsRequest(TEST_API_KEY, "accounts.setAccountInfo", config);

        assertNotNull("First request should not be null", request1);
        assertNotNull("Second request should not be null", request2);
        assertNotNull("Third request should not be null", request3);
    }

    @Test
    public void testConfigWithCustomPassword() {
        char[] customPassword = "myCustomPassword123".toCharArray();
        MtlsConfig config = MtlsConfig.fromPem(TEST_CERT_PEM, TEST_KEY_PEM, customPassword);

        GSAuthMtlsRequest request = new GSAuthMtlsRequest(TEST_API_KEY, TEST_API_METHOD, config);

        assertNotNull("Request with custom password should not be null", request);
    }

    @Test
    public void testConfigWithFilePathsAndCustomPassword() {
        char[] customPassword = "testPassword456".toCharArray();
        MtlsConfig config = MtlsConfig.fromFiles(
                tempCertFile.toString(),
                tempKeyFile.toString(),
                customPassword
        );

        GSAuthMtlsRequest request = new GSAuthMtlsRequest(TEST_API_KEY, TEST_API_METHOD, config);

        assertNotNull("Request with file paths and custom password should not be null", request);
    }

    @Test
    public void testConfigWithInvalidCertPathThrowsException() {
        try {
            MtlsConfig config = MtlsConfig.fromFiles(
                    "non-existent-cert.pem",
                    tempKeyFile.toString()
            );
            new GSAuthMtlsRequest(TEST_API_KEY, TEST_API_METHOD, config);
            fail("Should throw exception when certificate file doesn't exist");
        } catch (Exception e) {
            // Expected
            assertNotNull(e);
        }
    }

    @Test
    public void testConfigWithInvalidKeyPathThrowsException() {
        try {
            MtlsConfig config = MtlsConfig.fromFiles(
                    tempCertFile.toString(),
                    "non-existent-key.pem"
            );
            new GSAuthMtlsRequest(TEST_API_KEY, TEST_API_METHOD, config);
            fail("Should throw exception when key file doesn't exist");
        } catch (Exception e) {
            // Expected
            assertNotNull(e);
        }
    }

    @Test
    public void testConfigWithEmptyCertPemThrowsException() {
        try {
            MtlsConfig config = MtlsConfig.fromPem("", TEST_KEY_PEM);
            new GSAuthMtlsRequest(TEST_API_KEY, TEST_API_METHOD, config);
            fail("Should throw exception when certificate PEM is empty");
        } catch (Exception e) {
            // Expected
            assertNotNull(e);
        }
    }

    @Test
    public void testConfigWithEmptyKeyPemThrowsException() {
        try {
            MtlsConfig config = MtlsConfig.fromPem(TEST_CERT_PEM, "");
            new GSAuthMtlsRequest(TEST_API_KEY, TEST_API_METHOD, config);
            fail("Should throw exception when key PEM is empty");
        } catch (Exception e) {
            // Expected
            assertNotNull(e);
        }
    }

}

