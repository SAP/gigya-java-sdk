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
public class MtlsConfigTest extends TestCase {

    private static final String TEST_CERT_PEM = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDXTCCAkWgAwIBAgIJAKJ4hf8sHJK5MA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNV\n" +
            "BAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBX\n" +
            "-----END CERTIFICATE-----";

    private static final String TEST_KEY_PEM = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDp1pbG5043FME/\n" +
            "D6c74xT8yiLJCA2QOXAmJSTSEs+J12Qmdxm7weVypjPY29cY/AnwXtU1rlilkH2P\n" +
            "-----END PRIVATE KEY-----";

    private Path tempCertFile;
    private Path tempKeyFile;

    @Before
    public void setUp() throws IOException {
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
    public void testFromPemWithDefaultPassword() {
        MtlsConfig config = MtlsConfig.fromPem(TEST_CERT_PEM, TEST_KEY_PEM);

        assertNotNull("Config should not be null", config);
        assertNotNull("Password should not be null", config.getPassword());
        assertEquals("Default password should be 'changeit'", "changeit", new String(config.getPassword()));
    }

    @Test
    public void testFromPemWithCustomPassword() {
        char[] customPassword = "myPassword123".toCharArray();
        MtlsConfig config = MtlsConfig.fromPem(TEST_CERT_PEM, TEST_KEY_PEM, customPassword);

        assertNotNull("Config should not be null", config);
        assertNotNull("Password should not be null", config.getPassword());
        assertEquals("Password should match custom password", "myPassword123", new String(config.getPassword()));
    }

    @Test
    public void testFromFilesWithDefaultPassword() {
        MtlsConfig config = MtlsConfig.fromFiles(
                tempCertFile.toString(),
                tempKeyFile.toString()
        );

        assertNotNull("Config should not be null", config);
        assertNotNull("Password should not be null", config.getPassword());
        assertEquals("Default password should be 'changeit'", "changeit", new String(config.getPassword()));
    }

    @Test
    public void testFromFilesWithCustomPassword() {
        char[] customPassword = "filePassword456".toCharArray();
        MtlsConfig config = MtlsConfig.fromFiles(
                tempCertFile.toString(),
                tempKeyFile.toString(),
                customPassword
        );

        assertNotNull("Config should not be null", config);
        assertNotNull("Password should not be null", config.getPassword());
        assertEquals("Password should match custom password", "filePassword456", new String(config.getPassword()));
    }

    @Test
    public void testConstructorWithAllParameters() {
        char[] password = "testPass789".toCharArray();
        MtlsConfig config = new MtlsConfig(
                TEST_CERT_PEM,
                tempCertFile.toString(),
                TEST_KEY_PEM,
                tempKeyFile.toString(),
                password
        );

        assertNotNull("Config should not be null", config);
        assertNotNull("Password should not be null", config.getPassword());
    }

    @Test
    public void testConstructorWithNullPassword() {
        MtlsConfig config = new MtlsConfig(
                TEST_CERT_PEM,
                null,
                TEST_KEY_PEM,
                null,
                null
        );

        assertNotNull("Config should not be null", config);
        assertNotNull("Password should default when null", config.getPassword());
        assertEquals("Default password should be 'changeit'", "changeit", new String(config.getPassword()));
    }

    @Test
    public void testLoadCertificateFromPem() throws IOException {
        MtlsConfig config = MtlsConfig.fromPem(TEST_CERT_PEM, TEST_KEY_PEM);

        String loadedCert = config.loadCertificate();

        assertNotNull("Loaded certificate should not be null", loadedCert);
        assertTrue("Certificate should contain BEGIN marker", loadedCert.contains("BEGIN CERTIFICATE"));
        assertTrue("Certificate should contain END marker", loadedCert.contains("END CERTIFICATE"));
    }

    @Test
    public void testLoadPrivateKeyFromPem() throws IOException {
        MtlsConfig config = MtlsConfig.fromPem(TEST_CERT_PEM, TEST_KEY_PEM);

        String loadedKey = config.loadPrivateKey();

        assertNotNull("Loaded key should not be null", loadedKey);
        assertTrue("Key should contain BEGIN marker", loadedKey.contains("BEGIN PRIVATE KEY"));
        assertTrue("Key should contain END marker", loadedKey.contains("END PRIVATE KEY"));
    }

    @Test
    public void testLoadCertificateFromFile() throws IOException {
        MtlsConfig config = MtlsConfig.fromFiles(
                tempCertFile.toString(),
                tempKeyFile.toString()
        );

        String loadedCert = config.loadCertificate();

        assertNotNull("Loaded certificate should not be null", loadedCert);
        assertTrue("Certificate should match file content", loadedCert.contains("BEGIN CERTIFICATE"));
    }

    @Test
    public void testLoadPrivateKeyFromFile() throws IOException {
        MtlsConfig config = MtlsConfig.fromFiles(
                tempCertFile.toString(),
                tempKeyFile.toString()
        );

        String loadedKey = config.loadPrivateKey();

        assertNotNull("Loaded key should not be null", loadedKey);
        assertTrue("Key should match file content", loadedKey.contains("BEGIN PRIVATE KEY"));
    }

    @Test
    public void testValidationWithValidPemStrings() {
        try {
            MtlsConfig config = MtlsConfig.fromPem(TEST_CERT_PEM, TEST_KEY_PEM);
            config.validate();
            // Should not throw exception
            assertNotNull("Config should be valid", config);
        } catch (Exception e) {
            fail("Validation should pass with valid PEM strings: " + e.getMessage());
        }
    }

    @Test
    public void testValidationWithNullCertPemAndPath() {
        try {
            MtlsConfig config = new MtlsConfig(null, null, TEST_KEY_PEM, null, null);
            config.validate();
            fail("Validation should fail when both cert PEM and path are null");
        } catch (IllegalStateException e) {
            assertTrue("Error message should mention certificate",
                    e.getMessage().toLowerCase().contains("certificate"));
        }
    }

    @Test
    public void testValidationWithNullKeyPemAndPath() {
        try {
            MtlsConfig config = new MtlsConfig(TEST_CERT_PEM, null, null, null, null);
            config.validate();
            fail("Validation should fail when both key PEM and path are null");
        } catch (IllegalStateException e) {
            assertTrue("Error message should mention key",
                    e.getMessage().toLowerCase().contains("key"));
        }
    }

    @Test
    public void testValidationWithEmptyCertPem() {
        try {
            MtlsConfig.fromPem("", TEST_KEY_PEM);
            fail("Validation should fail with empty cert PEM");
        } catch (IllegalStateException e) {
            // Expected
            assertNotNull(e);
        }
    }

    @Test
    public void testValidationWithEmptyKeyPem() {
        try {
            MtlsConfig.fromPem(TEST_CERT_PEM, "");
            fail("Validation should fail with empty key PEM");
        } catch (IllegalStateException e) {
            // Expected
            assertNotNull(e);
        }
    }

    @Test
    public void testValidationWithNonExistentCertFile() {
        try {
            MtlsConfig.fromFiles(
                    "non-existent-cert.pem",
                    tempKeyFile.toString()
            );

            fail("Validation should fail with non-existent cert file");
        } catch (IllegalStateException e) {
            // Expected
            assertNotNull(e);
        }
    }

    @Test
    public void testValidationWithNonExistentKeyFile() {
        try {
            MtlsConfig.fromFiles(
                    tempCertFile.toString(),
                    "non-existent-key.pem"
            );

            fail("Validation should fail with non-existent key file");
        } catch (IllegalStateException e) {
            // Expected
            assertNotNull(e);
        }
    }

    @Test
    public void testPasswordIsClonedNotShared() {
        char[] originalPassword = "testPassword".toCharArray();
        MtlsConfig config = MtlsConfig.fromPem(TEST_CERT_PEM, TEST_KEY_PEM, originalPassword);

        // Modify original password
        originalPassword[0] = 'X';

        char[] retrievedPassword = config.getPassword();

        // Retrieved password should not be affected by modification to original
        assertFalse("Password should be cloned", retrievedPassword[0] == 'X');
        assertEquals("Password should start with 't'", 't', retrievedPassword[0]);
    }

    @Test
    public void testGetPasswordReturnsClone() {
        MtlsConfig config = MtlsConfig.fromPem(TEST_CERT_PEM, TEST_KEY_PEM);

        char[] password1 = config.getPassword();
        char[] password2 = config.getPassword();

        // Modify first retrieved password
        password1[0] = 'X';

        // Second retrieved password should not be affected
        assertFalse("getPassword() should return a clone", password1[0] == password2[0]);
    }

    @Test
    public void testMixedPemAndFilePath() {
        // Certificate from PEM, key from file
        MtlsConfig config1 = new MtlsConfig(
                TEST_CERT_PEM,
                null,
                null,
                tempKeyFile.toString(),
                null
        );
        assertNotNull("Config with cert PEM and key file should be valid", config1);

        // Certificate from file, key from PEM
        MtlsConfig config2 = new MtlsConfig(
                null,
                tempCertFile.toString(),
                TEST_KEY_PEM,
                null,
                null
        );
        assertNotNull("Config with cert file and key PEM should be valid", config2);
    }

    @Test
    public void testBothPemAndPathProvidedPrefersPem() throws IOException {
        // When both PEM and path are provided, PEM should be preferred
        MtlsConfig config = new MtlsConfig(
                TEST_CERT_PEM,
                tempCertFile.toString(),
                TEST_KEY_PEM,
                tempKeyFile.toString(),
                null
        );

        String loadedCert = config.loadCertificate();
        String loadedKey = config.loadPrivateKey();

        // Should load from PEM strings, not files
        assertNotNull("Should load certificate", loadedCert);
        assertNotNull("Should load key", loadedKey);
    }
}

