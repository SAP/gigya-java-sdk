package com.gigya.socialize;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Wraps Gigya server's response.
 */
public class GSResponse {
    private int errorCode = 0;
    private String errorMessage = null;
    private String errorDetails = null;
    private String responseText = "";

    private GSObject data = null;
    protected Map<String, List<String>> headers = null;
    private GSLogger logger = new GSLogger();
    private static TreeMap<Integer, String> errorMsgDic = new TreeMap<Integer, String>();
    private static final String LOG_HEADER = "*********** GSResponse Log ***********\n";

    static {
        errorMsgDic.put(500026, "No Internet Connection");
        errorMsgDic.put(400002, "Required parameter is missing");
        errorMsgDic.put(403000, "Invalid or missing session");
    }

    // for constructing response when there is not request (error before request creation)
    public GSResponse(String method, GSObject params, int errorCode, GSLogger traceSoFar) {
        this(method, params, errorCode, getErrorMessage(errorCode), traceSoFar);
    }

    public GSResponse(String method, GSObject params, int errorCode, String errorMessage, GSLogger traceSoFar) {
        this(method, params, errorCode, errorMessage, null, traceSoFar);
    }

    public GSResponse(String method, GSObject params, int errorCode, String errorMessage, String errorDetails, GSLogger traceSoFar) {
        logger.write(traceSoFar);
        if (errorMessage == null || errorMessage.length() == 0)
            errorMessage = getErrorMessage(errorCode);

        String format = "json";
        if (params != null)
            format = params.getString("format", "json").toLowerCase();
        if (format.equals("xml")) {
            this.responseText = getErrorResponseXML(method, params, errorCode, errorMessage);
        }
        this.data = params;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
    }

    protected GSResponse(String method, String responseText, GSLogger traceSoFar) {
        logger.write(traceSoFar);
        this.responseText = responseText.trim();

        if (responseText == null || responseText.length() == 0)
            return;

        if (responseText.startsWith("{")) // JSON format
        {
            try {
                this.data = new GSObject(responseText);
                this.errorCode = data.getInt("errorCode", 0);
                this.errorMessage = data.getString("errorMessage", null);
                this.errorDetails = data.getString("errorDetails", null);
            } catch (Exception ex) {
                this.errorCode = 500;
                this.errorMessage = ex.getMessage();
            }
        } else {
            // using string search to avoid dependency on parser
            String errCodeStr = getStringBetween(responseText, "<errorCode>", "</errorCode>");
            if (errCodeStr != null) {
                this.errorCode = Integer.parseInt(errCodeStr);
                this.errorMessage = getStringBetween(responseText, "<errorMessage>", "</errorMessage>");
            }
        }
        logger.write("errorCode", this.errorCode);
        logger.write("errorMessage", this.errorMessage);
        logger.write("errorDetails", this.errorDetails);
    }

    /**
     * Returns the result code of the operation.
     * Code '0' indicates success, any other number indicates failure. For the complete list of server error codes, see the <a  href="http://developers.gigya.com/display/GD/Response+Codes+and+Errors+REST">Error Codes</a> table.
     *
     * @return the error code
     */
    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * Returns a short textual description of the response error, for logging purposes.
     *
     * @return the error message string
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns a description of the response error, for logging purposes.
     *
     * @return the error details string
     */
    public String getErrorDetails() {
        return errorDetails;
    }

    /**
     * Returns the raw response data.
     * The raw response data is in JSON format, by default. If the request was sent with the format parameter set to "xml", the raw response data will be in XML format.
     *
     * @return the raw response data
     */
    public String getResponseText() {
        return this.responseText;
    }

    /**
     * Returns the response data in GSObject.
     * Please refer to Gigya's <a href="http://developers.gigya.com/display/GD/REST+API">REST API reference</a>, for a list of response data structure per method request.
     * Note: If the request was sent with the <em>format</em> parameter set to "xml", the getData() will return null and you should use getResponseText() method instead.
     * We only parse response text into GSObject if the request format is "json", which is the default.
     *
     * @return a GSObject containing the response data
     */
    public GSObject getData() {
        return this.data;
    }

    public boolean hasData() {
        return this.data != null;
    }

    public String getLog() {
        return LOG_HEADER + logger.toString();
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

	/* GETS */

    /**
     * Returns the boolean value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the boolean value to be returned if this dictionary doesn't contain the specified key.
     * @return the boolean value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     */
    public boolean getBool(String key, boolean defaultValue) {
        if (data == null)
            return defaultValue;

        return data.getBool(key, defaultValue);
    }

    /**
     * Returns the int value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the int value to be returned if this dictionary doesn't contain the specified key.
     * @return the int value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     */
    public int getInt(String key, int defaultValue) {
        if (data == null)
            return defaultValue;

        return data.getInt(key, defaultValue);
    }

    /**
     * Returns the long value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the long value to be returned if this dictionary doesn't contain the specified key.
     * @return the long value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     */
    public long getLong(String key, long defaultValue) {
        if (data == null)
            return defaultValue;

        return data.getLong(key, defaultValue);
    }

    /**
     * Returns the double value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the double value to be returned if this dictionary doesn't contain the specified key.
     * @return the double value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     */
    public double getDouble(String key, double defaultValue) {
        if (data == null)
            return defaultValue;

        return data.getDouble(key, defaultValue);
    }

    /**
     * Returns the String value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the String value to be returned if this dictionary doesn't contain the specified key.
     * @return the String value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     */
    public String getString(String key, String defaultValue) {
        if (data == null)
            return defaultValue;

        return data.getString(key, defaultValue);
    }

    /**
     * Returns the GSObject value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the GSObject value to be returned if this dictionary doesn't contain the specified key.
     * @return the GSObject value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     */
    public GSObject getObject(String key, GSObject defaultValue) {
        if (data == null)
            return defaultValue;

        return data.getObject(key, defaultValue);
    }

    /**
     * Returns the GSArray value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the GSArray value to be returned if this dictionary doesn't contain the specified key.
     * @return the GSArray value to which the specified key is mapped, or the <em>defaultValue</em> if this dictionary contains no mapping for the key.
     */
    public GSArray getArray(String key, GSArray defaultValue) {
        if (data == null)
            return defaultValue;

        return data.getArray(key, defaultValue);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\terrorCode:");
        sb.append(errorCode);
        sb.append("\n\terrorMessage:");
        sb.append(errorMessage);
        sb.append("\n\terrorDetails:");
        sb.append(errorDetails);
        sb.append("\n\tdata:");
        sb.append(data);
        return sb.toString();
    }

    private String getStringBetween(String source, String prefix, String suffix) {
        if (source == null || source.length() == 0) return null;
        int prefixStart = source.indexOf(prefix);
        int suffixStart = source.indexOf(suffix);
        if (prefixStart == -1 || suffixStart == -1) return null;

        return source.subSequence(prefixStart + prefix.length(), suffixStart).toString();
    }

    public static String getErrorMessage(int errorCode) {
        if (errorMsgDic.containsKey(errorCode)) {
            String errorMsg = errorMsgDic.get(errorCode);
            if (errorMsg != null) return errorMsg;
        }
        return "";
    }

    private static String getErrorResponseXML(String method, GSObject params, int errorCode, String errorMessage) {

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("<" + method + "Response xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:com:gigya:api http://socialize-api.gigya.com/schema\" xmlns=\"urn:com:gigya:api\">");
        sb.append("<errorCode>" + errorCode + "</errorCode>");
        sb.append("<errorMessage>" + errorMessage + "</errorMessager>");
        sb.append("</" + method + "Response>");
        return sb.toString();
    }


}

