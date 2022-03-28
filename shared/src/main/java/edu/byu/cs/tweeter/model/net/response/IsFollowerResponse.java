package edu.byu.cs.tweeter.model.net.response;

import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;

/**
 * A response for a {@link IsFollowerRequest}.
 */
public class IsFollowerResponse extends Response {

    private boolean isFollower;

    /**
     * Creates a response indicating that the corresponding request was unsuccessful.
     *
     * @param message a message describing why the request was unsuccessful.
     */
    public IsFollowerResponse(String message) {
        super(false, message);
        this.isFollower = false;
    }

    /**
     * Creates a response indicating that the corresponding request was successful.
     */
    public IsFollowerResponse(boolean isFollower) {
        super(true, null);
        this.isFollower = isFollower;
    }

    public boolean getIsFollower() {
        return isFollower;
    }
}