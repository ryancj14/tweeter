package edu.byu.cs.tweeter.client.presenter;

import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.observer.PagedTaskObserver;
import edu.byu.cs.tweeter.client.model.service.observer.UserObserver;
import edu.byu.cs.tweeter.client.presenter.view.PagedView;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public abstract class PagedPresenter<T> extends Presenter<PagedView<T>> {

    private static final int PAGE_SIZE = 10;

    private UserService userService;

    public PagedPresenter() {
        this.userService = new UserService();
    }

    protected T lastItem;
    protected boolean hasMorePages;
    protected boolean isLoading = false;

    public boolean hasMorePages() {
        return hasMorePages;
    }

    protected void setHasMorePages(boolean hasMorePages) {
        this.hasMorePages = hasMorePages;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void loadMoreItems(User user) {
        if (!isLoading) {   // This guard is important for avoiding a race condition in the scrolling code.
            isLoading = true;
            view.setLoadingStatus(true);
            getItems(Cache.getInstance().getCurrUserAuthToken(), user, PAGE_SIZE, lastItem);
        }
    }

    public abstract void getItems(AuthToken authToken, User user, int pageSize, T lastItem);

    public abstract String getDescription();

    public class PageObserver implements PagedTaskObserver<T> {

        @Override
        public void handleSuccess(List<T> story, boolean hasMorePages) {
            removeLoading();
            lastItem = (story.size() > 0) ? story.get(story.size() - 1) : null;
            setHasMorePages(hasMorePages);
            view.addItems(story);
        }

        @Override
        public void handleFailure(String message) {
            removeLoading();
            view.displayMessage(getDescription() + ": " + message);
        }

        @Override
        public void handleException(Exception ex) {
            removeLoading();
            view.displayMessage("Failed to get " + getDescription() + " because of exception: " + ex.getMessage());
        }

        private void removeLoading() {
            isLoading = false;
            view.setLoadingStatus(false);
        }
    }

    //GetUser
    public void onClick(String userAliasStr) {
        userService.getUser(Cache.getInstance().getCurrUserAuthToken(), userAliasStr, new StoryPresenter.GetUserObserver());
    }

    public class GetUserObserver implements UserObserver {

        @Override
        public void handleSuccess(User user) {
            view.navigateToUser(user);
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
