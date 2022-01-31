package edu.byu.cs.tweeter.client.presenter;

import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class StoryPresenter {

    private static final int PAGE_SIZE = 10;

    public interface View {
        void addStory(List<Status> story);
        void setLoadingStatus(boolean b);
        void displayMessage(String s);
        void addUser(User user);
    }

    private View view;
    private StatusService statusService;
    private UserService userService;

    private Status lastStatus;
    private boolean hasMorePages;
    private boolean isLoading = false;

    public StoryPresenter(StoryPresenter.View view) {
        this.view = view;
        this.statusService = new StatusService();
        this.userService = new UserService();
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

    public void loadMoreItems(User user) {
        if (!isLoading) {   // This guard is important for avoiding a race condition in the scrolling code.
            isLoading = true;
            view.setLoadingStatus(true);
            statusService.getStory(Cache.getInstance().getCurrUserAuthToken(), user, PAGE_SIZE, lastStatus, new StoryPresenter.GetStoryObserver());
        }
    }

    public class GetStoryObserver implements StatusService.GetStoryObserver {

        @Override
        public void handleSuccess(List<Status> story, boolean hasMorePages) {
            isLoading = false;
            view.setLoadingStatus(false);

            lastStatus = (story.size() > 0) ? story.get(story.size() - 1) : null;
            setHasMorePages(hasMorePages);
            view.addStory(story);
        }

        @Override
        public void handleFailure(String message) {
            isLoading = false;
            view.setLoadingStatus(false);

            view.displayMessage("Failed to get story: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            isLoading = false;
            view.setLoadingStatus(false);

            view.displayMessage("Failed to get story because of exception: " + ex.getMessage());
        }
    }

    //GetUser
    public void onClick(String userAliasStr) {
        userService.getUser(Cache.getInstance().getCurrUserAuthToken(), userAliasStr, new StoryPresenter.GetUserObserver());
    }

    public class GetUserObserver implements UserService.GetUserObserver {

        @Override
        public void handleSuccess(User user) {
            view.addUser(user);
        }

        @Override
        public void handleFailure(String message) {
            view.displayMessage("Failed to get user's profile: " + message);
        }

        @Override
        public void handleException(Exception ex) {
            view.displayMessage("Failed to get user's profile because of exception: " + ex.getMessage());
        }
    }
}
