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
        assertEquals(GSRequest.buildQS(params), "a=" + URL_ENCODED_STRING + "&c=" + URL_COMPLIANT_STRING);
    }
}