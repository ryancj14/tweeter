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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.byu.cs.tweeter.model.domain.User;

public class UserDAO implements UserDAOInterface {

    private static String hashPassword(String passwordToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(passwordToHash.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "FAILED TO HASH";
    }

    private static final String TableName = "users";

    private static final String UserAttr = "userAlias";
    private static final String HashedPasswordAttr = "hashedPassword";
    private static final String FirstNameAttr = "firstName";
    private static final String LastNameAttr = "lastName";
    private static final String ImageAttr = "image";

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
                    .withAttributeName(UserAttr)
                    .withAttributeType("S"));
//            tableAttributeDefinitions.add(new AttributeDefinition()
//                    .withAttributeName(HashedPasswordAttr)
//                    .withAttributeType("S"));
//            tableAttributeDefinitions.add(new AttributeDefinition()
//                    .withAttributeName(FirstNameAttr)
//                    .withAttributeType("S"));
//            tableAttributeDefinitions.add(new AttributeDefinition()
//                    .withAttributeName(LastNameAttr)
//                    .withAttributeType("S"));
//            tableAttributeDefinitions.add(new AttributeDefinition()
//                    .withAttributeName(ImageAttr)
//                    .withAttributeType("S"));

            // Table key schema
            ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<>();
            tableKeySchema.add(new KeySchemaElement()
                    .withAttributeName(UserAttr)
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

    public void addUser(String userAlias, String password, String firstName, String lastName, String image) {
        Table table = dynamoDB.getTable(TableName);

        Item item = new Item()
                .withPrimaryKey(UserAttr, userAlias)
                .withString(HashedPasswordAttr, hashPassword(password))
                .withString(FirstNameAttr, firstName)
                .withString(LastNameAttr, lastName)
                .withString(ImageAttr, image);
        table.putItem(item);
    }

    public boolean invalidPassword(String userAlias, String password) {
        Table table = dynamoDB.getTable(TableName);

        Item item = table.getItem(UserAttr, userAlias);
        if (item == null) {
            return false;
        } else {
            return !hashPassword(password).equals(item.get(HashedPasswordAttr));
        }
    }

    public User getUser(String userAlias) {

        Map<String, String> attrNames = new HashMap<>();
        attrNames.put("#vis", UserAttr);

        Map<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":user", new AttributeValue().withS(userAlias));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(TableName)
                .withKeyConditionExpression("#vis = :user")
                .withExpressionAttributeNames(attrNames)
                .withExpressionAttributeValues(attrValues);

        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        List<Map<String, AttributeValue>> items = queryResult.getItems();
        User user = new User();
        if (items != null) {
            for (Map<String, AttributeValue> item : items){
                user.alias = item.get(UserAttr).getS();
                user.firstName = item.get(FirstNameAttr).getS();
                user.lastName = item.get(LastNameAttr).getS();
                user.imageUrl = item.get(ImageAttr).getS();
            }
        }

        return user;
    }
}
