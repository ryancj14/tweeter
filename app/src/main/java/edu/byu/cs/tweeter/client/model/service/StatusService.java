package edu.byu.cs.tweeter.client.model.service;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetStoryTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.PostStatusTask;
import edu.byu.cs.tweeter.client.presenter.FeedPresenter;
import edu.byu.cs.tweeter.client.presenter.MainPresenter;
import edu.byu.cs.tweeter.client.presenter.StoryPresenter;
import edu.byu.cs.tweeter.client.view.main.MainActivity;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class StatusService {

    // GET STORY: lines 29-82
    // (GetStoryObserver interface,
    //  GetStory function (for StoryFragment),
    //  GetStoryHandler class)
    public interface GetStoryObserver {
        void handleSuccess(List<Status> statuses, boolean hasMorePages);
        void handleFailure(String message);
        void handleException(Exception ex);
    }

    public void getStory(AuthToken currUserAuthToken, User user, int pageSize, Status lastStatus, StoryPresenter.GetStoryObserver getStoryObserver) {
        GetStoryTask getStoryTask = new GetStoryTask(currUserAuthToken, user, pageSize, lastStatus,
                new StatusService.GetStoryHandler(getStoryObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getStoryTask);
    }

    private class GetStoryHandler extends Handler {

        private StatusService.GetStoryObserver observer;

        private GetStoryHandler(StatusService.GetStoryObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetStoryTask.SUCCESS_KEY);
            if (success) {
                List<Status> statuses = (List<Status>) msg.getData().getSerializable(GetStoryTask.STATUSES_KEY);
                boolean hasMorePages = msg.getData().getBoolean(GetStoryTask.MORE_PAGES_KEY);

                observer.handleSuccess(statuses, hasMorePages);
            } else if (msg.getData().containsKey(GetStoryTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetStoryTask.MESSAGE_KEY);

                observer.handleFailure(message);
            } else if (msg.getData().containsKey(GetStoryTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetStoryTask.EXCEPTION_KEY);

                observer.handleException(ex);
            }
        }
    }

    // GET FEED: lines 29-82
    // (GetFeedObserver interface,
    //  GetFeed function (for StoryFragment),
    //  GetFeedHandler class)
    public interface GetFeedObserver {
        void handleSuccess(List<Status> statuses, boolean hasMorePages);
        void handleFailure(String message);
        void handleException(Exception ex);
    }

    public void getFeed(AuthToken currUserAuthToken, User user, int pageSize, Status lastStatus, FeedPresenter.GetFeedObserver getFeedObserver) {
        GetStoryTask getStoryTask = new GetStoryTask(currUserAuthToken, user, pageSize, lastStatus,
                new StatusService.GetFeedHandler(getFeedObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getStoryTask);
    }

    private class GetFeedHandler extends Handler {

        private StatusService.GetFeedObserver observer;

        private GetFeedHandler(StatusService.GetFeedObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetStoryTask.SUCCESS_KEY);
            if (success) {
                List<Status> statuses = (List<Status>) msg.getData().getSerializable(GetStoryTask.STATUSES_KEY);
                boolean hasMorePages = msg.getData().getBoolean(GetStoryTask.MORE_PAGES_KEY);

                observer.handleSuccess(statuses, hasMorePages);
            } else if (msg.getData().containsKey(GetStoryTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetStoryTask.MESSAGE_KEY);

                observer.handleFailure(message);
            } else if (msg.getData().containsKey(GetStoryTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetStoryTask.EXCEPTION_KEY);

                observer.handleException(ex);
            }
        }
    }

    // POST STATUS: lines 116-1
    // (PostStatusObserver interface,
    //  postStatus function (for MainActivity),
    //  PostStatusHandler class)
    public interface PostStatusObserver {
        void handleSuccess();
        void handleFailure(String message);
        void handleException(Exception ex);
    }

    public void postStatus(AuthToken currUserAuthToken, Status newStatus, MainPresenter.PostStatusObserver postStatusObserver) {
        PostStatusTask statusTask = new PostStatusTask(currUserAuthToken, newStatus,
                new StatusService.PostStatusHandler(postStatusObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(statusTask);
    }

    private class PostStatusHandler extends Handler {

        private StatusService.PostStatusObserver observer;

        private PostStatusHandler(StatusService.PostStatusObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(PostStatusTask.SUCCESS_KEY);
            if (success) {
                observer.handleSuccess();
            } else if (msg.getData().containsKey(PostStatusTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(PostStatusTask.MESSAGE_KEY);

                observer.handleFailure(message);
            } else if (msg.getData().containsKey(PostStatusTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(PostStatusTask.EXCEPTION_KEY);

                observer.handleException(ex);
            }
        }
    }

}
