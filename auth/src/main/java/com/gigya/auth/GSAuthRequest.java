package com.gigya.auth;

import com.gigya.socialize.GSRequest;

public class GSAuthRequest extends GSRequest {

    private String privateKey;

    /**
     * @param userKey    Account user key.
     * @param privateKey Account private key.
     * @param apiMethod  Request api method.
     * @param apiKey     Site api key.
     */
    public GSAuthRequest(String userKey, String privateKey, String apiKey, String apiMethod) {
        super(apiKey, null, null, apiMethod, null, true, userKey);
        this.privateKey = privateKey;
    }

    @Override
    protected void signRequest(String token, String secret, String httpMethod, String resourceURI) {
        // Compose jwt && add to request header.
        final String jwt = GSAuthRequestUtils.composeJwt(this.userKey, this.privateKey);
        if (jwt == null) {
            logger.write("Failed to generate authorization JWT");
        }
        addHeader("Authorization", "Bearer " + jwt);
        // Api key is required.
        params.put("apiKey", token);
    }

    @Override
    protected boolean evaluateRequestAuthorization() {
        return true;
    }

}
