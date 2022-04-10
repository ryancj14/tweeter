package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;

import java.util.*;

/**
 * A DAO for accessing 'following' data from the database.
 */
public class FollowDAO implements FollowDAOInterface {

    private static final String TableName = "follows";
    private static final String IndexName = "followeeAlias-followerAlias-index";

    private static final String FollowerAttr = "followerAlias";
    private static final String FolloweeAttr = "followeeAlias";
    //private static final String VisitCountAttr = "visit_count";

    // DynamoDB client
    private static final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder
            .standard()
            .withRegion("us-east-1")
            .build();
    private static final DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);

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
        Map<String, String> attrNames = new HashMap<>();
        attrNames.put("#vis", FolloweeAttr);

        Map<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":followee", new AttributeValue().withS(followee));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(TableName)
                .withIndexName(IndexName)
                .withKeyConditionExpression("#vis = :followee")
                .withExpressionAttributeNames(attrNames)
                .withExpressionAttributeValues(attrValues);

        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        if (queryResult.getItems() == null) {
            return 0;
        }
        return queryResult.getItems().size();
    }

    public int getFollowingCount(String follower) {
        Map<String, String> attrNames = new HashMap<>();
        attrNames.put("#vis", FollowerAttr);

        Map<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":follower", new AttributeValue().withS(follower));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(TableName)
                .withKeyConditionExpression("#vis = :follower")
                .withExpressionAttributeNames(attrNames)
                .withExpressionAttributeValues(attrValues);

        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        if (queryResult.getItems() == null) {
            return 0;
        }
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

        Map<String, String> attrNames = new HashMap<>();
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

        Map<String, String> attrNames = new HashMap<>();
        attrNames.put("#vis", FolloweeAttr);

        Map<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":followee", new AttributeValue().withS(followee));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(TableName)
                .withIndexName(IndexName)
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
