package com.gigya.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static junit.framework.TestCase.*;

@RunWith(PowerMockRunner.class)
public class GSAuthRequestUtilsTest {

    private String getPrivateKey() {
        String rawKey = "PRIVATE_KEY_HERE";
        return stripUnwantedDelimiters(rawKey);
    }

    private String getPublicKey() {
        String rawKey = "PUBLIC_KEY_HERE";
        return stripUnwantedDelimiters(rawKey);
    }

    private String stripUnwantedDelimiters(String raw) {
        return raw.replace("\\r", "").replace("\\n", "");
    }

    @Test
    public void test_rsaPrivateKeyFromBase64StringPKCS8() {
        final String key = getPrivateKey();
        final PrivateKey privateKey = GSAuthRequestUtils.rsaPrivateKeyFromBase64String(key);
        assertNotNull(key);
        assertEquals("RSA", privateKey.getAlgorithm());
    }

    @Test
    public void test_rsaPublicKeyFromBase64String() {
        // Act
        final PublicKey key = GSAuthRequestUtils.rsaPublicKeyFromBase64String(getPublicKey());
        // Assert
        assertNotNull(key);
        assertEquals("RSA", key.getAlgorithm());
    }

    @Test
    public void test_rsaPublicKeyFromJWKString() {
        // Arrange
        final String jwk = "JWK_HERE";
        // Act
        final PublicKey key = GSAuthRequestUtils.rsaPublicKeyFromJWKString(jwk);
        // Assert
        assertNotNull(key);
        assertEquals("RSA", key.getAlgorithm());
    }

    @Test
    public void test_compose() {
        // Act
        final String jws = GSAuthRequestUtils.composeJwt("USER_KEY_HERE", getPrivateKey());
        System.out.println(jws);
        // Assert
        assertNotNull(jws);
    }

    @Test
    public void test_generateTokenAndVerify() {
        // Arrange
        final PublicKey publicKey = GSAuthRequestUtils.rsaPublicKeyFromBase64String(getPublicKey());
        // Act
        final String jws = GSAuthRequestUtils.composeJwt("USER_KEY_HERE", getPrivateKey());
        final Jws<Claims> claimsJws = Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(jws);
        // Assert
        assertNotNull(claimsJws);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void test_generateTokenAndParseWithoutPublicKey() {
        // Act
        final String jws = GSAuthRequestUtils.composeJwt("USER_KEY_HERE", getPrivateKey());
        final Jws<Claims> claimsJws = Jwts.parser().parseClaimsJws(jws);
    }

    @Test
    public void test_generateTokenAndParseHeadersWithoutKey() throws JSONException {
        final String jws = GSAuthRequestUtils.composeJwt("USER_KEY_HERE", getPrivateKey());

        assertNotNull(jws);

        String[] split = jws.split("\\.");
        assertTrue(split.length > 0);

        final String encodedHeaders = split[0];
        final String decodedHeaders = new String(Base64.getDecoder().decode(encodedHeaders.getBytes()));
        assertNotNull(decodedHeaders);
        System.out.println(decodedHeaders);

        final JSONObject jsonObject = new JSONObject(decodedHeaders);
        final String kid = jsonObject.getString("kid");

        assertNotNull(kid);
        System.out.println(kid);
    }

    @Test
    public void test_fetchPublicJWK() {

        final String jwk = GSAuthRequestUtils.fetchPublicJWK(
                "KID_HERE",
                "API_KEY_HERE",
                "API_DOMAIN_HERE");

        assertNotNull(jwk);
    }

    @Test
    public void test_fetchPublicJWK_withWrongKid() {

        final String jwk = GSAuthRequestUtils.fetchPublicJWK(
                "KID_HERE",
                "API_KEY_HERE",
                "API_DOMAIN_HERE");

        assertNull(jwk);
    }

}
