package edu.byu.cs.tweeter.client.model.service;

import android.os.Bundle;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.FollowTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowersCountTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowersTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingCountTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.IsFollowerTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.UnfollowTask;
import edu.byu.cs.tweeter.client.model.service.handler.BackgroundTaskHandler;
import edu.byu.cs.tweeter.client.model.service.handler.GetCountHandler;
import edu.byu.cs.tweeter.client.model.service.handler.PagedTaskHandler;
import edu.byu.cs.tweeter.client.model.service.handler.SimpleTaskHandler;
import edu.byu.cs.tweeter.client.model.service.observer.GetCountObserver;
import edu.byu.cs.tweeter.client.model.service.observer.PagedTaskObserver;
import edu.byu.cs.tweeter.client.model.service.observer.ServiceObserver;
import edu.byu.cs.tweeter.client.presenter.MainPresenter;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowService extends Executes {

    // GET FOLLOWING
    // getFollowing function (for FollowingFragment)
    public void getFollowing(AuthToken currUserAuthToken, User user, int pageSize, User lastFollowee, PagedTaskObserver<User> getFollowingObserver) {
        GetFollowingTask getFollowingTask = new GetFollowingTask(currUserAuthToken, user, pageSize, lastFollowee,
                new PagedTaskHandler<>(getFollowingObserver));
        execute(getFollowingTask);
    }

    // GET FOLLOWERS
    //  getFollowers function (for FollowersFragment),
    public void getFollowers(AuthToken currUserAuthToken, User user, int pageSize, User lastFollower, PagedTaskObserver<User> getFollowersObserver) {
        GetFollowersTask getFollowersTask = new GetFollowersTask(currUserAuthToken, user, pageSize, lastFollower,
                new PagedTaskHandler<>(getFollowersObserver));
        execute(getFollowersTask);
    }

    // GET FOLLOWERS COUNT & GET FOLLOWING COUNT
    // getFollowersCountAndGetFollowingCount function (for MainActivity)
    public void getFollowersCountAndGetFollowingCount(AuthToken currUserAuthToken, User selectedUser,
                                                      GetCountObserver getCountObserver1,
                                                      GetCountObserver getCountObserver2) {
        // Get count of most recently selected user's followers.
        GetFollowersCountTask followersCountTask = new GetFollowersCountTask(currUserAuthToken,
                selectedUser, new GetCountHandler(getCountObserver1));
        execute(followersCountTask);

        // Get count of most recently selected user's followees (who they are following)
        GetFollowingCountTask followingCountTask = new GetFollowingCountTask(currUserAuthToken,
                selectedUser, new GetCountHandler(getCountObserver2));
        execute(followingCountTask);
    }

    // IS FOLLOWER
    // IsFollowerObserver interface
    // isFollower function (for MainActivity)
    // IsFollowerHandler class
    public interface IsFollowerObserver extends ServiceObserver {
        void handleSuccess(boolean isFollower);
    }

    public void isFollower(AuthToken currUserAuthToken, User currUser, User selectedUser, MainPresenter.IsFollowerObserver isFollowerObserver) {
        IsFollowerTask isFollowerTask = new IsFollowerTask(currUserAuthToken,
                currUser, selectedUser, new FollowService.IsFollowerHandler(isFollowerObserver));
        execute(isFollowerTask);
    }

    private class IsFollowerHandler extends BackgroundTaskHandler<IsFollowerObserver> {

        private IsFollowerHandler(IsFollowerObserver observer) {
            super(observer);
        }

        @Override
        protected void handleSuccess(Bundle data, IsFollowerObserver observer) {
            boolean isFollower = data.getBoolean(IsFollowerTask.IS_FOLLOWER_KEY);
            observer.handleSuccess(isFollower);
        }
    }

    // FOLLOW
    // follow function (for MainActivity)
    public void follow(AuthToken currUserAuthToken, User selectedUser, MainPresenter.FollowObserver followObserver) {
        FollowTask followTask = new FollowTask(currUserAuthToken, selectedUser,
                new SimpleTaskHandler(followObserver));
        execute(followTask);
    }

    // UNFOLLOW
    // unfollow function (for MainActivity)
    public void unfollow(AuthToken currUserAuthToken, User selectedUser, MainPresenter.UnfollowObserver unfollowObserver) {
        UnfollowTask unfollowTask = new UnfollowTask(currUserAuthToken, selectedUser,
                new SimpleTaskHandler(unfollowObserver));
        execute(unfollowTask);
    }

}
