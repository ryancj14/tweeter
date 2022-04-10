package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.server.dao.AuthTokenDAO;
import edu.byu.cs.tweeter.server.dao.FeedDAO;
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.dao.StoryDAO;
import edu.byu.cs.tweeter.server.service.StatusService;

/**
 * An AWS lambda function that logs a user in and returns the user object and an auth code for
 * a successful login.
 */
public class PostStatusHandler implements RequestHandler<PostStatusRequest, PostStatusResponse> {
    @Override
    public PostStatusResponse handleRequest(PostStatusRequest request, Context context) {
        StatusService statusService = new StatusService();
        return statusService.postStatus(request, new AuthTokenDAO(), new StoryDAO(), new FollowDAO(), new FeedDAO());
    }
}