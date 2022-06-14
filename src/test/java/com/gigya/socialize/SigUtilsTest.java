package com.gigya.socialize;

import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.powermock.api.mockito.PowerMockito.whenNew;


@RunWith(PowerMockRunner.class)
@PrepareForTest({SigUtils.class})
@PowerMockIgnore("javax.crypto.*")
public class SigUtilsTest extends TestCase {

    final String DOMAIN_NAME = "photos.example.net";
    final String METHOD_NAME = "photos";
    final String REQUEST_METHOD = "POST";
    final String CONSUMER_SECRET = "test-consumer-secret";//insert consumer key here
    final String TOKEN_SECRET = "test-token-secret";//insert token secret here
    GSRequest request;
    String apiMethodUrl;
    String sessionSecret;


    @Before
    public void setUp() throws Exception {
        super.setUp();

        JSONObject jsonParams = new JSONObject();
        jsonParams.put("oauth_consumer_key", CONSUMER_SECRET);
        jsonParams.put("oauth_token", TOKEN_SECRET);
        jsonParams.put("oauth_signature_method", "HMAC-SHA1");
        jsonParams.put("oauth_timestamp", "1191242096");
        jsonParams.put("oauth_nonce", "kllo9940pd9333jh");
        jsonParams.put("oauth_version", "1.0");
        jsonParams.put("file", "vacation.jpg");
        jsonParams.put("size", "original");

        request = new GSRequest("", METHOD_NAME, new GSObject(jsonParams));
        apiMethodUrl = "http://" + DOMAIN_NAME + "/" + METHOD_NAME;

        String baseSecret = CONSUMER_SECRET + "&" + TOKEN_SECRET;
        sessionSecret = Base64.encodeToString(baseSecret.getBytes("UTF-8"), false);
    }

    @Test
    public void testOAuth1SigningReturnsExpectedSignature() throws Exception {
        String baseString = SigUtils.calcOAuth1BaseString(REQUEST_METHOD, apiMethodUrl, request);
        assertEquals(SigUtils.getOAuth1Signature(baseString, sessionSecret), "j4/Jwtp11HLWl7KG3QlsczfRRpU=");
    }

    @Test
    public void testGetOAuth1BaseStringFromUrlSchemeWithNonDefaultPortReturnsExpectedString() throws Exception {
        String urlScheme = "http";
        int port = 8080;
        assertEquals(SigUtils.calcOAuth1BaseString(REQUEST_METHOD, getApiMethodUrl(urlScheme, port), request), getExpectedOAuth1BaseString(urlScheme, port));

        urlScheme = "https";
        port = 4433;
        assertEquals(SigUtils.calcOAuth1BaseString(REQUEST_METHOD, getApiMethodUrl(urlScheme, port), request), getExpectedOAuth1BaseString(urlScheme, port));
    }

    @Test
    public void testGetDynamicSessionSignatureUserSigned() throws Exception {
        // Mock
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Date now = sdf.parse("2019-01-01 00:00:00");
        whenNew(Date.class).withAnyArguments().thenReturn(now);
        // Arrange
        final String glt_cookie = "glt_0sadashd913fhe9qsjfjh1fg";
        final String secret = "dGVzdHNlY3JldA==";//insert your secret here
        final String userKey = "testuserkey";//insert your token here
        // Act
        final String signature = SigUtils.getDynamicSessionSignatureUserSigned(glt_cookie, 5, userKey, secret);
        // Assert
        assertEquals("1546300805_alskdlaksd123123_7M7NQjTXz2ERXexIBsks1a2UXB4=", signature);
    }

    private String getApiMethodUrl(String urlScheme, int port) {
        return String.format("%s://%s:%d/%s", urlScheme, DOMAIN_NAME, port, METHOD_NAME);
    }

    private String getExpectedOAuth1BaseString(String urlScheme, int port) {
        return "POST&" + urlScheme + "%3A%2F%2Fphotos.example.net%3A" + port + "%2Fphotos&file%3Dvacation.jpg%26" +
                "oauth_consumer_key%3Dtest-consumer-secret%26oauth_nonce%3Dkllo9940pd9333jh%26oauth_signature_method%3DHMAC-SHA1%26" +
                "oauth_timestamp%3D1191242096%26oauth_token%3Dtest-token-secret%26oauth_version%3D1.0%26size%3Doriginal"; //change the token here to your inserted token.
    }
}