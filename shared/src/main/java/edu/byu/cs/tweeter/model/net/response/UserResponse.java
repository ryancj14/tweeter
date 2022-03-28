package edu.byu.cs.tweeter.model.net.response;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.UserRequest;

/**
 * A response for a {@link UserRequest}.
 */
public class UserResponse extends Response {

    private User user;

    /**
     * Creates a response indicating that the corresponding request was unsuccessful.
     *
     * @param message a message describing why the request was unsuccessful.
     */
    public UserResponse(String message) {
        super(false, message);
        this.user = null;
    }

    /**
     * Creates a response indicating that the corresponding request was successful.
     */
    public UserResponse(User user) {
        super(true, null);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}