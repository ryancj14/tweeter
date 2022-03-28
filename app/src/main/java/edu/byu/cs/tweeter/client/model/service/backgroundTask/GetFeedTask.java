package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.TweeterRemoteException;
import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;
import edu.byu.cs.tweeter.util.Pair;

/**
 * Background task that retrieves a page of statuses from a user's feed.
 */
public class GetFeedTask extends PagedStatusTask {

    public GetFeedTask(AuthToken authToken, User targetUser, int limit, Status lastStatus,
                       Handler messageHandler) {
        super(authToken, targetUser, limit, lastStatus, messageHandler);
    }

    @Override
    protected Pair<List<Status>, Boolean> getItems() {
        return getFakeData().getPageOfStatus(getLastItem(), getLimit());
    }

    private static final String LOG_TAG = "GetFeedTask";
    private static final String URL_PATH = "/getfeed";

    @Override
    protected void runTask() {
        try {
            String targetUserAlias = targetUser == null ? null : targetUser.getAlias();
            String lastStatusDate = lastItem == null ? null : lastItem.getDate();

            FeedRequest request = new FeedRequest(authToken, targetUserAlias, limit, lastStatusDate);
            FeedResponse response = getServerFacade().getFeed(request, URL_PATH);

            if(response.isSuccess()) {
                this.items = response.getItems();
                this.hasMorePages = response.getHasMorePages();
                sendSuccessMessage();
            }
            else {
                sendFailedMessage(response.getMessage());
            }
        } catch (IOException | TweeterRemoteException ex) {
            Log.e(LOG_TAG, "Failed to get Feed", ex);
            sendExceptionMessage(ex);
        }
    }
}
