package edu.byu.cs.tweeter.server.service;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;
import edu.byu.cs.tweeter.server.dao.AuthTokenDAO;
import edu.byu.cs.tweeter.server.dao.FeedDAO;
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.dao.StoryDAO;
import edu.byu.cs.tweeter.server.dao.UserDAO;
import edu.byu.cs.tweeter.util.FakeData;

/**
 * Contains the business logic for getting the users a user is following.
 */
public class StatusService {

    private static final String ReceiverAliasAttr = "receiverAlias";
    private static final String TimeStampAttr = "timestamp";
    private static final String PostTextAttr = "userAlias";
    private static final String PosterAliasAttr = "posterAlias";

    private UserDAO getUserDAO() {
        return new UserDAO();
    }

    private AuthTokenDAO getAuthTokenDAO() {
        return new AuthTokenDAO();
    }

    public FeedResponse getFeed(FeedRequest request) {
        String userAlias = request.getUserAlias();
        String lastItemAlias = request.getLastItemAlias();
        int limit = request.getLimit();
        String authTokenString = request.getAuthToken().getToken();
        getAuthTokenDAO().deleteOldEntries();
        if(userAlias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a user alias");
        } else if(lastItemAlias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a last item alias");
        } else if(limit <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        } else if(!getAuthTokenDAO().validAuthToken(authTokenString)) {
            throw new RuntimeException("[BadRequest] AuthToken timed out");
        }

        List<Map<String, AttributeValue>> items = getFeedDAO().getFeed(userAlias);
        List<Status> responseFeed = new ArrayList<>(limit);

        List<Status> feed = new ArrayList<>();
        if (items != null) {
            for (Map<String, AttributeValue> item : items){
                feed.add(getStatus(item));
            }
        }

        boolean hasMorePages = false;

        if (!feed.isEmpty()) {
            int feedIndex = getFeedStartingIndex(lastItemAlias, feed);

            for(int limitCounter = 0; feedIndex < feed.size() && limitCounter < limit; feedIndex++, limitCounter++) {
                responseFeed.add(feed.get(feedIndex));
            }

            hasMorePages = feedIndex < feed.size();
        }

        return new FeedResponse(responseFeed, hasMorePages);
    }

    private Status getStatus(Map<String, AttributeValue> item) {
        String posterAlias = item.get(PosterAliasAttr).getS();
        User poster = getUserDAO().getUser(posterAlias);
        String post = item.get(PostTextAttr).getS();
        String timestampStr = item.get(TimeStampAttr).getN();
        long timestamp = Long.parseLong(timestampStr);
        Date date = new java.util.Date(timestamp*1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        sdf.setTimeZone(java.util.TimeZone.getDefault());
        String formattedDate = sdf.format(date);
        List<String> urls = getURLs(post);
        List<String> mentions = getMentions(post);
        return new Status(post, poster, formattedDate, urls, mentions);
    }

    private List<String> getURLs(String post) {
        return getList(post, "http://");
    }

    private List<String> getMentions(String post) {
        return getList(post, "@");
    }


    private List<String> getList(String post, String s) {
        String postText = post;
        List<String> list = new ArrayList<>();
        while (postText.contains(s)) {
            postText = postText.substring(postText.indexOf(s));
            String item = postText.substring(0, postText.indexOf(" "));
            list.add(item);
        }
        return list;
    }

    public StoryResponse getStory(StoryRequest request) {
        String userAlias = request.getUserAlias();
        String lastItemAlias = request.getLastItemAlias();
        int limit = request.getLimit();
        if(userAlias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a user alias");
        } else if(lastItemAlias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a last item alias");
        } else if(limit <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        List<Map<String, AttributeValue>> items = getStoryDAO().getStory(userAlias);
        List<Status> responseStory = new ArrayList<>(limit);

        List<Status> story = new ArrayList<>();
        if (items != null) {
            for (Map<String, AttributeValue> item : items){
                story.add(getStatus(item));
            }
        }

        boolean hasMorePages = false;

        if (!story.isEmpty()) {
            int feedIndex = getFeedStartingIndex(lastItemAlias, story);

            for(int limitCounter = 0; feedIndex < story.size() && limitCounter < limit; feedIndex++, limitCounter++) {
                responseStory.add(story.get(feedIndex));
            }

            hasMorePages = feedIndex < story.size();
        }

        return new StoryResponse(responseStory, hasMorePages);
    }

    public PostStatusResponse postStatus(PostStatusRequest request) {
        Status status = request.getStatus();
        String authTokenString = request.getAuthToken().getToken();
        getAuthTokenDAO().deleteOldEntries();
        if (request.getStatus() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a status");
        } else if (!getAuthTokenDAO().validAuthToken(authTokenString)) {
            throw new RuntimeException("[BadRequest] AuthToken timed out");
        }
        String posterAlias = status.getUser().getAlias();
        String postText = status.getPost();
        long timestamp = Instant.now().getEpochSecond();
        getStoryDAO().addStatus(posterAlias, postText, timestamp);
        List<String> aliases = getFollowDAO().getFollowers(getAuthTokenDAO().getUserAlias(request.getAuthToken()));
        for (String receiverAlias : aliases) {
            getFeedDAO().addStatus(posterAlias, receiverAlias, postText, timestamp);
        }
        return new PostStatusResponse();
    }

    FeedDAO getFeedDAO() { return new FeedDAO(); }

    StoryDAO getStoryDAO() {
        return new StoryDAO();
    }

    FollowDAO getFollowDAO() {
        return new FollowDAO();
    }

    /**
     * Returns the list of dummy followee data. This is written as a separate method to allow
     * mocking of the followees.
     *
     * @return the followees.
     */
    List<Status> getDummyFeed() {
        return getFakeData().getFakeStatuses();
    }

    /**
     * Returns the {@link FakeData} object used to generate dummy followees.
     * This is written as a separate method to allow mocking of the {@link FakeData}.
     *
     * @return a {@link FakeData} instance.
     */
    FakeData getFakeData() {
        return new FakeData();
    }

    private int getFeedStartingIndex(String lastStatusAlias, List<Status> feed) {

        int feedIndex = 0;

        if(lastStatusAlias != null) {
            // This is a paged request for something after the first page. Find the first item
            // we should return
            for (int i = 0; i < feed.size(); i++) {
                if(lastStatusAlias.equals(feed.get(i).getDate())) {
                    // We found the index of the last item returned last time. Increment to get
                    // to the first one we should return
                    feedIndex = i + 1;
                    break;
                }
            }
        }

        return feedIndex;
    }
}