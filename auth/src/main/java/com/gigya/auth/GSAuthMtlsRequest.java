package com.gigya.auth;

import com.gigya.socialize.GSRequest;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GSAuthMtlsRequest - A request class that uses mutual TLS (mTLS) authentication
 * This class applies client certificates ONLY to connections created by this class,
 * by overriding the configureConnection() hook method.
 */
public class GSAuthMtlsRequest extends GSRequest {

    private final MtlsConfig mtlsConfig;

    /**
     * Constructor for mTLS request.
     * @param apiKey     Site API key.
     * @param apiMethod  Request API method (e.g. "accounts.login").
     * @param mtlsConfig mTLS configuration (PEMs or file paths).
     */
    public GSAuthMtlsRequest(String apiKey, String apiMethod, MtlsConfig mtlsConfig) {
        super(apiKey, null, null, apiMethod, null, true, null);
        if (mtlsConfig == null) {
            this.mtlsConfig = MtlsConfig.fromDefaultLocation();
        } else {
            this.mtlsConfig = mtlsConfig;
        }
        this.mtlsConfig.validate();
    }

    public GSAuthMtlsRequest(String apiKey, String apiMethod) {
        this(apiKey, apiMethod, null);
    }

    @Override
    protected void signRequest(String token, String secret, String httpMethod, String resourceURI) {
        // Api key is required.
        if (token != null) params.put("apiKey", token);
    }

    @Override
    protected boolean evaluateRequestAuthorization() {
        return true;
    }

    /**
     * Override hook method to apply client certificates to HTTPS connections.
     * This method is called by GSRequest.sendRequest() after creating the connection.
     */
    @Override
    protected void configureConnection(URLConnection conn) {
        if (!(conn instanceof HttpsURLConnection)) {
            return;
        }

        try {
            HttpsURLConnection httpsConn = (HttpsURLConnection) conn;

            CertificateBundle bundle = loadCertificates();
            if (bundle == null) {
                throw new IllegalStateException("Failed to load client certificates - cannot proceed with mTLS");
            }

            SSLContext sslContext = createSSLContext(bundle);

            applyCertificatesToConnection(httpsConn, sslContext);

        } catch (Exception e) {
            logger.write("GSAuthMtlsRequest", "Failed to configure mTLS: " + e.getMessage());
            logger.write(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Load client certificates and private key from files.
     *
     * @return CertificateBundle containing private key and certificate chain, or null if loading fails
     */
    private CertificateBundle loadCertificates() {
        try {
            String certPem = mtlsConfig.loadCertificate();
            String keyPem  = mtlsConfig.loadPrivateKey();

            PrivateKey privateKey = parsePrivateKeyPkcs8(keyPem);
            X509Certificate[] chain = parseCertificateChain(certPem);

            // Validate
            if (privateKey == null || chain.length == 0) {
                return null;
            }

            return new CertificateBundle(privateKey, chain);
        } catch (Exception e) {
            logger.write("GSAuthMtlsRequest", "Error loading certificates: " + e.getMessage());
            return null;
        }
    }

    /**
     * Create an SSLContext configured with client certificates and trust settings.
     *
     * @param bundle The certificate bundle containing private key and certificate chain
     * @return Configured SSLContext
     * @throws Exception if SSL context creation fails
     */
    private SSLContext createSSLContext(CertificateBundle bundle) throws Exception {
        // Build KeyStore with client key + certificate chain
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        char[] password = mtlsConfig.getPassword();
        keyStore.load(null, password);
        keyStore.setKeyEntry("client", bundle.privateKey, password, bundle.chain);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return sslContext;
    }

    /**
     * Apply the SSL context to the HTTPS connection.
     *
     * @param httpsConn The HTTPS connection to configure
     * @param sslContext The SSL context with client certificates
     */
    private void applyCertificatesToConnection(HttpsURLConnection httpsConn, SSLContext sslContext) {
        httpsConn.setSSLSocketFactory(sslContext.getSocketFactory());
        httpsConn.setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
    }

    /**
     * Inner class to hold certificate bundle (private key + certificate chain).
     */
    private static class CertificateBundle {
        final PrivateKey privateKey;
        final X509Certificate[] chain;

        CertificateBundle(PrivateKey privateKey, X509Certificate[] chain) {
            this.privateKey = privateKey;
            this.chain = chain;
        }
    }

    // ----------------- Certificate parsing helpers -----------------
    private static PrivateKey parsePrivateKeyPkcs8(String pem) throws Exception {
        String base64Key = trimKey(pem);
        return GSAuthRequestUtils.rsaPrivateKeyFromBase64String(base64Key);
    }

    private static String trimKey(String pem) {
        return pem
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("-----BEGIN RSA PRIVATE KEY-----", "")  // Also handle PKCS#1
                .replaceAll("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
    }

    private static X509Certificate[] parseCertificateChain(String pem) throws Exception {
        Pattern pattern = Pattern.compile("-----BEGIN CERTIFICATE-----(.*?)-----END CERTIFICATE-----", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(pem);
        List<X509Certificate> list = new ArrayList<>();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        while (matcher.find()) {
            String block = matcher.group(1).replaceAll("\\s", "");
            byte[] certBytes = Base64.getDecoder().decode(block);
            X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
            list.add(cert);
        }

        return list.toArray(new X509Certificate[0]);
    }

}

