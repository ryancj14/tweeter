package edu.byu.cs.tweeter.client.presenter.view;

public interface MainView extends View {
    void logoutUser();
    void closePostingToast();
    void setFollowerCount(int count);
    void setFolloweeCount(int count);
    void updateFollowButton(boolean removed);
    void updateSelectedUserFollowingAndFollowers();
    void enableFollowButton();
}
