package edu.byu.cs.tweeter.client.presenter;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.view.main.following.FollowingFragment;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowingPresenter {

    private static final int PAGE_SIZE = 10;

    public interface View {
        void displayMessage(String message);
        void setLoadingStatus(boolean value);
        void addFollowees(List<User> followees);
    }

    private View view;
    private FollowService followService;
    private UserService userService;

    private User lastFollowee;

    private boolean hasMorePages;
    private boolean isLoading = false;

    public FollowingPresenter(View view) {
        this.view = view;
    }

    public User getLastFollowee() {
        return lastFollowee;
    }

    public void setLastFollowee(User lastFollowee) {
        this.lastFollowee = lastFollowee;
    }

    public boolean hasMorePages() {
        return hasMorePages;
    }

    public void setHasMorePages(boolean hasMorePages) {
        this.hasMorePages = hasMorePages;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public void loadMoreItems(User user) {
        if (!isLoading) {   // This guard is important for avoiding a race condition in the scrolling code.
            isLoading = true;
            view.setLoadingStatus(true);
            followService.getFollowing(Cache.getInstance().getCurrUserAuthToken(), user, PAGE_SIZE, lastFollowee, new GetFollowingObserver());
        }
    }

    public class GetFollowingObserver implements FollowService.GetFollowingObserver {

        @Override
        public void handleSuccess(List<User> followees, boolean hasMorePages) {
            isLoading = false;
            view.setLoadingStatus(false);

            lastFollowee = (followees.size() > 0) ? followees.get(followees.size() - 1) : null;
            setHasMorePages(hasMorePages);
            view.addFollowees(followees);
        }

        @Override
        public void handleFailure(String message) {
            isLoading = false;
            view.setLoadingStatus(false);

            view.displayMessage("Failed to get following: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            isLoading = false;
            view.setLoadingStatus(false);

            view.displayMessage("Failed to get following because of exception: " + ex.getMessage());
        }
    }

    public void onClick(String userAliasStr) {
        new FollowingFragment.FollowingHolder.GetUserHandler()
        userService.getUser(Cache.getInstance().getCurrUserAuthToken(), userAliasStr, new GetFollowingObserver());
        GetUserTask getUserTask = new GetUserTask(Cache.getInstance().getCurrUserAuthToken(),
                userAliasStr, new FollowingFragment.FollowingHolder.GetUserHandler());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getUserTask);
        Toast.makeText(getContext(), "Getting user's profile...", Toast.LENGTH_LONG).show();
    }

    public class GetUserObserver implements UserService.GetUserObserver {

    }
}
