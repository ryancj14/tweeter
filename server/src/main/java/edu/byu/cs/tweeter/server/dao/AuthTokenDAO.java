package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.byu.cs.tweeter.model.domain.AuthToken;

public class AuthTokenDAO {

    private static final String TableName = "auth_token";

    private static final String AuthTokenAttr = "authToken";
    private static final String TimeStampAttr = "timestamp";
    private static final String UserAliasAttr = "userAlias";

    // DynamoDB client
    private static AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder
            .standard()
            .withRegion("us-east-1")
            .build();
    private static DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);

    private static boolean isNonEmptyString(String value) {
        return (value != null && value.length() > 0);
    }

    public void createTable() throws DataAccessException {
        try {
            // Attribute definitions
            ArrayList<AttributeDefinition> tableAttributeDefinitions = new ArrayList<>();

            tableAttributeDefinitions.add(new AttributeDefinition()
                    .withAttributeName(AuthTokenAttr)
                    .withAttributeType("S"));
            tableAttributeDefinitions.add(new AttributeDefinition()
                    .withAttributeName(UserAliasAttr)
                    .withAttributeType("S"));
            tableAttributeDefinitions.add(new AttributeDefinition()
                    .withAttributeName(TimeStampAttr)
                    .withAttributeType("N"));

            // Table key schema
            ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<>();
            tableKeySchema.add(new KeySchemaElement()
                    .withAttributeName(AuthTokenAttr)
                    .withKeyType(KeyType.HASH));  //Partition key

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

    public void addAuthToken(String authToken, String userAlias, long timeStamp) {
        Table table = dynamoDB.getTable(TableName);

        Item item = new Item()
                .withPrimaryKey(AuthTokenAttr, authToken)
                .withString(UserAliasAttr, userAlias)
                .withNumber(TimeStampAttr, timeStamp);

        table.putItem(item);
    }

    public void deleteAuthToken(String authToken) {
        Table table = dynamoDB.getTable(TableName);
        table.deleteItem(AuthTokenAttr, authToken);
    }

    public void deleteOldEntries() {
        Table table = dynamoDB.getTable(TableName);

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(TableName);
        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        List<Map<String, AttributeValue>> items = queryResult.getItems();
        if (items != null) {
            for (Map<String, AttributeValue> item : items){
                String timestampString = item.get(TimeStampAttr).getN();
                long timestamp = Long.parseLong(timestampString);
                if (timestamp < (Instant.now().getEpochSecond() - 600)) {
                    deleteAuthToken(item.get(AuthTokenAttr).getS());
                }
            }
        }
    }

    public boolean validAuthToken(String authToken) {
        Map<String, String> attrNames = new HashMap<String, String>();
        attrNames.put("#loc", AuthTokenAttr);

        Map<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":token", new AttributeValue().withS(authToken));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(TableName)
                .withKeyConditionExpression("#loc = :token")
                .withExpressionAttributeNames(attrNames)
                .withExpressionAttributeValues(attrValues);

        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        List<Map<String, AttributeValue>> items = queryResult.getItems();
        if (items != null) {
            return true;
        }
        return false;
    }

    public String getUserAlias(AuthToken authToken) {
        Table table = dynamoDB.getTable(TableName);
        Item item = table.getItem(AuthTokenAttr, authToken.getToken());
        return item.getString(UserAliasAttr);
    }
}
