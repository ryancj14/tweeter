package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.UnfollowResponse;
import edu.byu.cs.tweeter.server.dao.AuthTokenDAO;
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.service.FollowService;

/**
 * An AWS lambda function that returns the users a user is following.
 */
public class UnfollowHandler implements RequestHandler<UnfollowRequest, UnfollowResponse> {

    /**
     * Returns the users that the user specified in the request is following. Uses information in
     * the request object to limit the number of followees returned and to return the next set of
     * followees after any that were returned in a previous request.
     *
     * @param request contains the data required to fulfill the request.
     * @param context the lambda context.
     * @return the followees.
     */
    @Override
    public UnfollowResponse handleRequest(UnfollowRequest request, Context context) {
        FollowService service = new FollowService();
        return service.unfollow(request, new AuthTokenDAO(), new FollowDAO());
    }
}