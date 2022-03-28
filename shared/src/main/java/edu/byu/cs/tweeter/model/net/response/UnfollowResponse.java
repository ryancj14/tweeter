package edu.byu.cs.tweeter.model.net.response;

import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;

/**
 * A response for a {@link UnfollowRequest}.
 */
public class UnfollowResponse extends Response {

    /**
     * Creates a response indicating that the corresponding request was unsuccessful.
     *
     * @param message a message describing why the request was unsuccessful.
     */
    public UnfollowResponse(String message) {
        super(false, message);
    }

    /**
     * Creates a response indicating that the corresponding request was successful.
     */
    public UnfollowResponse() {
        super(true, null);
    }

}