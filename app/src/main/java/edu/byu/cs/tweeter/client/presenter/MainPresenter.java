package edu.byu.cs.tweeter.client.presenter;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.observer.GetCountObserver;
import edu.byu.cs.tweeter.client.model.service.observer.SimpleTaskObserver;
import edu.byu.cs.tweeter.client.presenter.view.MainView;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class MainPresenter extends Presenter<MainView> {

    private static final String LOG_TAG = "MainActivity";

    private FollowService followService;
    private UserService userService;
    private StatusService statusService;

    public MainPresenter(MainView view) {
        this.view = view;
        this.followService = new FollowService();
        this.userService = new UserService();
    }

    public StatusService getStatusService() {
        if (userService == null) {
            return new StatusService();
        }
        else {
            return statusService;
        }
    }

    public void logOut() {
        userService.logout(Cache.getInstance().getCurrUserAuthToken(), new LogoutObserver());
    }

    public class LogoutObserver implements SimpleTaskObserver {
        @Override
        public void handleSuccess() {
            view.logoutUser();
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to logout: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to logout because of exception: " + ex.getMessage());
        }
    }

    public void updateCounts(User selectedUser) {
        followService.getFollowersCountAndGetFollowingCount(Cache.getInstance().getCurrUserAuthToken(),
                selectedUser, new GetFollowersCountObserver(), new GetFollowingCountObserver());
    }

    public class GetFollowersCountObserver implements GetCountObserver {

        @Override
        public void handleSuccess(int count) {
            view.setFollowerCount(count);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to get followers count: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to get followers count because of exception: " + ex.getMessage());
        }
    }

    public class GetFollowingCountObserver implements GetCountObserver {

        @Override
        public void handleSuccess(int count) {
            view.setFolloweeCount(count);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to get following count: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to get following count because of exception: " + ex.getMessage());
        }
    }

    public void checkIsFollower(User selectedUser) {
        followService.isFollower(Cache.getInstance().getCurrUserAuthToken(), Cache.getInstance().getCurrUser(),
                selectedUser, new IsFollowerObserver());
    }

    public class IsFollowerObserver implements FollowService.IsFollowerObserver {

        @Override
        public void handleSuccess(boolean isFollower) {
            view.updateFollowButton(!isFollower);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to determine following relationship: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to determine following relationship because of exception: " + ex.getMessage());
        }
    }

    public void follow(User selectedUser) {
        followService.follow(Cache.getInstance().getCurrUserAuthToken(), selectedUser, new FollowObserver());
    }

    public class FollowObserver implements SimpleTaskObserver {

        @Override
        public void handleSuccess() {
            view.updateSelectedUserFollowingAndFollowers();
            view.updateFollowButton(false);
            view.enableFollowButton();
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to follow: " + message);
            view.enableFollowButton();
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to follow because of exception: " + ex.getMessage());
            view.enableFollowButton();
        }
    }

    public void setFollowEnabled() {
        view.enableFollowButton();
    }

    public void unfollow(User selectedUser) {
        followService.unfollow(Cache.getInstance().getCurrUserAuthToken(), selectedUser, new UnfollowObserver());
    }

    public class UnfollowObserver implements SimpleTaskObserver {

        @Override
        public void handleSuccess() {
            view.updateSelectedUserFollowingAndFollowers();
            view.updateFollowButton(true);
            view.enableFollowButton();
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to unfollow: " + message);
            view.enableFollowButton();
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to unfollow because of exception: " + ex.getMessage());
            view.enableFollowButton();
        }
    }

    public void postStatus(String post) {
        try {
            Status newStatus = new Status(post, Cache.getInstance().getCurrUser(), getFormattedDateTime(), parseURLs(post), parseMentions(post));
            statusService = getStatusService();
            statusService.postStatus(Cache.getInstance().getCurrUserAuthToken(), newStatus, new PostStatusObserver());
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            view.displayMessage("Failed to post the status because of exception: " + ex.getMessage());
        }
    }

    public class PostStatusObserver implements SimpleTaskObserver {

        @Override
        public void handleSuccess() {
            view.closePostingToast();
            view.displayMessage("Successfully Posted!");
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to post status: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to post status because of exception: " + ex.getMessage());
        }
    }

    public String getFormattedDateTime() throws ParseException {
        SimpleDateFormat userFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat statusFormat = new SimpleDateFormat("MMM d yyyy h:mm aaa");

        return statusFormat.format(userFormat.parse(LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 8)));
    }

    public List<String> parseURLs(String post) {
        List<String> containedUrls = new ArrayList<>();
        for (String word : post.split("\\s")) {
            if (word.startsWith("http://") || word.startsWith("https://")) {

                int index = findUrlEndIndex(word);

                word = word.substring(0, index);

                containedUrls.add(word);
            }
        }

        return containedUrls;
    }

    public int findUrlEndIndex(String word) {
        if (word.contains(".com")) {
            int index = word.indexOf(".com");
            index += 4;
            return index;
        } else if (word.contains(".org")) {
            int index = word.indexOf(".org");
            index += 4;
            return index;
        } else if (word.contains(".edu")) {
            int index = word.indexOf(".edu");
            index += 4;
            return index;
        } else if (word.contains(".net")) {
            int index = word.indexOf(".net");
            index += 4;
            return index;
        } else if (word.contains(".mil")) {
            int index = word.indexOf(".mil");
            index += 4;
            return index;
        } else {
            return word.length();
        }
    }

    public List<String> parseMentions(String post) {
        List<String> containedMentions = new ArrayList<>();

        for (String word : post.split("\\s")) {
            if (word.startsWith("@")) {
                word = word.replaceAll("[^a-zA-Z0-9]", "");
                word = "@".concat(word);

                containedMentions.add(word);
            }
        }

        return containedMentions;
    }

}
