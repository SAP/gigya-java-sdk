package com.gigya.socialize;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Unified GSRequest class supporting multiple authentication methods:
 * - Basic authentication (apiKey + secretKey)
 * - JWT authentication (userKey + privateKey)
 * - Anonymous authentication (apiKey only)
 * - mTLS authentication (apiKey + client certificates)
 * - OAuth token authentication (accessToken)
 * 
 * Authentication is configured via GSConfig or constructor parameters.
 */
public class GSRequest {
    public static final String VERSION = "java_3.2.4";

    public static boolean ENABLE_CONNECTION_POOLING = true;

    protected static long timestampOffsetSec = 0;
    private static Random randomGenerator = new Random();
    private static final String DEFAULT_API_DOMAIN = "us1.gigya.com";

    // Configuration and authentication
    private GSConfig config;
    private AuthMode authMode;
    
    // Request properties
    protected String host;
    protected String path;
    protected String accessToken;
    protected String apiKey;
    protected String secretKey;
    protected String userKey;
    protected String privateKey;
    protected GSObject params;
    private GSObject urlEncodedParams;
    private Map<String, String> additionalHeaders = new HashMap<String, String>();
    protected boolean useHTTPS = true;
    protected boolean isLoggedIn;
    protected boolean isRetry = false;
    protected String apiMethod;
    protected String apiDomain = DEFAULT_API_DOMAIN;
    protected String hostOverride = null;
    protected String format;

    protected GSLogger logger = new GSLogger();
    private Proxy proxy = null;

    // Authentication modes
    public enum AuthMode {
        BASIC,      // apiKey + secretKey
        JWT,        // userKey + privateKey + apiKey
        ANONYMOUS,  // apiKey only
        MTLS,       // apiKey + client certificates
        OAUTH       // accessToken
    }

    /**
     * Default constructor - uses GSConfig singleton for configuration
     */
    public GSRequest() {
        this.config = GSConfig.getInstance();
        initializeFromConfig();
    }

    /**
     * Constructor with custom configuration
     */
    public GSRequest(GSConfig config) {
        this.config = config != null ? config : GSConfig.getInstance();
        initializeFromConfig();
    }

    /**
     * Constructor with API method - uses GSConfig singleton
     */
    public GSRequest(String apiMethod) {
        this();
        this.apiMethod = apiMethod;
    }

    /**
     * Constructor with custom config and API method
     */
    public GSRequest(GSConfig config, String apiMethod) {
        this(config);
        this.apiMethod = apiMethod;
    }

    // Legacy constructors for backward compatibility
    public GSRequest(String accessToken, String apiMethod) {
        this();
        this.accessToken = accessToken;
        this.apiMethod = apiMethod;
        this.authMode = AuthMode.OAUTH;
    }

    public GSRequest(String accessToken, String apiMethod, GSObject clientParams) {
        this(accessToken, apiMethod);
        setParams(clientParams);
    }

    public GSRequest(String apiKey, String secretKey, String apiMethod, GSObject clientParams) {
        this();
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.apiMethod = apiMethod;
        this.authMode = AuthMode.BASIC;
        setParams(clientParams);
    }

    public GSRequest(String apiKey, String secretKey, String apiMethod, boolean useHTTPS) {
        this(apiKey, secretKey, apiMethod, null);
        this.useHTTPS = useHTTPS;
    }

    public GSRequest(String apiKey, String secretKey, String apiMethod) {
        this(apiKey, secretKey, apiMethod, null);
    }

    public GSRequest(String apiKey, String secretKey, String apiMethod, GSObject clientParams, boolean useHTTPS) {
        this(apiKey, secretKey, apiMethod, clientParams);
        this.useHTTPS = useHTTPS;
    }

    public GSRequest(String apiKey, String secretKey, String accessToken, String apiMethod, GSObject clientParams, boolean useHTTPS) {
        this();
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.accessToken = accessToken;
        this.apiMethod = apiMethod;
        this.useHTTPS = useHTTPS;
        setParams(clientParams);
        determineAuthMode();
    }

    public GSRequest(String apiKey, String secretKey, String apiMethod, GSObject clientParams, boolean useHTTPS, String userKey) {
        this(apiKey, secretKey, null, apiMethod, clientParams, useHTTPS);
        this.userKey = userKey;
        determineAuthMode();
    }

    public GSRequest(String apiKey, String secretKey, String accessToken, String apiMethod, GSObject clientParams, boolean useHTTPS, String userKey) {
        this(apiKey, secretKey, accessToken, apiMethod, clientParams, useHTTPS);
        this.userKey = userKey;
        determineAuthMode();
    }

    /**
     * Initialize request from GSConfig
     */
    private void initializeFromConfig() {
        if (config != null) {
            this.apiKey = config.getApiKey();
            this.secretKey = config.getSecretKey();
            this.userKey = config.getUserKey();
            this.privateKey = config.getPrivateKey();
            this.apiDomain = config.getApiDomain() != null ? config.getApiDomain() : DEFAULT_API_DOMAIN;
        }
        
        if (params == null) {
            params = new GSObject();
        }
        
        determineAuthMode();
    }

    /**
     * Determine authentication mode based on available credentials
     */
    private void determineAuthMode() {
        if (accessToken != null) {
            authMode = AuthMode.OAUTH;
        } else if (config != null && config.hasMtlsAuth()) {
            authMode = AuthMode.MTLS;
        } else if (config != null && config.hasJwtAuth()) {
            authMode = AuthMode.JWT;
        } else if (config != null && config.hasBasicAuth()) {
            authMode = AuthMode.BASIC;
        } else if (config != null && config.hasAnonymousAuth()) {
            authMode = AuthMode.ANONYMOUS;
        } else if (userKey != null && privateKey != null && apiKey != null) {
            authMode = AuthMode.JWT;
        } else if (apiKey != null && secretKey != null) {
            authMode = AuthMode.BASIC;
        } else if (apiKey != null) {
            authMode = AuthMode.ANONYMOUS;
        } else {
            authMode = AuthMode.ANONYMOUS; // Default fallback
        }
    }

    // Fluent API setters
    public GSRequest setApiKey(String apiKey) {
        this.apiKey = apiKey;
        determineAuthMode();
        return this;
    }

    public GSRequest setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        determineAuthMode();
        return this;
    }

    public GSRequest setUserKey(String userKey) {
        this.userKey = userKey;
        determineAuthMode();
        return this;
    }

    public GSRequest setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        determineAuthMode();
        return this;
    }

    public GSRequest setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        determineAuthMode();
        return this;
    }

    public GSRequest setApiDomain(String apiDomain) {
        if (apiDomain != null) {
            this.apiDomain = apiDomain;
        } else {
            this.apiDomain = DEFAULT_API_DOMAIN;
        }
        return this;
    }

    /**
     * Allows passing an existing GSLogger
     */
    public void setLogger(GSLogger logger) {
        if (logger != null) {
            this.logger.write(logger.toString());
        }
    }

    /**
     * Retrieves the logger object
     */
    public GSLogger getLogger() {
        return this.logger;
    }

    public void setMethod(String apiMethod) {
        this.apiMethod = apiMethod;
    }

    /**
     * @return API Method of the request
     */
    public String getMethod() {
        return this.apiMethod;
    }

    /**
     * Clear request parameters
     */
    public void clearParams() {
        this.params = new GSObject();
        this.urlEncodedParams = null;
    }

    /**
     * Sets a request parameter with a value
     */
    public void setParam(String key, String value) {
        params.put(key, value);
    }

    public void setParam(String key, int value) {
        params.put(key, value);
    }

    public void setParam(String key, long value) {
        params.put(key, value);
    }

    public void setParam(String key, boolean value) {
        params.put(key, value);
    }

    public void setParam(String key, GSObject value) {
        params.put(key, value);
    }

    public void setParam(String key, GSArray value) {
        params.put(key, value);
    }

    /**
     * Returns a GSObject object containing the parameters of this request
     */
    public GSObject getParams() {
        return params;
    }

    /**
     * Sets a proxy
     */
    public void setProxy(Proxy p) {
        this.proxy = p;
    }

    public void setUseHTTPS(boolean useHTTPS) {
        this.useHTTPS = useHTTPS;
    }

    /**
     * Sets a GSObject object containing the parameters of this request
     */
    public void setParams(GSObject clientParams) {
        if (clientParams == null)
            this.params = new GSObject();
        else
            this.params = clientParams.clone();
    }

    /**
     * Sets the domain used for making API calls
     */
    public void setAPIDomain(String apiDomain) {
        setApiDomain(apiDomain);
    }

    public void setHostOverride(String host) {
        this.hostOverride = host;
    }

    /**
     * Sends the request synchronously
     */
    public GSResponse send() {
        return send(-1);
    }

    /**
     * Sends the request synchronously with timeout
     */
    public GSResponse send(int timeoutMS) {
        if (this.apiMethod == null) {
            return new GSResponse(null, this.params, 400002, "API method not specified", logger);
        }

        if (this.apiMethod.startsWith("/"))
            this.apiMethod = this.apiMethod.replaceFirst("/", "");

        if (this.apiMethod.indexOf(".") == -1) {
            this.host = "socialize." + apiDomain;
            this.path = "/socialize." + apiMethod;
        } else {
            String[] tokens = apiMethod.split("\\.");
            this.host = tokens[0] + "." + apiDomain;
            this.path = "/" + apiMethod;
        }

        // use "_host" to override domain, if available
        this.host = this.params.getString("_host", this.host);
        this.params.remove("_host");

        this.format = this.params.getString("format", "json");
        setParam("format", this.format);

        logger.write("apiKey", apiKey);
        logger.write("userKey", userKey);
        logger.write("apiMethod", apiMethod);
        logger.write("authMode", authMode != null ? authMode.toString() : "NONE");
        logger.write("params", params);
        logger.write("useHTTPS", useHTTPS);

        // Evaluate request authorization conditions
        if (!evaluateRequestAuthorization()) {
            return new GSResponse(this.apiMethod, this.params, 400002, "Insufficient authentication credentials", logger);
        }

        try {
            GSResponse res = sendRequest("POST", this.host, this.path,
                    params, apiKey, secretKey, this.useHTTPS, this.isLoggedIn,
                    timeoutMS);
            // if error code indicates timestamp expiration, retry the request
            if (res.getErrorCode() == 403002 && !isRetry) {
                isRetry = true;
                params.remove("sig");
                return send();
            } else {
                return res;
            }
        } catch (InvalidKeyException exKey) {
            return new GSResponse(this.apiMethod, this.params, 400006,
                    "Invalid parameter value:" + exKey.getMessage(), logger);
        } catch (UnsupportedEncodingException exEncoding) {
            return new GSResponse(this.apiMethod, this.params, 400006,
                    "Invalid parameter value: " + exEncoding.getMessage(),
                    logger);
        } catch (IllegalArgumentException exArg) {
            return new GSResponse(this.apiMethod, this.params, 400006,
                    "Invalid parameter value: " + exArg.getMessage(), logger);
        } catch (SocketTimeoutException exTimeout) {
            return new GSResponse(this.apiMethod, this.params, 504002,
                    "Request Timeout", logger);
        } catch (Exception ex) {
            logger.write(ex);
            return new GSResponse(this.apiMethod, this.params, 500000,
                    ex.toString(), logger);
        }
    }

    /**
     * Evaluate request authorization based on authentication mode
     */
    protected boolean evaluateRequestAuthorization() {
        switch (authMode) {
            case OAUTH:
                return this.accessToken != null;
            case BASIC:
                return this.apiKey != null && this.secretKey != null;
            case JWT:
                return this.userKey != null && this.privateKey != null && this.apiKey != null;
            case ANONYMOUS:
                return this.apiKey != null;
            case MTLS:
                return this.apiKey != null && (config != null && config.hasMtlsConfig());
            default:
                return this.accessToken != null || this.secretKey != null || 
                       (this.userKey != null && this.apiKey != null);
        }
    }

    /**
     * Sends the request asynchronously
     */
    public void send(GSResponseListener listener) {
        send(listener, null);
    }

    /**
     * Sends the request asynchronously with context
     */
    public void send(final GSResponseListener listener, final Object context) {
        Runnable r = new Runnable() {
            public void run() {
                GSResponse res = send();
                if (listener != null) {
                    listener.onGSResponse(apiMethod, res, context);
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    /**
     * Static utility: Converts a GSObject to a query string
     */
    public static String buildQS(GSObject params) {
        StringBuilder req = new StringBuilder();
        String val;
        for (String key : params.getKeys()) {
            val = UrlEncode(params.getString(key, null));
            req.append(key);
            req.append('=');
            req.append(val);
            req.append('&');
        }
        if (req.length() > 0)
            req.deleteCharAt(req.length() - 1);

        return req.toString();
    }

    private String buildQS() {
        StringBuilder req = new StringBuilder();
        String val;
        for (String key : params.getKeys()) {
            val = urlEncodedParam(key);
            req.append(key);
            req.append('=');
            req.append(val);
            req.append('&');
        }
        if (req.length() > 0)
            req.deleteCharAt(req.length() - 1);

        return req.toString();
    }

    /**
     * Sign request based on authentication mode
     */
    protected void signRequest(String token, String secret, String httpMethod, String resourceURI)
            throws UnsupportedEncodingException, InvalidKeyException, MalformedURLException {
        
        switch (authMode) {
            case OAUTH:
                if (this.accessToken != null) {
                    params.put("oauth_token", this.accessToken);
                }
                break;
                
            case JWT:
                // Compose JWT and add to request header
                final String jwt = GSAuthUtils.composeJwt(this.userKey, this.privateKey);
                if (jwt == null) {
                    logger.write("Failed to generate authorization JWT");
                    throw new InvalidKeyException("Failed to generate JWT");
                }
                addHeader("Authorization", "Bearer " + jwt);
                // API key is required
                if (token != null) {
                    params.put("apiKey", token);
                }
                break;
                
            case ANONYMOUS:
                // Only API key required
                if (this.apiKey != null) {
                    params.put("apiKey", this.apiKey);
                }
                break;
                
            case MTLS:
                // API key is required for mTLS, certificates handled in configureConnection
                if (token != null) {
                    params.put("apiKey", token);
                }
                break;
                
            case BASIC:
            default:
                // Original signing logic for basic authentication
                if (this.accessToken != null) {
                    params.put("oauth_token", this.accessToken);
                } else {
                    if (!params.containsKey("oauth_token") && token != null) {
                        params.put("apiKey", token);
                    }

                    if (this.userKey != null)
                        params.put("userKey", this.userKey);

                    if (secret != null) {
                        String timestamp = Long.toString((System
                                .currentTimeMillis() / 1000)
                                + timestampOffsetSec);

                        String nonce = System.currentTimeMillis()
                                + "_"
                                + randomGenerator.nextInt();

                        params.put("timestamp", timestamp);
                        params.put("nonce", nonce);

                        String baseString = SigUtils.calcOAuth1BaseString(
                                httpMethod, resourceURI, this);
                        logger.write("baseString", baseString);

                        String signature = SigUtils.getOAuth1Signature(
                                baseString, secret);

                        params.put("sig", signature);
                        logger.write("sig", signature);
                    }
                }
                break;
        }
    }

    /**
     * Send the actual HTTP/S request
     */
    protected GSResponse sendRequest(String httpMethod,
                                     String domain,
                                     String path,
                                     GSObject params,
                                     String token,
                                     String secret,
                                     boolean useHTTPS,
                                     boolean isLoggedIn,
                                     int timeoutMS)
            throws Exception {
        long start = new Date().getTime();
        OutputStreamWriter wr = null;
        BufferedReader rd = null;
        StringBuilder res = new StringBuilder();
        URLConnection conn = null;
        try {

            if (this.hostOverride != null)
                domain = hostOverride;

            String protocol = (useHTTPS || (accessToken != null)) ? "https"
                    : "http";
            String resourceURI = protocol + "://" + domain + path;

            setParam("httpStatusCodes", "false");
            if (!params.containsKey("sdk"))
                setParam("sdk", GSRequest.VERSION);

            logger.write("sdk", params.getString("sdk"));

            // Sign the request based on auth mode
            signRequest(token, secret, httpMethod, resourceURI);

            String data = this.buildQS();
            logger.write("post_data", data);

            URL url = new URL(resourceURI);
            logger.write("url", url);

            if (proxy == null)
                conn = url.openConnection();
            else
                conn = url.openConnection(proxy);

            // Configure connection (e.g., add client certificates for mTLS)
            configureConnection(conn);

            if (timeoutMS != -1) {
                conn.setConnectTimeout(timeoutMS);
                conn.setReadTimeout(timeoutMS);
            }

            // Add additional custom headers
            if (additionalHeaders != null) {
                for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            conn.setRequestProperty("Accept-Encoding", "gzip");

            if (GSRequest.ENABLE_CONNECTION_POOLING) {
                conn.setRequestProperty("X-Connection", "Keep-Alive");
            } else {
                conn.setRequestProperty("connection", "close");
            }

            conn.setDoOutput(true);
            ((HttpURLConnection) conn).setRequestMethod(httpMethod);

            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            int responseStatusCode = ((HttpURLConnection) conn).getResponseCode();
            boolean badRequest = (responseStatusCode >= HttpURLConnection.HTTP_BAD_REQUEST);
            InputStream input;

            if (badRequest)
                input = ((HttpURLConnection) conn).getErrorStream();
            else
                input = conn.getInputStream();

            if ("gzip".equals(conn.getContentEncoding())) {
                input = new GZIPInputStream(input);
            }
            rd = new BufferedReader(new InputStreamReader(input, "UTF-8"));

            String line;
            while ((line = rd.readLine()) != null) {
                res.append(line);
            }

            logger.write("server", conn.getHeaderField("x-server"));
            logger.write("raw_response", res.toString());

            // calc timestamp offset
            String dateHeader = conn.getHeaderField("Date");
            if (dateHeader != null) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat(
                            "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
                    Date serverDate = format.parse(dateHeader);
                    timestampOffsetSec = (serverDate.getTime() - System
                            .currentTimeMillis()) / 1000;
                } catch (Exception ex) {
                }
            }
            long end = new Date().getTime();
            logger.write("request_duration", end - start);

            GSResponse gsr = new GSResponse(this.apiMethod, res.toString(), logger);
            gsr.headers = conn.getHeaderFields();

            wr.close();
            rd.close();
            input.close();

            return gsr;
        } catch (Exception ex) {
            logger.write(ex);
            throw ex;
        } finally {
            if (wr != null)
                try {
                    wr.close();
                } catch (IOException e) {
                    logger.write(e);
                }
            if (rd != null)
                try {
                    rd.close();
                } catch (IOException e) {
                    logger.write(e);
                }
            if (conn != null && !GSRequest.ENABLE_CONNECTION_POOLING)
                ((HttpURLConnection) conn).disconnect();
        }
    }

    public String urlEncodedParam(String key) {
        if (urlEncodedParams == null)
            urlEncodedParams = new GSObject();

        String encoded = urlEncodedParams.getString(key, null);

        if (encoded == null) {
            encoded = GSRequest.UrlEncode(params.getString(key, null));
            urlEncodedParams.put(key, encoded);
        }

        return encoded;
    }

    /**
     * Applies URL encoding rules to the String value
     */
    public static String UrlEncode(String value) {
        if (value == null)
            return null;

        try {
            return URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (Exception ex) {
            return null;
        }
    }

    public void addHeader(String key, String value) {
        additionalHeaders.put(key, value);
    }

    /**
     * Configure the URLConnection before the request is sent.
     * For mTLS authentication, this applies client certificates.
     */
    protected void configureConnection(URLConnection conn) {
        if (authMode == AuthMode.MTLS && conn instanceof HttpsURLConnection) {
            try {
                configureMtlsConnection((HttpsURLConnection) conn);
            } catch (Exception e) {
                logger.write("GSRequest", "Failed to configure mTLS: " + e.getMessage());
                logger.write(e);
                throw new RuntimeException("mTLS configuration failed: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Configure mTLS for HTTPS connections
     */
    private void configureMtlsConnection(HttpsURLConnection httpsConn) throws Exception {
        if (config == null || !config.hasMtlsConfig()) {
            throw new IllegalStateException("mTLS configuration not available");
        }

        CertificateBundle bundle = loadMtlsCertificates();
        if (bundle == null) {
            throw new IllegalStateException("Failed to load client certificates - cannot proceed with mTLS");
        }

        SSLContext sslContext = createMtlsSSLContext(bundle);
        httpsConn.setSSLSocketFactory(sslContext.getSocketFactory());
        httpsConn.setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
    }

    /**
     * Load mTLS certificates and private key
     */
    private CertificateBundle loadMtlsCertificates() {
        try {
            String certPem = config.loadMtlsCertificate();
            String keyPem = config.loadMtlsPrivateKey();

            PrivateKey privateKey = parseMtlsPrivateKeyPkcs8(keyPem);
            X509Certificate[] chain = parseMtlsCertificateChain(certPem);

            if (privateKey == null || chain.length == 0) {
                return null;
            }

            return new CertificateBundle(privateKey, chain);
        } catch (Exception e) {
            logger.write("GSRequest", "Error loading mTLS certificates: " + e.getMessage());
            return null;
        }
    }

    /**
     * Create SSL context for mTLS
     */
    private SSLContext createMtlsSSLContext(CertificateBundle bundle) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        char[] password = config.getMtlsPassword();
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
     * Inner class to hold certificate bundle
     */
    private static class CertificateBundle {
        final PrivateKey privateKey;
        final X509Certificate[] chain;

        CertificateBundle(PrivateKey privateKey, X509Certificate[] chain) {
            this.privateKey = privateKey;
            this.chain = chain;
        }
    }

    // mTLS Certificate parsing helpers
    private static PrivateKey parseMtlsPrivateKeyPkcs8(String pem) throws Exception {
        String base64Key = trimMtlsKey(pem);
        return GSAuthUtils.rsaPrivateKeyFromBase64String(base64Key);
    }

    private static String trimMtlsKey(String pem) {
        return pem
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("-----BEGIN RSA PRIVATE KEY-----", "")
                .replaceAll("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
    }

    private static X509Certificate[] parseMtlsCertificateChain(String pem) throws Exception {
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
