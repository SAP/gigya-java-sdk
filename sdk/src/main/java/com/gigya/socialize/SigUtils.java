package com.gigya.socialize;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * This class is a utility class with static methods for calculating and validating cryptographic signatures.
 */
public class SigUtils {

    /**
     * Use this method to verify the authenticity of a <a href="http://developers.gigya.com/display/GD/socialize.getUserInfo+REST">socialize.getUserInfo</a> API method response, to make sure it is in fact originating from Gigya, and prevent fraud.
     * The "socialize.getUserInfo" API method response data include the following fields: UID, signatureTimestamp (a timestamp) and UIDSignature (a cryptographic signature).
     * Pass these fields as the corresponding parameters of this method, along with your partner's "<strong>Secret Key</strong>". Your secret key (provided in BASE64 encoding) is located at the bottom of the <a href="http://www.gigya.com/site/partners/wfsocapi.aspx#&amp;&amp;userstate=SiteSetup">Site Setup</a> page on Gigya's website.
     * The return value of the method indicates if the signature is valid (thus, originating from Gigya) or not.
     *
     * @param UID       pass the <em>UID</em> field returned by the "socialize.getUserInfo" API method response
     * @param timestamp pass the <em>signatureTimestamp</em> field returned by the "socialize.getUserInfo" API method response
     * @param secret    your partner's "<strong>Secret Key</strong>", obtained from Gigya's website.
     * @param signature pass the <em>UIDSignature</em> field returned by the "socialize.getUserInfo" API method response
     */
    public static boolean validateUserSignature(String UID, String timestamp, String secret, String signature) throws InvalidKeyException, UnsupportedEncodingException {
        String expectedSig = calcSignature("HmacSHA1", timestamp + "_" + UID, Base64.decode(secret));
        return expectedSig.equals(signature);
    }

    /**
     * Use this method to verify the authenticity of a <a href="http://developers.gigya.com/display/GD/socialize.getUserInfo+REST">socialize.getUserInfo</a> API method response, to make sure it is in fact originating from Gigya, and prevent fraud.
     * The "socialize.getUserInfo" API method response data include the following fields: UID, signatureTimestamp (a timestamp) and UIDSignature (a cryptographic signature).
     * Pass these fields as the corresponding parameters of this method, along with your partner's "<strong>Secret Key</strong>". Your secret key (provided in BASE64 encoding) is located at the bottom of the <a href="http://www.gigya.com/site/partners/wfsocapi.aspx#&amp;&amp;userstate=SiteSetup">Site Setup</a> page on Gigya's website.
     * The return value of the method indicates if the signature is valid (thus, originating from Gigya) or not.
     *
     * @param UID        pass the <em>UID</em> field returned by the "socialize.getUserInfo" API method response
     * @param timestamp  pass the <em>signatureTimestamp</em> field returned by the "socialize.getUserInfo" API method response
     * @param secret     your partner's "<strong>Secret Key</strong>", obtained from Gigya's website.
     * @param signature  pass the <em>UIDSignature</em> field returned by the "socialize.getUserInfo" API method response
     * @param expiration pass the signature expiration time in seconds to validate against the signature timestamp
     */
    public static boolean validateUserSignature(String UID, String timestamp, String secret, String signature, int expiration) throws InvalidKeyException, UnsupportedEncodingException {
        return !signatureTimestampExpired(timestamp, expiration) && validateUserSignature(UID, timestamp, secret, signature);
    }

    /**
     * Use this method to verify the authenticity of a <a href="http://developers.gigya.com/display/GD/socialize.getFriendsInfo+REST">socialize.getFriendsInfo</a> API method response, to make sure it is in fact originating from Gigya, and prevent fraud.
     * The "socialize.getFriendsInfo" API method response data include the following fields: UID, signatureTimestamp (a timestamp) and friendshipSignature (a cryptographic signature).
     * Pass these fields as the corresponding parameters of this method, along with your partner's "<strong>Secret Key</strong>". Your secret key (provided in BASE64 encoding) is located at the bottom of the <a href="http://www.gigya.com/site/partners/wfsocapi.aspx#&amp;&amp;userstate=SiteSetup">Site Setup</a> page on Gigya's website.
     * The return value of the method indicates if the signature is valid (thus, originating from Gigya) or not.
     *
     * @param UID       pass the <em>UID</em> field returned by the "socialize.getFriendsInfo" API method response
     * @param timestamp pass the <em>signatureTimestamp</em> field returned by the "socialize.getFriendsInfo" API method response
     * @param secret    your partner's "<strong>Secret Key</strong>", obtained from Gigya's website.
     * @param signature pass the <em>friendshipSignature</em> field returned by the "socialize.getFriendsInfo" API method response
     */
    public static boolean validateFriendSignature(String UID, String timestamp, String friendUID, String secret, String signature) throws InvalidKeyException, UnsupportedEncodingException {
        String expectedSig = calcSignature("HmacSHA1", timestamp + "_" + friendUID + "_" + UID, Base64.decode(secret));
        return expectedSig.equals(signature);
    }

    /**
     * Use this method to verify the authenticity of a <a href="http://developers.gigya.com/display/GD/socialize.getFriendsInfo+REST">socialize.getFriendsInfo</a> API method response, to make sure it is in fact originating from Gigya, and prevent fraud.
     * The "socialize.getFriendsInfo" API method response data include the following fields: UID, signatureTimestamp (a timestamp) and friendshipSignature (a cryptographic signature).
     * Pass these fields as the corresponding parameters of this method, along with your partner's "<strong>Secret Key</strong>". Your secret key (provided in BASE64 encoding) is located at the bottom of the <a href="http://www.gigya.com/site/partners/wfsocapi.aspx#&amp;&amp;userstate=SiteSetup">Site Setup</a> page on Gigya's website.
     * The return value of the method indicates if the signature is valid (thus, originating from Gigya) or not.
     *
     * @param UID        pass the <em>UID</em> field returned by the "socialize.getFriendsInfo" API method response
     * @param timestamp  pass the <em>signatureTimestamp</em> field returned by the "socialize.getFriendsInfo" API method response
     * @param secret     your partner's "<strong>Secret Key</strong>", obtained from Gigya's website.
     * @param signature  pass the <em>friendshipSignature</em> field returned by the "socialize.getFriendsInfo" API method response
     * @param expiration pass the signature expiration time in seconds to validate against the signature timestamp
     */
    public static boolean validateFriendSignature(String UID, String timestamp, String friendUID, String secret, String signature, int expiration) throws InvalidKeyException, UnsupportedEncodingException {
        return !signatureTimestampExpired(timestamp, expiration) && validateFriendSignature(UID, timestamp, friendUID, secret, signature);
    }


    public static String getOAuth1Signature(String baseString, String secret) throws InvalidKeyException, MalformedURLException, UnsupportedEncodingException {

        byte[] keyBytes = Base64.decode(secret);
        return calcSignature("HmacSHA1", baseString, keyBytes);
    }

    /**
     * This is a utility method for generating a cryptographic signature.
     *
     * @param algorithmName the algorithm for calculating the signature. The options are: "HmacSHA256" or "HmacSHA1"
     * @param text          the string for signing
     * @param key           the key for signing. Use your partner's "Secret Key", obtained from Gigya's website, as the signing key
     */
    private static String calcSignature(String algorithmName, String text, byte[] key) throws InvalidKeyException, UnsupportedEncodingException {
        byte[] textData = text.getBytes("UTF-8");
        SecretKeySpec signingKey = new SecretKeySpec(key, algorithmName);

        Mac mac;
        try {
            mac = Mac.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(textData);

        return Base64.encodeToString(rawHmac, false);
    }

    public static String calcOAuth1BaseString(String httpMethod, String url, GSRequest request) throws MalformedURLException, UnsupportedEncodingException {
        // Normalize the URL per the OAuth requirements
        StringBuilder normalizedUrl = new StringBuilder();
        java.net.URL u = new java.net.URL(url);

        normalizedUrl.append(u.getProtocol().toLowerCase());
        normalizedUrl.append("://");
        normalizedUrl.append(u.getHost().toLowerCase());
        if ((u.getProtocol().toUpperCase().equals("HTTP") && u.getPort() != 80 && u.getPort() != -1) || (u.getProtocol().toUpperCase().equals("HTTPS") && u.getPort() != 443 && u.getPort() != -1)) {
            normalizedUrl.append(':');
            normalizedUrl.append(u.getPort());
        }
        normalizedUrl.append(u.getPath());


        // Create a sorted list of query parameters
        StringBuilder queryString = new StringBuilder();
        for (String key : request.getParams().getKeys()) {
            if (request.urlEncodedParam(key) != null) {
                queryString.append(key);
                queryString.append("="); // URL encoded '='
                queryString.append(request.urlEncodedParam(key));
                queryString.append("&"); // URL encoded '&'
            }
        }
        queryString.deleteCharAt(queryString.length() - 1);    // remove the last ampersand

        // Construct the base string from the HTTP method, the URL and the parameters
        String baseString = httpMethod.toUpperCase() + "&" + GSRequest.UrlEncode(normalizedUrl.toString()) + "&" + GSRequest.UrlEncode(queryString.toString());
        return baseString;

    }

    /**
     * This is a utility method for generating the cookie value of a dynamic session expiration cookie.
     * Use this method as part of implementing dynamic control over login session expiration, in conjunction with assigning the value '-1' to the sessionExpiration parameter of the client side login methods (i.e. showLoginUI / login).
     * Learn more in the <a href="http://developers.gigya.com/display/GD/Security+Guidelines#SecurityGuidelines-ControlSessionExpiration">Control Session Expiration</a> guide.
     *
     * @param glt_cookie       the login token received from Gigya after successful Login. Gigya stores the token in a cookie named: "glt_" + [Your API Key]
     * @param timeoutInSeconds how many seconds until session expiration. For example, if you would like the session to expire in 5 minutes set this parameter to 300.
     * @param secret           your Gigya "Secret Key", is provided, in BASE64 encoding, at the bottom of the  Dashboard page on the Gigya's website.
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     */
    public static String getDynamicSessionSignature(String glt_cookie, int timeoutInSeconds, String secret) throws InvalidKeyException, UnsupportedEncodingException {
        // cookie format: 
        // <expiration time in unix time format_BASE64(HMACSHA1(secret key, <login token>_<expiration time in unix time format>))


        String expirationTimeUnix = String.valueOf(new Date().getTime() / 1000 + timeoutInSeconds);
        String unsignedExpString = glt_cookie + "_" + expirationTimeUnix;
        String signedExpString = calcSignature("HmacSHA1", unsignedExpString, Base64.decode(secret)); // sign the base string using the secret key
        String ret = expirationTimeUnix + '_' + signedExpString;   // define the cookie value

        return ret;
    }

    public static String getDynamicSessionSignatureUserSigned(String glt_cookie, int timeoutInSeconds, String userKey, String secret) throws UnsupportedEncodingException, InvalidKeyException {
        // cookie format:
        // <expiration time in unix time format>_<User Key>_BASE64(HMACSHA1(secret key, <login token>_<expiration time in unix time format>_<User Key>))

        String expirationTimeUnix = String.valueOf(new Date().getTime() / 1000 + timeoutInSeconds);
        String unsignedExpString = glt_cookie + "_" + expirationTimeUnix + "_" + userKey;
        String signedExpString = calcSignature("HmacSHA1", unsignedExpString, Base64.decode(secret)); // sign the base string using the secret key

        return expirationTimeUnix + "_" + userKey + "_" + signedExpString;
    }

    public static boolean signatureTimestampExpired(String signatureTimestamp, int expiration) {
        long timestamp = Long.parseLong(signatureTimestamp);
        long now = System.currentTimeMillis() / 1000; // In seconds.
        return Math.abs(now - timestamp) > expiration;
    }
}
