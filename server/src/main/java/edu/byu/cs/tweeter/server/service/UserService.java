package edu.byu.cs.tweeter.server.service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
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
import edu.byu.cs.tweeter.server.dao.AuthTokenDAOInterface;
import edu.byu.cs.tweeter.server.dao.UserDAO;
import edu.byu.cs.tweeter.server.dao.UserDAOInterface;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class UserService {

    public LoginResponse login(LoginRequest request, UserDAOInterface userTable, AuthTokenDAOInterface authTable) {
        String userAlias = request.getUsername();
        String password = request.getPassword();
        if(userAlias == null) {
            throw new RuntimeException("[BadRequest] Missing a username");
        } else if(password == null) {
            throw new RuntimeException("[BadRequest] Missing a password");
        } else if(userTable.invalidPassword(userAlias, password)) {
            throw new RuntimeException("[BadRequest] Invalid password");
        }
        User user = userTable.getUser(userAlias);
        AuthToken authToken = createAddAuthToken(userAlias, authTable);
        return new LoginResponse(user, authToken);
    }

    private static final String BucketName = "cs340-ryanjohnson";

    public RegisterResponse register(RegisterRequest request, UserDAOInterface userTable, AuthTokenDAOInterface authTable) {
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
        byte[] bytes = Base64.getDecoder().decode(image);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        PutObjectRequest req = new PutObjectRequest(BucketName, userAlias, stream, new ObjectMetadata()).withCannedAcl(CannedAccessControlList.PublicRead);
        s3.putObject(req);
        String url = s3.getUrl(BucketName, userAlias).toString();
        //String url = s3.getUrl(BucketName, "Animal Kingdom-18.JPG").toString();

        // Create and Add User
        userTable.addUser(userAlias, password, firstName, lastName, url);
        User user = new User(firstName, lastName, userAlias, url);

        // Create and Add AuthToken
        AuthToken authToken = createAddAuthToken(userAlias, authTable);

        return new RegisterResponse(user, authToken);
    }

    private AuthToken createAddAuthToken(String userAlias, AuthTokenDAOInterface authTable) {
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String token = new String(array, StandardCharsets.UTF_8);
        long dateTime = Instant.now().getEpochSecond();
        String dateTimeString = Long.toString(dateTime);
        AuthToken authToken = new AuthToken(token, dateTimeString);
        authTable.addItem(token, userAlias, dateTime);
        return authToken;
    }

    public LogoutResponse logout(LogoutRequest request, AuthTokenDAOInterface authTable) {
        String authTokenString = request.getAuthToken().getToken();
        authTable.deleteItem(authTokenString);
        return new LogoutResponse();
    }

    public UserResponse getUser(UserRequest request, AuthTokenDAOInterface authTable, UserDAOInterface userTable) {
        String userAlias = request.getUserAliasStr();
        String authTokenString = request.getAuthToken().getToken();
        authTable.deleteOldEntries(authTokenString);
        if(userAlias == null) {
            throw new RuntimeException("[BadRequest] Missing a user alias");
        } else if(authTable.invalidAuthToken(authTokenString)) {
            throw new RuntimeException("[BadRequest] AuthToken timed out");
        }
        User user = userTable.getUser(userAlias);
        return new UserResponse(user);
    }
}
