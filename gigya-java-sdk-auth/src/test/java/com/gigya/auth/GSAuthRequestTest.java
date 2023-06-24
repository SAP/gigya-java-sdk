package com.gigya.auth;

import com.gigya.socialize.GSResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(PowerMockRunner.class)
public class GSAuthRequestTest {

    @Test
    public void test_authorizationRequest() {

        // Arrange
        final String USER_KEY = "USER_KEY_HERE";
        final String API_KEY = "API_KEY_HERE";
        final String PRIVATE_KEY = "PRIVATE_KEY_HERE";

        // Act
        final GSAuthRequest request = new GSAuthRequest(USER_KEY, PRIVATE_KEY, API_KEY, "accounts.getAccountInfo");
        request.setParam("UID", "UID_HERE");
        request.setAPIDomain("API_DOMAIN_HERE");
        final GSResponse response = request.send();

        System.out.println(response.getResponseText());

        // Assert
        assertEquals(0, response.getErrorCode());
    }

    @Test
    public void test_verifyIdToken() {

        // Arrange
        final String API_KEY = "API_KEY_HERE";
        // Need to update the id token to a valid one prior to test.
        final String idToken = "ID_TOKEN_HERE";
        final String jwk = "JWK_HERE";

        // Act
        GSAuthRequestUtils.addToPublicKeyCache("API_DOMAIN_HERE", jwk);
        final String UID = GSAuthRequestUtils.validateSignature(idToken, API_KEY, "API_DOMAIN_HERE");

        // Assert
        assertEquals("ASSESRION_HERE", UID);
    }

    @Test
    public void test_validateSignature_with_expired_idToken() {

        // Arrange
        final String API_KEY = "API_KEY_HERE";
        final String expiredIdToken = "EXPIRED_ID_TOKEN_HERE";
        final String jwk = "JWK_HERE";

        // Act
        GSAuthRequestUtils.addToPublicKeyCache("API_DOMAIN_HERE", jwk);
        final String UID = GSAuthRequestUtils.validateSignature(expiredIdToken, API_KEY, "API_DOMAIN_HERE");

        // Assert
        assertNull(UID);
    }


}
