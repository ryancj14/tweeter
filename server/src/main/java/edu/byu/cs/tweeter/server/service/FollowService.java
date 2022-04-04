package edu.byu.cs.tweeter.server.service;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingCountRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.FollowResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingCountResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingResponse;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;
import edu.byu.cs.tweeter.model.net.response.UnfollowResponse;
import edu.byu.cs.tweeter.server.dao.AuthTokenDAO;
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.dao.UserDAO;
import edu.byu.cs.tweeter.util.FakeData;

/**
 * Contains the business logic for getting the users a user is following.
 */
public class FollowService {

    private UserDAO getUserDAO() {
        return new UserDAO();
    }

    private AuthTokenDAO getAuthTokenDAO() {
        return new AuthTokenDAO();
    }


    FollowDAO getFollowDAO() {
        return new FollowDAO();
    }

    public FollowingResponse getFollowees(FollowingRequest request) {
        String followerAlias = request.getFollowerAlias();
        int limit = request.getLimit();
        String lastFolloweeAlias = request.getLastFolloweeAlias();
        if(followerAlias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if(limit <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        List<String> allAliases = getFollowDAO().getFollowing(followerAlias);
        List<User> responseFollowees = new ArrayList<>(limit);

        List<User> allFollowees = new ArrayList<>();
        for (String alias : allAliases) {
            allFollowees.add(getUserDAO().getUser(alias));
        }

        boolean hasMorePages = false;

        if (!allFollowees.isEmpty()) {
            int followeesIndex = getFolloweesStartingIndex(lastFolloweeAlias, allFollowees);

            for(int limitCounter = 0; followeesIndex < allFollowees.size() && limitCounter < limit; followeesIndex++, limitCounter++) {
                responseFollowees.add(allFollowees.get(followeesIndex));
            }

            hasMorePages = followeesIndex < allFollowees.size();
        }

        return new FollowingResponse(responseFollowees, hasMorePages);
    }

    public FollowersResponse getFollowers(FollowersRequest request) {
        String followeeAlias = request.getFolloweeAlias();
        int limit = request.getLimit();
        String lastFollowerAlias = request.getLastFollowerAlias();
        if(followeeAlias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if(limit <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        List<String> allAliases = getFollowDAO().getFollowers(followeeAlias);
        List<User> responseFollowers = new ArrayList<>(limit);

        List<User> allFollowers = new ArrayList<>();
        for (String alias : allAliases) {
            allFollowers.add(getUserDAO().getUser(alias));
        }

        boolean hasMorePages = false;

        if (!allFollowers.isEmpty()) {
            int followeesIndex = getFolloweesStartingIndex(lastFollowerAlias, allFollowers);

            for(int limitCounter = 0; followeesIndex < allFollowers.size() && limitCounter < limit; followeesIndex++, limitCounter++) {
                responseFollowers.add(allFollowers.get(followeesIndex));
            }

            hasMorePages = followeesIndex < allFollowers.size();
        }

        return new FollowersResponse(responseFollowers, hasMorePages);
    }

    public FollowingCountResponse getFollowingCount(FollowingCountRequest request) {
        String userAlias = request.getTargetUserAlias();
        if(userAlias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a user alias");
        }
        int followingCount = getFollowDAO().getFollowingCount(userAlias);
        return new FollowingCountResponse(followingCount);
    }

    public FollowersCountResponse getFollowersCount(FollowersCountRequest request) {
        String userAlias = request.getTargetUserAlias();
        if(userAlias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a user alias");
        }
        int followersCount = getFollowDAO().getFollowersCount(userAlias);
        return new FollowersCountResponse(followersCount);
    }

    public IsFollowerResponse isFollower(IsFollowerRequest request) {
        String follower = request.getFollowerAlias();
        String followee = request.getFolloweeAlias();
        if(follower == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if(followee == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee alias");
        }
        return new IsFollowerResponse(getFollowDAO().isFollowing(follower, followee));
    }

    public FollowResponse follow(FollowRequest request) {
        String authTokenString = request.getAuthToken().getToken();
        getAuthTokenDAO().deleteOldEntries();
        if(request.getFolloweeAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee alias");
        } else if (!getAuthTokenDAO().validAuthToken(authTokenString)) {
            throw new RuntimeException("[BadRequest] AuthToken timed out");
        }
        AuthToken authToken = request.getAuthToken();
        String follower = getAuthTokenDAO().getUserAlias(authToken);
        String followee = request.getFolloweeAlias();
        getFollowDAO().addFollow(follower, followee);
        return new FollowResponse();
    }

    public UnfollowResponse unfollow(UnfollowRequest request) {
        String authTokenString = request.getAuthToken().getToken();
        getAuthTokenDAO().deleteOldEntries();
        if(request.getFolloweeAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee alias");
        } else if (!getAuthTokenDAO().validAuthToken(authTokenString)) {
            throw new RuntimeException("[BadRequest] AuthToken timed out");
        }
        AuthToken authToken = request.getAuthToken();
        String follower = getAuthTokenDAO().getUserAlias(authToken);
        String followee = request.getFolloweeAlias();
        getFollowDAO().deleteFollow(follower, followee);
        return new UnfollowResponse();
    }

    private int getFolloweesStartingIndex(String lastFolloweeAlias, List<User> allFollowees) {

        int followeesIndex = 0;

        if(lastFolloweeAlias != null) {
            // This is a paged request for something after the first page. Find the first item
            // we should return
            for (int i = 0; i < allFollowees.size(); i++) {
                if(lastFolloweeAlias.equals(allFollowees.get(i).getAlias())) {
                    // We found the index of the last item returned last time. Increment to get
                    // to the first one we should return
                    followeesIndex = i + 1;
                    break;
                }
            }
        }

        return followeesIndex;
    }

    private int getFollowersStartingIndex(String lastFollowerAlias, List<User> allFollowers) {

        int followersIndex = 0;

        if(lastFollowerAlias != null) {
            // This is a paged request for something after the first page. Find the first item
            // we should return
            for (int i = 0; i < allFollowers.size(); i++) {
                if(lastFollowerAlias.equals(allFollowers.get(i).getAlias())) {
                    // We found the index of the last item returned last time. Increment to get
                    // to the first one we should return
                    followersIndex = i + 1;
                    break;
                }
            }
        }

        return followersIndex;
    }

    List<User> getDummyFollowees() {
        return getFakeData().getFakeUsers();
    }

    List<User> getDummyFollowers() {
        return getFakeData().getFakeUsers();
    }

    FakeData getFakeData() {
        return new FakeData();
    }
}
