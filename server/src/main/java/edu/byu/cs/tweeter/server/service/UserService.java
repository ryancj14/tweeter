package edu.byu.cs.tweeter.server.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Random;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.request.UserRequest;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;
import edu.byu.cs.tweeter.model.net.response.UserResponse;
import edu.byu.cs.tweeter.server.dao.AuthTokenDAO;
import edu.byu.cs.tweeter.server.dao.UserDAO;
import edu.byu.cs.tweeter.util.FakeData;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;

public class UserService {

    private UserDAO getUserDAO() {
        return new UserDAO();
    }

    private AuthTokenDAO getAuthTokenDAO() {
        return new AuthTokenDAO();
    }

    public LoginResponse login(LoginRequest request) {
        String userAlias = request.getUsername();
        String password = request.getPassword();
        if(userAlias == null) {
            throw new RuntimeException("[BadRequest] Missing a username");
        } else if(password == null) {
            throw new RuntimeException("[BadRequest] Missing a password");
        } else if(!getUserDAO().validPassword(userAlias, password)) {
            throw new RuntimeException("[BadRequest] Invalid password");
        }
        User user = getUserDAO().getUser(userAlias);
        AuthToken authToken = createAddAuthToken(userAlias);
        return new LoginResponse(user, authToken);
    }

    private static final String BucketName = "cs340-ryanjohnson";

    public static Bucket getBucket(String bucket_name) {
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
        Bucket named_bucket = null;
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucket_name)) {
                named_bucket = b;
            }
        }
        return named_bucket;
    }

    public RegisterResponse register(RegisterRequest request) {
        String userAlias = request.getUsername();
        String password = request.getPassword();
        String firstName = request.getFirstName();
        String lastName = request.getLastName();
        String image = request.getImage();
        if(userAlias == null){
            throw new RuntimeException("[BadRequest] Missing a username");
        } else if(password == null) {
            throw new RuntimeException("[BadRequest] Missing a password");
        } else if(firstName == null) {
            throw new RuntimeException("[BadRequest] Missing a first name");
        } else if(lastName == null) {
            throw new RuntimeException("[BadRequest] Missing a last name");
        } else if(image == null) {
            throw new RuntimeException("[BadRequest] Missing an image");
        }

        // Upload image to s3
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        Bucket b = null;
        if (s3.doesBucketExistV2(BucketName)) {
            b = getBucket(BucketName);
        } else {
            try {
                b = s3.createBucket(BucketName);
            } catch (AmazonS3Exception e) {
                throw new RuntimeException("[ServerError] Image bucket doesn't exist and creation failed");
            }
        }
        s3.putObject(BucketName, userAlias, image);
        String url = s3.getUrl(BucketName, userAlias).toString();

        // Create and Add User
        getUserDAO().addUser(userAlias, password, firstName, lastName, url);
        User user = new User(firstName, lastName, userAlias, url);

        // Create and Add AuthToken
        AuthToken authToken = createAddAuthToken(userAlias);

        return new RegisterResponse(user, authToken);
    }

    private AuthToken createAddAuthToken(String userAlias) {
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String token = new String(array, StandardCharsets.UTF_8);
        long dateTime = Instant.now().getEpochSecond();
        String dateTimeString = Long.toString(dateTime);
        AuthToken authToken = new AuthToken(token, dateTimeString);
        getAuthTokenDAO().addAuthToken(token, userAlias, dateTime);
        return authToken;
    }

    public LogoutResponse logout(LogoutRequest request) {
        String authTokenString = request.getAuthToken().getToken();
        getAuthTokenDAO().deleteAuthToken(authTokenString);
        return new LogoutResponse();
    }

    public UserResponse getUser(UserRequest request) {
        String userAlias = request.getUserAliasStr();
        String authTokenString = request.getAuthToken().getToken();
        getAuthTokenDAO().deleteOldEntries();
        if(userAlias == null) {
            throw new RuntimeException("[BadRequest] Missing a user alias");
        } else if(!getAuthTokenDAO().validAuthToken(authTokenString)) {
            throw new RuntimeException("[BadRequest] AuthToken timed out");
        }
        User user = getUserDAO().getUser(userAlias);
        return new UserResponse(user);
    }

    /**
     * Returns the dummy user to be returned by the login operation.
     * This is written as a separate method to allow mocking of the dummy user.
     *
     * @return a dummy user.
     */
    User getDummyUser() {
        return getFakeData().getFirstUser();
    }

    /**
     * Returns the dummy auth token to be returned by the login operation.
     * This is written as a separate method to allow mocking of the dummy auth token.
     *
     * @return a dummy auth token.
     */
    AuthToken getDummyAuthToken() {
        return getFakeData().getAuthToken();
    }

    /**
     * Returns the {@link FakeData} object used to generate dummy users and auth tokens.
     * This is written as a separate method to allow mocking of the {@link FakeData}.
     *
     * @return a {@link FakeData} instance.
     */
    FakeData getFakeData() {
        return new FakeData();
    }
}
