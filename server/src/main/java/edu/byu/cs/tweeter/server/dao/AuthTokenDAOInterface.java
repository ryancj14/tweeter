package edu.byu.cs.tweeter.server.dao;

import edu.byu.cs.tweeter.model.domain.AuthToken;

public interface AuthTokenDAOInterface extends DAOInterface {
    void addItem(String authToken, String userAlias, long timeStamp);

    void deleteItem(String authToken);

    void deleteOldEntries(String authToken);

    boolean invalidAuthToken(String authToken);

    String getUserAlias(AuthToken authToken);
}
