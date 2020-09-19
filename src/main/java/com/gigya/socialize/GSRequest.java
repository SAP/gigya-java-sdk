package com.gigya.socialize;

import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.zip.GZIPInputStream;

/**
 * This class is used for sending a request to Gigya Service.
 */
public class GSRequest {
    public static final String VERSION = "java_3.2.4";

    public static boolean ENABLE_CONNECTION_POOLING = true;

    protected static long timestampOffsetSec = 0; // used internally by the SDK, to compensate for time diff with server
    private static Random randomGenerator = new Random();
    private static final String DEFAULT_API_DOMAIN = "us1.gigya.com";

    private String host;
    private String path;
    private String accessToken;
    private String apiKey;
    private String secretKey;
    private GSObject params;
    private GSObject urlEncodedParams;
    private boolean useHTTPS;
    private boolean isLoggedIn;
    private boolean isRetry = false;
    private String userKey;
    protected String apiMethod;
    protected String apiDomain = DEFAULT_API_DOMAIN;
    protected String hostOverride = null;
    protected String format;

    private GSLogger logger = new GSLogger();
    private Proxy proxy = null;


    public GSRequest(String accessToken, String apiMethod) {
        this(null, null, accessToken, apiMethod, null, true, null);
    }

    public GSRequest(String accessToken, String apiMethod, GSObject clientParams) {
        this(null, null, accessToken, apiMethod, clientParams, true, null);
    }

    public GSRequest(String apiKey, String secretKey, String apiMethod, GSObject clientParams) {
        this(apiKey, secretKey, null, apiMethod, clientParams, false, null);
    }

    public GSRequest(String apiKey, String secretKey, String apiMethod, boolean useHTTPS) {
        this(apiKey, secretKey, null, apiMethod, null, useHTTPS, null);
    }

    public GSRequest(String apiKey, String secretKey, String apiMethod) {
        this(apiKey, secretKey, null, apiMethod, null, false, null);
    }

    public GSRequest(String apiKey, String secretKey, String apiMethod, GSObject clientParams, boolean useHTTPS) {
        this(apiKey, secretKey, null, apiMethod, clientParams, useHTTPS, null);
    }

    public GSRequest(String apiKey, String secretKey, String accessToken, String apiMethod, GSObject clientParams, boolean useHTTPS) {
        this(apiKey, secretKey, accessToken, apiMethod, clientParams, useHTTPS, null);
    }

    public GSRequest(String apiKey, String secretKey, String apiMethod, GSObject clientParams, boolean useHTTPS, String userKey) {
        this(apiKey, secretKey, null, apiMethod, clientParams, useHTTPS, userKey);
    }

    /**
     * Constructs a request using an apiKey and a secretKey. Suitable when using
     * Gigya's proprietary authorization method. To learn more, please refer to
     * our <a href="http://developers.gigya.com/display/GD/REST+API+with+Gigya's+Authorization+Method"
     * >Using Gigya's REST API with our proprietary authorization method
     * Guide</a> Please provide a user ID (UID) in the <em>params</em> object to
     * specify the user.
     *
     * @param apiKey       your Gigya API-Key which can be obtained from the <a href=
     *                     "https://www.gigya.com/site/partners/settings.aspx#&amp;&amp;userstate=SiteSetup"
     *                     >Site Setup</a> page on Gigya's website (Read more in the <a
     *                     href=
     *                     "http://developers.gigya.com/display/GD/Site+Setup"
     *                     >Site Setup</a> guide).
     * @param secretKey    your Gigya Secret-Key which can be obtained from the <a href=
     *                     "https://www.gigya.com/site/partners/settings.aspx#&amp;&amp;userstate=SiteSetup"
     *                     >Site Setup</a> page on Gigya's website.
     * @param apiMethod    the Gigya API method to call, including namespace. For
     *                     example: "socialize.getUserInfo". Please refer to our <a
     *                     href="http://developers.gigya.com/display/GD/REST+API"
     *                     >REST API reference</a> for the list of available methods.
     * @param clientParams a GSObject object that contains the parameters for the Gigya
     *                     API method to call. Please refer to our <a
     *                     href="http://developers.gigya.com/display/GD/REST+API"
     *                     >REST API reference</a> and find in the specific method
     *                     reference the list of method parameters. Please provide a user
     *                     ID (UID) in the params object to specify the user.
     * @param useHTTPS     this parameter determines whether the request to Gigya will be
     *                     sent over HTTP or HTTPS. To send of HTTPS, please set this
     *                     parameter to true. The library uses HTTP (the request is
     *                     signed with the session's secret key) and only uses HTTPS if
     *                     the secret is not present. but you can use this parameter to
     *                     override the decision.
     * @param userKey      A key of an administrative user with extra permissions.
     *                     If this parameter is provided, then the secretKey parameter is assumed to be
     *                     the admin user's secret key and not the site's secret key.
     */
    public GSRequest(String apiKey, String secretKey, String accessToken, String apiMethod, GSObject clientParams, boolean useHTTPS, String userKey) {
        if (apiMethod == null || apiMethod.length() == 0)
            return;

        if (clientParams == null)
            this.params = new GSObject();
        else
            this.params = clientParams.clone();

        this.apiMethod = apiMethod;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.accessToken = accessToken;
        this.useHTTPS = useHTTPS;
        this.userKey = userKey;
    }

    /**
     * Allows passing an existing GSLogger
     *
     * @param logger
     */
    public void setLogger(GSLogger logger) {
        if (logger != null) {
            this.logger.write(logger.toString());
        }
    }

    /**
     * Retrieves the logger object
     *
     * @return logger object
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
     * Sets a request parameter with a value. If the request previously
     * contained a mapping for the key, the old value is replaced by the
     * specified value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value a String value to be associated with the specified key
     */
    public void setParam(String key, String value) {
        params.put(key, value);
    }

    /**
     * Sets a request parameter with a value. If the request previously
     * contained a mapping for the key, the old value is replaced by the
     * specified value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value a int value to be associated with the specified key
     */
    public void setParam(String key, int value) {
        params.put(key, value);
    }

    /**
     * Sets a request parameter with a value. If the request previously
     * contained a mapping for the key, the old value is replaced by the
     * specified value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value a long value to be associated with the specified key
     */
    public void setParam(String key, long value) {
        params.put(key, value);
    }

    /**
     * Sets a request parameter with a value. If the request previously
     * contained a mapping for the key, the old value is replaced by the
     * specified value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value a boolean value to be associated with the specified key
     */
    public void setParam(String key, boolean value) {
        params.put(key, value);
    }

    /**
     * Sets a request parameter with a value. If the request previously
     * contained a mapping for the key, the old value is replaced by the
     * specified value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value a GSObject value to be associated with the specified key
     */
    public void setParam(String key, GSObject value) {
        params.put(key, value);
    }

    /**
     * Sets a request parameter with a value. If the request previously
     * contained a mapping for the key, the old value is replaced by the
     * specified value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value a GSArray value to be associated with the specified key
     */
    public void setParam(String key, GSArray value) {
        params.put(key, value);
    }

    /**
     * Returns a GSObject object containing the parameters of this request.
     *
     * @return the <em>params</em> field of this request.
     */
    public GSObject getParams() {
        return params;
    }

    /**
     * Sets a proxy.
     *
     * @param p a proxy settings object
     */
    public void setProxy(Proxy p) {
        this.proxy = p;
    }

    public void setUseHTTPS(boolean useHTTPS) {
        this.useHTTPS = useHTTPS;
    }

    /**
     * Sets a GSObject object containing the parameters of this request.
     */
    public void setParams(GSObject clientParams) {
        if (clientParams == null)
            this.params = new GSObject();
        else
            this.params = clientParams.clone();
    }

    /**
     * Sets the domain used for making API calls. <br />
     * Clients with sites defined under Gigya's European data center should set  it to "eu1.gigya.com". To verify site location contact your implementation manager.
     *
     * @param apiDomain the domain of the data center to be used. For example: "eu1.gigya.com" for Europe data center
     */
    public void setAPIDomain(String apiDomain) {
        if (apiDomain != null)
            this.apiDomain = apiDomain;
        else
            this.apiDomain = DEFAULT_API_DOMAIN;
    }

    public void setHostOverride(String host) {
        this.hostOverride = host;
    }

    /**
     * Sends the request synchronously. The method returns a GSResponse object
     * which represents Gigya's response.
     *
     * @return a GSResponse object representing Gigya's response
     */
    public GSResponse send() {
        return send(-1);
    }

    /**
     * Sends the request synchronously. The method returns a GSResponse object
     * which represents Gigya's response
     *
     * @param timeoutMS using this parameter you may set a timeout to this request.
     *                  The timeout is the number of milliseconds till returning
     *                  timeout response. If the timeout expires, the server will
     *                  return a response with a "Request Timeout" error (error code
     *                  504002).
     * @return a GSResponse object representing Gigya's response
     */
    public GSResponse send(int timeoutMS) {
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
        logger.write("params", params);
        logger.write("useHTTPS", useHTTPS);


        if (this.accessToken == null &&
                ((this.apiKey == null && this.userKey == null)
                        || (this.secretKey == null && this.userKey != null)))
            return new GSResponse(this.apiMethod, this.params, 400002, logger);

        try {
            GSResponse res = sendRequest("POST", this.host, this.path,
                    params, apiKey, secretKey, this.useHTTPS, this.isLoggedIn,
                    timeoutMS);
            // if error code indicates timestamp expiration, retry the request.
            // (sendRequest calculates the tsOffset)
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
     * Sends the request asynchronously.
     *
     * @param listener an Object which implements the GSResponseListener
     */
    public void send(GSResponseListener listener) {
        send(listener, null);
    }

    /**
     * Sends the request asynchronously.
     *
     * @param listener an Object which implements the GSResponseListener
     * @param context  this object will be passed untouched and received back in the
     *                 response
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
     * Converts a GSObject to a query string.
     *
     * @param params the GSObject to convert
     * @return the query string
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

    /////////////////////////////////////// PRIVATE & PROTECTED ////////////////////////////////////////////
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
     * Send the actual HTTP/S request
     *
     * @param httpMethod "POST" or "GET"
     * @param domain
     * @param path
     * @param params
     * @param token      session token
     * @param secret     session secret
     * @param useHTTPS   override HTTPS usage
     * @return token and secret can be: 1. session secret & session token - for
     * mobile login 2. OAuth token & null - from OAuth login endpoint 3.
     * apiKey & secret - for server-to-server calls 4. apiKey & null -
     * when passing requiresLogin=false, for calls that don't require
     * login
     * @throws Exception
     */
    protected GSResponse sendRequest(String httpMethod, String domain,
                                     String path, GSObject params, String token, String secret,
                                     boolean useHTTPS, boolean isLoggedIn, int timeoutMS)
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

            if (accessToken != null) {
                params.put("oauth_token", accessToken);
            } else {
                if (!params.containsKey("oauth_token"))
                    params.put("apiKey", token);

                if (this.userKey != null)
                    params.put("userKey", this.userKey);

                if (secret != null) {
                    String timestamp = Long.toString((System
                            .currentTimeMillis() / 1000)
                            + timestampOffsetSec);

                    String nonce = Long
                            .toString(System.currentTimeMillis())
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
                    logger.write("signature", signature);
                }
            }

            String data = this.buildQS();
            logger.write("post_data", data);

            URL url = new URL(resourceURI);
            logger.write("url", url);

            if (proxy == null)
                conn = url.openConnection();
            else
                conn = url.openConnection(proxy);

            if (timeoutMS != -1) {
                conn.setConnectTimeout(timeoutMS);
                conn.setReadTimeout(timeoutMS);
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

            return gsr;
        } catch (Exception ex) {
            logger.write(ex);
            throw ex;
        } finally {
            if (wr != null)
                try {
                    wr.close();
                } catch (IOException e) {
                }
            if (rd != null)
                try {
                    rd.close();
                } catch (IOException e) {
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
     * Applies URL encoding rules to the String value, and returns the outcome.
     *
     * @param value the string to encode
     * @return the URL encoded string
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

}
