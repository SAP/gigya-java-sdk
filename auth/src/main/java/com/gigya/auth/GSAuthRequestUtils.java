package com.gigya.auth;

import com.gigya.socialize.GSArray;
import com.gigya.socialize.GSLogger;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class GSAuthRequestUtils {

    public static final String VERSION = "java_auth_1.0.0";

    public static GSLogger logger = new GSLogger();

    /**
     * Caching the public keys.
     * Key - data center (lower cased).
     * Value - public key jwk.
     */
    private static Map<String, String> publicKeysCache = new HashMap<>();

    /**
     * Explicitly add a public jwk to cache.
     *
     * @param dataCenterKey Data center as the key.
     * @param jwk           JWK value.
     */
    public static void addToPublicKeyCache(String dataCenterKey, String jwk) {
        publicKeysCache.put(dataCenterKey, jwk);
    }

    /**
     * Clear public key cache.
     */
    public static void clearPublicKeysCache() {
        publicKeysCache.clear();
    }

    /**
     * Clear cached public key from public keys cache.
     *
     * @param dataCenter Data center key.
     */
    public static void clearPublicKeysCache(String dataCenter) {
        publicKeysCache.remove(dataCenter);
    }

    /**
     * Trim .pem style keys from delimiters and wrappers.
     *
     * @param raw Raw .pem style string.
     * @return Stripped base64 encoded key.
     */
    private static String trimKey(String raw) {
        return raw
                .replace("-----BEGIN PRIVATE KEY-----", "") //PKCS#8
                .replace("-----END PRIVATE KEY-----", "") //PKCS#8
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\r", "")
                .replace("\n", "")
                .replace("\\n", "");
    }

    /**
     * Generate an RSA private key instance from given Base64 encoded String.
     *
     * @param encodedPrivateKey Base64 encoded private key String resource (RSA - PKCS#8).
     * @return Generated private key instance.
     */
    static PrivateKey rsaPrivateKeyFromBase64String(String encodedPrivateKey) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(
                    trimKey(encodedPrivateKey)));
            return kf.generatePrivate(keySpecPKCS8);
        } catch (Exception ex) {
            logger.write("Failed to generate RSA private key from encoded string");
            logger.write(ex);
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Generate an RSA public key instance from given Base64 encoded String.
     *
     * @param encodedPublicKey ase64 encoded public key String resource.
     * @return Generated public key instance.
     */
    static PublicKey rsaPublicKeyFromBase64String(String encodedPublicKey) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(
                    trimKey(encodedPublicKey).getBytes()));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception ex) {
            logger.write("Failed to generate RSA public key from encoded string");
            logger.write(ex);
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Generate an RSA public key instance from JWK String.
     *
     * @param jwk JWK String.
     * @return Generated public key instance.
     */
    static PublicKey rsaPublicKeyFromJWKString(String jwk) {
        try {
            // JWK to json.
            JSONObject jsonObject = new JSONObject(jwk);
            final String n = jsonObject.getString("n");
            final String e = jsonObject.getString("e");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n)); //n
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e)); //e
            return kf.generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (Exception ex) {
            logger.write(ex);
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Compost a JWT given account userKey and privateKey.
     *
     * @param userKey    Account user key.
     * @param privateKey Account Base64 encoded private key.
     * @return Generated JWT String.
     */
    public static String composeJwt(String userKey, String privateKey) {

        // #1 - Decode RSA private key (PKCS#1).
        final PrivateKey key = rsaPrivateKeyFromBase64String(privateKey);
        if (key == null) {
            logger.write("Failed to instantiate private key from Base64");
            // Key generation failed.
            return null;
        }

        // #2 - Add JWT headers.
        final Map<String, Object> header = new HashMap<>();
        header.put("alg", "RS256");
        header.put("typ", "JWT");
        header.put("kid", userKey);

        // #3 - Compose & sign Jwt.
        return Jwts.builder()
                .setHeaderParams(header)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime())
                .signWith(key, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * Verify JWT given apiKey and secret.
     * ApiKey and secret are required to perform a GSRequest to fetch the DC public key.
     * <p>
     * Verify Gigya Id Token.
     *
     * @param jwt       Id token.
     * @param apiKey    Client ApiKey.
     * @param apiDomain Api domain.
     * @return UID field if validation is successful.
     */
    public static String validateSignature(String jwt, String apiKey, String apiDomain) {

        final String kid = getKidFromJWSHeader(jwt);
        if (kid == null) {
            logger.write("Failed to parse kid header");
            return null;
        }

        String publicJWK = null;

        // Try to fetch from cache.
        if (publicKeysCache.containsKey(apiDomain.toLowerCase())) {
            publicJWK = publicKeysCache.get(apiDomain.toLowerCase());
        }

        if (publicJWK == null) {
            // None in cache - fetch from network.
            publicJWK = fetchPublicJWK(kid, apiKey, apiDomain);
        }

        if (publicJWK == null) {
            // JWT not available.
            logger.write("Failed to fetch jwk public key");
            return null;
        }

        final PublicKey publicKey = GSAuthRequestUtils.rsaPublicKeyFromJWKString(publicJWK);
        if (publicKey == null) {
            logger.write("Failed to instantiate PublicKey instance from jwk");
            return null;
        }

        // Update public key cache only when it was successfully generated. Useless otherwise.
        publicKeysCache.put(apiDomain.toLowerCase(), publicJWK);

        return verifyJwt(jwt, apiKey, publicKey);
    }

    /**
     * Try to fetch the "kid" field from a jwt header.
     *
     * @param jws Json web token.
     * @return Kid field from token header.
     */
    static String getKidFromJWSHeader(String jws) {
        String[] split = jws.split("\\.");
        if (split.length > 0) {
            final String encodedHeaders = split[0];
            final String decodedHeaders = new String(Base64.getDecoder().decode(encodedHeaders.getBytes()));

            // Parse json headers. Fetch kid fields.
            try {
                final JSONObject jsonObject = new JSONObject(decodedHeaders);
                return jsonObject.getString("kid");
            } catch (JSONException e) {
                logger.write("Failed to parse jwk headers");
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Fetch available public key JWK representation validated by the "kid".
     *
     * @param kid       validation field.
     * @param apiKey    Site ApiKey.
     * @param apiDomain Data center.
     * @return Validated JWK.
     */
    static String fetchPublicJWK(String kid, String apiKey, String apiDomain) {

        // Fetch the public key using endpoint "accounts.getJWTPublicKey".
        final GSAnonymousRequest request = new GSAnonymousRequest(apiKey, apiDomain, "accounts.getJWTPublicKey");
        request.setParam("V2", true);

        final GSResponse response = request.send();
        if (response.getErrorCode() == 0) {

            final GSArray keys = response.getArray("keys", null);
            if (keys == null) {
                logger.write("Failed to obtain JWK from response data");
                return null;
            }
            if (keys.length() == 0) {
                logger.write("Failed to obtain JWK from response data - data is empty");
                return null;
            }

            for (Object key : keys) {
                if (key instanceof GSObject) {
                    final String jwkKid = ((GSObject) key).getString("kid", null);
                    if (jwkKid != null && jwkKid.equals(kid)) {
                        return ((GSObject) key).toJsonString();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Verify JWT given public key instance & api key constraints.
     *
     * @param jwt    JWT token to verify.
     * @param apiKey Account ApiKey.
     * @return UID field (jwt subject) if verified.
     */
    static String verifyJwt(String jwt, String apiKey, PublicKey key) {
        try {
            // #1 - Parse token. Signing key must be available.
            final Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(jwt);

            // #2 - Verify JWT provided api key with input api key.
            final String issuer = claimsJws.getBody().getIssuer();
            final String validIssuer = "https://fidm.gigya.com/jwt/" + apiKey;
            if (issuer != null && !issuer.equals(validIssuer)) {
                logger.write("JWT verification failed - apiKey does not match");
                return null;
            }

            // #3 - Verify current time is between iat & exp. Add 120 seconds grace period.
            final long iat = claimsJws.getBody().get("iat", Long.class);
            final long exp = claimsJws.getBody().get("exp", Long.class);
            final long skew = 120; // Seconds.
            final long currentTimeInUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000;  // UTC (seconds).
            if (!(currentTimeInUTC >= iat && (currentTimeInUTC <= exp + skew))) {
                logger.write("JWT verification failed - expired");
                return null;
            }

            // #4 - Fetch the UID field - subject field of the jwt.
            final String UID = claimsJws.getBody().getSubject();
            if (UID != null) {
                return UID;
            }
        } catch (Exception e) {
            logger.write("Failed to verify jwt with exception");
            logger.write(e);
            e.printStackTrace();
        }
        return null;
    }
}
