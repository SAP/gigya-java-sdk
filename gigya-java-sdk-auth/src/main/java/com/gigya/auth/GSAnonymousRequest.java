package com.gigya.auth;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;

public class GSAnonymousRequest extends GSRequest {

    /**
     * @param apiKey    Site api key.
     * @param apiDomain Site api domain.
     * @param apiMethod Request api method.
     */
    public GSAnonymousRequest(String apiKey, String apiDomain, String apiMethod) {
        super(apiKey, null, null, apiMethod, null, true, null);
        setAPIDomain(apiDomain);
    }

    /**
     * @param apiKey       Site api key.
     * @param apiDomain    Site api domain.
     * @param clientParams Request parameters.
     * @param apiMethod    Request api method.
     */
    public GSAnonymousRequest(String apiKey, String apiDomain, GSObject clientParams, String apiMethod) {
        super(apiKey, null, null, apiMethod, clientParams, true, null);
        setAPIDomain(apiDomain);
    }

    @Override
    protected void signRequest(String token, String secret, String httpMethod, String resourceURI) {
        // Override super call.
        if (this.apiKey != null) {
            setParam("apiKey", this.apiKey);
        }
    }

    @Override
    protected boolean evaluateRequestAuthorization() {
        return this.apiKey != null;
    }
}
