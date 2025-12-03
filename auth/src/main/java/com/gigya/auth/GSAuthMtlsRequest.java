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
 * GSAuthMtlsRequest - A request class that uses mutual TLS (mTLS) authentication.
 * Accepts client certificate & private key via PEM string or file path (either form is acceptable).
 */
public class GSAuthMtlsRequest extends GSRequest {

    private final MtlsConfig mtlsConfig;

    /**
     * Constructor: provide mTLS configuration via MtlsConfig object.
     *
     * @param apiKey Site API key.
     * @param apiMethod The API method to call (e.g., "accounts.getAccountInfo")
     * @param mtlsConfig The mTLS configuration containing certificate and private key (as PEM strings or file paths)
     */
    public GSAuthMtlsRequest(String apiKey, String apiMethod, MtlsConfig mtlsConfig) {
        super(apiKey, null, null, apiMethod, null, true, null);
        if (mtlsConfig == null) {
            throw new IllegalArgumentException("MtlsConfig cannot be null");
        }
        this.mtlsConfig = mtlsConfig;
        mtlsConfig.validate();
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

    @Override
    protected void configureConnection(URLConnection conn) {
        if (!(conn instanceof HttpsURLConnection)) {
            return;
        }

        CertificateBundle bundle = loadCertificates();
        if (bundle == null) {
            throw new IllegalStateException("Failed to load client certificates - cannot proceed with mTLS");
        }

        try {
            SSLContext sslContext = createSSLContext(bundle);
            applyCertificatesToConnection((HttpsURLConnection) conn, sslContext);
        } catch (Exception e) {
            logger.write("GSAuthMtlsRequest", "Failed to configure mTLS: " + e.getMessage());
            logger.write(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CertificateBundle loadCertificates() {
        try {
            String certPem = mtlsConfig.loadCertificate();
            String keyPem = mtlsConfig.loadPrivateKey();
            PrivateKey privateKey = parsePrivateKeyPkcs8(keyPem);
            X509Certificate[] chain = parseCertificateChain(certPem);

            if (privateKey == null || chain.length == 0) {
                return null;
            }

            return new CertificateBundle(privateKey, chain);
        } catch (Exception e) {
            logger.write("GSAuthMtlsRequest", "Error loading certificates: " + e.getMessage());
            return null;
        }
    }

    private SSLContext createSSLContext(CertificateBundle bundle) throws Exception {
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
                .replaceAll("-----BEGIN RSA PRIVATE KEY-----", "")
                .replaceAll("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
    }

    private static X509Certificate[] parseCertificateChain(String pem) throws Exception {
        Pattern pattern = Pattern.compile("-----BEGIN CERTIFICATE-----(.*?)-----END CERTIFICATE-----", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(pem);
        List<X509Certificate> list = new ArrayList<X509Certificate>();
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

