package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;

import java.util.*;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingCountRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.FollowResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingCountResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingResponse;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;
import edu.byu.cs.tweeter.model.net.response.UnfollowResponse;
import edu.byu.cs.tweeter.util.FakeData;

/**
 * A DAO for accessing 'following' data from the database.
 */
public class FollowDAO {

    private static final String TableName = "follows";
    private static final String IndexName = "followeeAlias-followerAlias-index";

    private static final String FollowerAttr = "followerAlias";
    private static final String FolloweeAttr = "followeeAlias";
    //private static final String VisitCountAttr = "visit_count";

    // DynamoDB client
    private static AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder
            .standard()
            .withRegion("us-east-1")
            .build();
    private static DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);

    private static boolean isNonEmptyString(String value) {
        return (value != null && value.length() > 0);
    }

    /**
     * Create the "visits" table and the "visits-index" global index
     *
     * @throws DataAccessException
     */
    public void createTable() throws DataAccessException {
        try {
            // Attribute definitions
            ArrayList<AttributeDefinition> tableAttributeDefinitions = new ArrayList<>();

            tableAttributeDefinitions.add(new AttributeDefinition()
                    .withAttributeName(FollowerAttr)
                    .withAttributeType("S"));
            tableAttributeDefinitions.add(new AttributeDefinition()
                    .withAttributeName(FolloweeAttr)
                    .withAttributeType("S"));

            
            
            // Table key schema
            ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<>();
            tableKeySchema.add(new KeySchemaElement()
                    .withAttributeName(FollowerAttr)
                    .withKeyType(KeyType.HASH));  //Partition key
            tableKeySchema.add(new KeySchemaElement()
                    .withAttributeName(FolloweeAttr)
                    .withKeyType(KeyType.RANGE));  //Sort key

            // Secondary Index
            GlobalSecondaryIndex index = new GlobalSecondaryIndex()
                    .withIndexName(IndexName)
                    .withProvisionedThroughput(new ProvisionedThroughput()
                            .withReadCapacityUnits((long) 1)
                            .withWriteCapacityUnits((long) 1))
                    .withProjection(new Projection().withProjectionType(ProjectionType.ALL));

            ArrayList<KeySchemaElement> indexKeySchema = new ArrayList<>();

            indexKeySchema.add(new KeySchemaElement()
                    .withAttributeName(FolloweeAttr)
                    .withKeyType(KeyType.HASH));  //Partition key
            indexKeySchema.add(new KeySchemaElement()
                    .withAttributeName(FollowerAttr)
                    .withKeyType(KeyType.RANGE));  //Sort key

            index.setKeySchema(indexKeySchema);
            // Secondary Index End

            CreateTableRequest createTableRequest = new CreateTableRequest()
                    .withTableName(TableName)
                    .withProvisionedThroughput(new ProvisionedThroughput()
                            .withReadCapacityUnits((long) 1)
                            .withWriteCapacityUnits((long) 1))
                    .withAttributeDefinitions(tableAttributeDefinitions)
                    .withKeySchema(tableKeySchema)
                    .withGlobalSecondaryIndexes(index);

            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();
        }
        catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    /**
     * Delete the "visits" table and the "visits-index" global index
     *
     * @throws DataAccessException
     */
    public void deleteTable() throws DataAccessException {
        try {
            Table table = dynamoDB.getTable(TableName);
            if (table != null) {
                table.delete();
                table.waitForDelete();
            }
        }
        catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public int getFollowersCount(String followee) {
        Map<String, String> attrNames = new HashMap<String, String>();
        attrNames.put("#vis", FolloweeAttr);

        Map<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":followee", new AttributeValue().withS(followee));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(TableName)
                .withKeyConditionExpression("#vis = :followee")
                .withExpressionAttributeNames(attrNames)
                .withExpressionAttributeValues(attrValues);

        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        return queryResult.getItems().size();
    }

    public int getFollowingCount(String follower) {
        Map<String, String> attrNames = new HashMap<String, String>();
        attrNames.put("#vis", FollowerAttr);

        Map<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":follower", new AttributeValue().withS(follower));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(TableName)
                .withKeyConditionExpression("#vis = :follower")
                .withExpressionAttributeNames(attrNames)
                .withExpressionAttributeValues(attrValues);

        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        return queryResult.getItems().size();
    }

    public boolean isFollowing(String follower, String followee) {
        Table table = dynamoDB.getTable(TableName);

        Item item = table.getItem(FollowerAttr, follower, FolloweeAttr, followee);
        return (item != null);
    }

    public void addFollow(String follower, String followee) {
        Table table = dynamoDB.getTable(TableName);

        Item item = new Item()
                .withPrimaryKey(FollowerAttr, follower, FolloweeAttr, followee);

        table.putItem(item);
    }

    public void deleteFollow(String follower, String followee) {
        Table table = dynamoDB.getTable(TableName);
        table.deleteItem(FollowerAttr, follower, FolloweeAttr, followee);
    }

    public List<String> getFollowing(String follower) {
        List<String> result = new ArrayList<>();

        Map<String, String> attrNames = new HashMap<String, String>();
        attrNames.put("#vis", FollowerAttr);

        Map<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":follower", new AttributeValue().withS(follower));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(TableName)
                .withKeyConditionExpression("#vis = :follower")
                .withExpressionAttributeNames(attrNames)
                .withExpressionAttributeValues(attrValues);

        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        List<Map<String, AttributeValue>> items = queryResult.getItems();
        if (items != null) {
            for (Map<String, AttributeValue> item : items){
                result.add(item.get(FolloweeAttr).getS());
            }
        }

        return result;
    }

    public List<String> getFollowers(String followee) {
        List<String> result = new ArrayList<>();

        Map<String, String> attrNames = new HashMap<String, String>();
        attrNames.put("#vis", FolloweeAttr);

        Map<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":followee", new AttributeValue().withS(followee));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(TableName)
                .withKeyConditionExpression("#vis = :followee")
                .withExpressionAttributeNames(attrNames)
                .withExpressionAttributeValues(attrValues);

        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        List<Map<String, AttributeValue>> items = queryResult.getItems();
        if (items != null) {
            for (Map<String, AttributeValue> item : items){
                result.add(item.get(FollowerAttr).getS());
            }
        }

        return result;
    }
}
