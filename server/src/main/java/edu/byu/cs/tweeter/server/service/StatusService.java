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
import edu.byu.cs.tweeter.server.dao.AuthTokenDAOInterface;
import edu.byu.cs.tweeter.server.dao.FeedDAO;
import edu.byu.cs.tweeter.server.dao.FeedDAOInterface;
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.dao.FollowDAOInterface;
import edu.byu.cs.tweeter.server.dao.StoryDAO;
import edu.byu.cs.tweeter.server.dao.StoryDAOInterface;
import edu.byu.cs.tweeter.server.dao.UserDAO;
import edu.byu.cs.tweeter.server.dao.UserDAOInterface;

/**
 * Contains the business logic for getting the users a user is following.
 */
public class StatusService {

    private static final String TimeStampAttr = "timestamp";
    private static final String PostTextAttr = "userAlias";
    private static final String PosterAliasAttr = "posterAlias";

    public FeedResponse getFeed(FeedRequest request, AuthTokenDAOInterface authTable, FeedDAOInterface feedTable, UserDAOInterface userTable) {
        String userAlias = request.getUserAlias();
        String lastItemAlias = request.getLastItemAlias();
        int limit = request.getLimit();
        String authTokenString = request.getAuthToken().getToken();
        authTable.deleteOldEntries(authTokenString);
        if(userAlias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a user alias");
        } else if(limit <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        } else if(authTable.invalidAuthToken(authTokenString)) {
            throw new RuntimeException("[BadRequest] AuthToken timed out");
        }

        List<Map<String, AttributeValue>> items = feedTable.getFeed(userAlias);
        List<Status> responseFeed = new ArrayList<>(limit);

        List<Status> feed = new ArrayList<>();
        if (items != null) {
            for (Map<String, AttributeValue> item : items){
                feed.add(getStatus(item, userTable));
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

    private Status getStatus(Map<String, AttributeValue> item, UserDAOInterface userTable) {
        String posterAlias = item.get(PosterAliasAttr).getS();
        User poster = userTable.getUser(posterAlias);
        String post = item.get(PostTextAttr).getS();
        String timestampStr = item.get(TimeStampAttr).getN();
        long timestamp = Long.parseLong(timestampStr);
        Date date = new java.util.Date(timestamp*1000L);
        @SuppressWarnings("SimpleDateFormat")
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        sdf.setTimeZone(java.util.TimeZone.getDefault());
        String formattedDate = sdf.format(date);
        List<String> urls = getURLs(post);
        List<String> mentions = getMentions(post);
        return new Status(post, poster, formattedDate, urls, mentions);
    }

    public List<String> getURLs(String post) {
        return getList(post, "http://");
    }

    public List<String> getMentions(String post) {
        return getList(post, "@");
    }


    private List<String> getList(String post, String s) {
        String postText = post;
        List<String> list = new ArrayList<>();
        while (postText.contains(s)) {
            postText = postText.substring(postText.indexOf(s));
            String item;
            if (postText.contains(" ")) {
                item = postText.substring(0, postText.indexOf(" "));
                postText = postText.substring(postText.indexOf(" "));
            } else {
                item = postText;
                postText = "";
            }
            list.add(item);
        }
        return list;
    }

    public StoryResponse getStory(StoryRequest request, StoryDAOInterface storyTable, UserDAOInterface userTable) {
        String userAlias = request.getUserAlias();
        String lastItemAlias = request.getLastItemAlias();
        int limit = request.getLimit();
        if(userAlias == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a user alias");
        } else if(limit <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        List<Map<String, AttributeValue>> items = storyTable.getStory(userAlias);
        List<Status> responseStory = new ArrayList<>(limit);

        List<Status> story = new ArrayList<>();
        if (items != null) {
            for (Map<String, AttributeValue> item : items){
                story.add(getStatus(item, userTable));
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

    public PostStatusResponse postStatus(PostStatusRequest request, AuthTokenDAOInterface authTable, StoryDAOInterface storyTable, FollowDAOInterface followTable, FeedDAOInterface feedTable) {
        Status status = request.getStatus();
        String authTokenString = request.getAuthToken().getToken();
        authTable.deleteOldEntries(authTokenString);
        if (request.getStatus() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a status");
        } else if (authTable.invalidAuthToken(authTokenString)) {
            throw new RuntimeException("[BadRequest] AuthToken timed out");
        }
        String posterAlias = status.getUser().getAlias();
        String postText = status.getPost();
        long timestamp = Instant.now().getEpochSecond();
        storyTable.addStatus(posterAlias, postText, timestamp);
        List<String> aliases = followTable.getFollowers(posterAlias);
        for (String receiverAlias : aliases) {
            feedTable.addStatus(posterAlias, receiverAlias, postText, timestamp);
        }
        return new PostStatusResponse();
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