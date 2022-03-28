package edu.byu.cs.tweeter.model.net.request;

import edu.byu.cs.tweeter.model.domain.AuthToken;

/**
 * Contains all the information needed to make a User request.
 */
public class UserRequest {

    private AuthToken authToken;
    private String userAliasStr;

    private UserRequest() {}

    public UserRequest(AuthToken authToken, String userAliasStr) {
        this.authToken = authToken;
        this.userAliasStr = userAliasStr;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }

    public String getUserAliasStr() {
        return userAliasStr;
    }

    public void setUserAliasStr(String userAliasStr) {
        this.userAliasStr = userAliasStr;
    }
}
