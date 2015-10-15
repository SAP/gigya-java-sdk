package com.gigya.socialize;

import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class SigUtilsTest extends TestCase {

    final String DOMAIN_NAME = "photos.example.net";
    final String METHOD_NAME = "photos";
    final String REQUEST_METHOD = "POST";
    final String CONSUMER_SECRET = "kd94hf93k423kf44";
    final String TOKEN_SECRET = "pfkkdhi9sl3r4s00";
    final String SIGNATURE = "wPkvxykrw+BTdCcGqKr+3I+PsiM=";
    GSRequest request;
    String apiMethodUrl;
    String sessionSecret;


    @Before
    public void setUp() throws Exception {
        super.setUp();

        JSONObject jsonParams = new JSONObject();
        jsonParams.put("oauth_consumer_key", "dpf43f3p2l4k3l03");
        jsonParams.put("oauth_token", "nnch734d00sl2jdk");
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
        assertEquals(SigUtils.getOAuth1Signature(baseString, sessionSecret), SIGNATURE);
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

    private String getApiMethodUrl(String urlScheme, int port) {
        return String.format("%s://%s:%d/%s", urlScheme, DOMAIN_NAME, port, METHOD_NAME);
    }

    private String getExpectedOAuth1BaseString(String urlScheme, int port) {
        return "POST&" + urlScheme + "%3A%2F%2Fphotos.example.net%3A" + port + "%2Fphotos&file%3Dvacation.jpg%26" +
                "oauth_consumer_key%3Ddpf43f3p2l4k3l03%26oauth_nonce%3Dkllo9940pd9333jh%26oauth_signature_method%3DHMAC-SHA1%26" +
                "oauth_timestamp%3D1191242096%26oauth_token%3Dnnch734d00sl2jdk%26oauth_version%3D1.0%26size%3Doriginal";
    }
}