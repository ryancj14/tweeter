package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.List;
import java.util.Map;

public interface StoryDAOInterface extends DAOInterface {
    void addStatus(String posterAlias, String postText, long timeStamp);

    List<Map<String, AttributeValue>> getStory(String userAlias);
}

