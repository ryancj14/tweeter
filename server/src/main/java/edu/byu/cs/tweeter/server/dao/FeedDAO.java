package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedDAO implements FeedDAOInterface {

    private static final String TableName = "feed";

    private static final String ReceiverAliasAttr = "receiverAlias";
    private static final String TimeStampAttr = "timestamp";
    private static final String PostTextAttr = "userAlias";
    private static final String PosterAliasAttr = "posterAlias";
    

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
                    .withAttributeName(ReceiverAliasAttr)
                    .withAttributeType("S"));
//            tableAttributeDefinitions.add(new AttributeDefinition()
//                    .withAttributeName(PosterAliasAttr)
//                    .withAttributeType("S"));
//            tableAttributeDefinitions.add(new AttributeDefinition()
//                    .withAttributeName(PostTextAttr)
//                    .withAttributeType("S"));
            tableAttributeDefinitions.add(new AttributeDefinition()
                    .withAttributeName(TimeStampAttr)
                    .withAttributeType("N"));

            // Table key schema
            ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<>();
            tableKeySchema.add(new KeySchemaElement()
                    .withAttributeName(ReceiverAliasAttr)
                    .withKeyType(KeyType.HASH));  //Partition key
            tableKeySchema.add(new KeySchemaElement()
                    .withAttributeName(TimeStampAttr)
                    .withKeyType(KeyType.RANGE));  //Sort key

            CreateTableRequest createTableRequest = new CreateTableRequest()
                    .withTableName(TableName)
                    .withProvisionedThroughput(new ProvisionedThroughput()
                            .withReadCapacityUnits((long) 1)
                            .withWriteCapacityUnits((long) 1))
                    .withAttributeDefinitions(tableAttributeDefinitions)
                    .withKeySchema(tableKeySchema);

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

    public void addStatus(String posterAlias, String receiverAlias, String postText, long timeStamp) {
        Table table = dynamoDB.getTable(TableName);

        Item item = new Item()
                .withPrimaryKey(ReceiverAliasAttr, receiverAlias)
                .withString(PosterAliasAttr, posterAlias)
                .withString(PostTextAttr, postText)
                .withNumber(TimeStampAttr, timeStamp);

        table.putItem(item);
    }

    public List<Map<String, AttributeValue>> getFeed(String userAlias) {

        Map<String, String> attrNames = new HashMap<>();
        attrNames.put("#vis", ReceiverAliasAttr);

        Map<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":receiver", new AttributeValue().withS(userAlias));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(TableName)
                .withKeyConditionExpression("#vis = :receiver")
                .withExpressionAttributeNames(attrNames)
                .withExpressionAttributeValues(attrValues);

        QueryResult queryResult = amazonDynamoDB.query(queryRequest);

        return queryResult.getItems();
    }
}
