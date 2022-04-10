package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.List;
import java.util.Map;

public interface FeedDAOInterface extends DAOInterface {
    void deleteTable() throws DataAccessException;

    void addStatus(String posterAlias, String receiverAlias, String postText, long timeStamp);

    List<Map<String, AttributeValue>> getFeed(String userAlias);
}
