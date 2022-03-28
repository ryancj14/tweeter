package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;
import android.util.Log;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowingCountRequest;
import edu.byu.cs.tweeter.model.net.response.FollowingCountResponse;

/**
 * Background task that queries how many other users a specified user is following.
 */
public class GetFollowingCountTask extends GetCountTask {

    public GetFollowingCountTask(AuthToken authToken, User targetUser, Handler messageHandler) {
        super(authToken, targetUser, messageHandler);
    }

    private static final String LOG_TAG = "GetFollowingCountTask";
    private static final String URL_PATH = "/getfollowingcount";

    @Override
    protected void runTask() {
        try {
            String targetUserAlias = targetUser.getAlias();
            FollowingCountRequest request = new FollowingCountRequest(authToken, targetUserAlias);
            FollowingCountResponse response = getServerFacade().getFollowingCount(request, URL_PATH);

            if(response.isSuccess()) {
                count = response.getCount();
                sendSuccessMessage();
            }
            else {
                sendFailedMessage(response.getMessage());
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            sendExceptionMessage(ex);
        }
    }
}
