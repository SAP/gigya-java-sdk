package com.gigya.auth;

import com.gigya.socialize.GSRequest;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GSAuthMtlsRequest - A request class that uses mutual TLS (mTLS) authentication
 * with client certificates instead of JWT tokens.
 *
 * This class applies client certificates ONLY to connections created by this class,
 * by overriding the configureConnection() hook method.
 */
public class GSAuthMtlsRequest extends GSRequest {

    private static final String DEFAULT_CERTS_DIR = "certs";
    private static final String DEFAULT_KEY_FILE = "highrate-app.key";
    private static final String DEFAULT_CHAIN_FILE = "app-with-partner.pem";

    private String certsDir = DEFAULT_CERTS_DIR;
    private String keyFile = DEFAULT_KEY_FILE;
    private String chainFile = DEFAULT_CHAIN_FILE;
    private final char[] password = System.getenv()
            .getOrDefault("SDK_MTLS_PASS", "password")
            .toCharArray();

    /**
     * Constructor for mTLS request with default certificate paths.
     *
     * @param apiKey     Site api key.
     * @param apiMethod  Request api method (e.g., "accounts.login", "admin.getUserKey").
     */
    public GSAuthMtlsRequest(String apiKey, String apiMethod) {
        super(apiKey, null, null, apiMethod, null, true, null);
    }

    /**
     * Constructor for mTLS request with custom certificate paths.
     * @param apiKey     Site api key.
     * @param apiMethod  Request api method.
     * @param certsDir   Directory containing certificate files.
     * @param keyFile    Private key file name.
     * @param chainFile  Certificate chain file name.
     */
    public GSAuthMtlsRequest(String apiKey, String apiMethod, String certsDir, String keyFile, String chainFile) {
        super(apiKey, null, null, apiMethod, null, true, null);
        if (certsDir != null) this.certsDir = certsDir;
        if (keyFile != null) this.keyFile = keyFile;
        if (chainFile != null) this.chainFile = chainFile;
    }

    public GSAuthMtlsRequest setCertsDir(String certsDir) { this.certsDir = certsDir; return this; }
    public GSAuthMtlsRequest setKeyFile(String keyFile) { this.keyFile = keyFile; return this; }
    public GSAuthMtlsRequest setChainFile(String chainFile) { this.chainFile = chainFile; return this; }

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
        }
    }

    /**
     * Load client certificates and private key from files.
     *
     * @return CertificateBundle containing private key and certificate chain, or null if loading fails
     */
    private CertificateBundle loadCertificates() {
        try {
            // Read PEM files
            String keyPem = Files.readString(Paths.get(certsDir, keyFile), StandardCharsets.UTF_8);
            String chainPem = Files.readString(Paths.get(certsDir, chainFile), StandardCharsets.UTF_8);

            // Parse PEM to Java objects
            PrivateKey privateKey = parsePrivateKeyPkcs8(keyPem);
            X509Certificate[] chain = parseCertificateChain(chainPem);

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
        keyStore.load(null, password);
        keyStore.setKeyEntry("client", bundle.privateKey, password, bundle.chain);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null); // Use default truststore

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return sslContext;
    }

    /**
     * Apply the SSL context to the HTTPS connection and log confirmation.
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
        String content = pem.replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
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

