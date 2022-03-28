package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;
import android.util.Log;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowersCountRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersCountResponse;

/**
 * Background task that queries how many followers a user has.
 */
public class GetFollowersCountTask extends GetCountTask {

    public GetFollowersCountTask(AuthToken authToken, User targetUser, Handler messageHandler) {
        super(authToken, targetUser, messageHandler);
    }

    private static final String LOG_TAG = "GetFollowersCountTask";
    private static final String URL_PATH = "/getfollowerscount";

    @Override
    protected void runTask() {
        try {
            String targetUserAlias = targetUser.getAlias();
            FollowersCountRequest request = new FollowersCountRequest(authToken, targetUserAlias);
            FollowersCountResponse response = getServerFacade().getFollowersCount(request, URL_PATH);

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
