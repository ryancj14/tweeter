package edu.byu.cs.tweeter.server.dao;

import java.util.List;

public interface FollowDAOInterface extends DAOInterface {
    int getFollowersCount(String followee);

    int getFollowingCount(String follower);

    boolean isFollowing(String follower, String followee);

    void addFollow(String follower, String followee);

    void deleteFollow(String follower, String followee);

    List<String> getFollowing(String follower);

    List<String> getFollowers(String followee);
}
