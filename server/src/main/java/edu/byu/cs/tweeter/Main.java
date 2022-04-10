package edu.byu.cs.tweeter;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.server.dao.AuthTokenDAO;
import edu.byu.cs.tweeter.server.dao.DataAccessException;
import edu.byu.cs.tweeter.server.dao.FeedDAO;
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.dao.StoryDAO;
import edu.byu.cs.tweeter.server.dao.UserDAO;
import edu.byu.cs.tweeter.server.service.StatusService;

public class Main {
    private static final String BucketName = "cs340-ryanjohnson";

    public static void main(String[] args) throws DataAccessException {
        AuthTokenDAO authTokenDAO = new AuthTokenDAO();
        FeedDAO feedDAO = new FeedDAO();
        FollowDAO followDAO = new FollowDAO();
        StoryDAO storyDAO = new StoryDAO();
        UserDAO userDAO = new UserDAO();
//        authTokenDAO.createTable();
//        feedDAO.createTable();
//        followDAO.createTable();
//        storyDAO.createTable();
//        userDAO.createTable();
//        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
//        String url = s3.getUrl(BucketName, "Animal Kingdom-18.JPG").toString();
//        userDAO.addUser("@rcj", "1", "Ryan", "Johnson", url);
//        userDAO.addUser("@spencer", "3", "Spencer", "Christofferson", url);
//        userDAO.addUser("cassie", "2", "Cassie", "Johnson", url);
////        userDAO.addUser("@brinnley", "4", "Brinnley", "Christofferson", url);
////        followDAO.addFollow("@ryan", "@cassie");
////        followDAO.addFollow("@ryan", "@spencer");
////        followDAO.addFollow("@ryan", "@brinnley");
////        followDAO.addFollow("@spencer", "@brinnley");
//        System.out.println(userDAO.invalidPassword("cassie", "2"));
//        System.out.println(userDAO.getUser("@ryan").getFirstName());
//
//        String image = "JKJKJKJKJKJKJK";
//        String userAlias = "@jk";
////        byte[] bytes = Base64.getDecoder().decode(image);
////        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
////        PutObjectRequest req = new PutObjectRequest(BucketName, userAlias, stream, new ObjectMetadata()).withCannedAcl(CannedAccessControlList.PublicRead);
////        s3.putObject(req);
////        url = s3.getUrl(BucketName, userAlias).toString();
//
//        byte[] bytes = Base64.getDecoder().decode(image);
//        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
//        PutObjectRequest req = new PutObjectRequest(BucketName, userAlias, stream, new ObjectMetadata());
//        s3.putObject(req);
//        url = s3.getUrl(BucketName, userAlias).toString();
//
//        StatusService statusService = new StatusService();
//        statusService.getURLs("Check out http://www.go.com");
//        statusService.getURLs("http://www.go.com");

//        List<String> followers = followDAO.getFollowers("@spencer");
        long timestamp = Instant.now().getEpochSecond();
//        for (String receiverAlias : followers) {
//            System.out.println(receiverAlias);
//            feedDAO.addStatus("@spencer", receiverAlias, "newPost", timestamp);
//        }

        authTokenDAO.addItem("11","@spencer", timestamp);
        System.out.println(authTokenDAO.getUserAlias(new AuthToken("11", "")));
        List<String> followers = followDAO.getFollowers("@spencer");
        for (String receiverAlias : followers) {
            System.out.println(receiverAlias);
            feedDAO.addStatus("@spencer", receiverAlias, "newPost", timestamp);
        }
    }
}
