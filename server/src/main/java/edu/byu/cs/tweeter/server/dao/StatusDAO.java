package edu.byu.cs.tweeter.server.dao;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingResponse;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;
import edu.byu.cs.tweeter.util.FakeData;

/**
 * A DAO for accessing 'following' data from the database.
 */
public class StatusDAO {

    public PostStatusResponse postStatus(PostStatusRequest request) {
        // TODO: Generates dummy data. Replace with a real implementation.
        assert request.getStatus() != null;

        return new PostStatusResponse();
    }

    public FeedResponse getFeed(FeedRequest request) {
        // TODO: Generates dummy data. Replace with a real implementation.
        assert request.getLimit() > 0;
        assert request.getLastItemAlias() != null;

        List<Status> feed = getDummyFeed();
        List<Status> responseFeed = new ArrayList<>(request.getLimit());

        boolean hasMorePages = false;

        if(request.getLimit() > 0) {
            if (feed != null) {
                int feedIndex = getFeedStartingIndex(request.getLastItemAlias(), feed);

                for(int limitCounter = 0; feedIndex < feed.size() && limitCounter < request.getLimit(); feedIndex++, limitCounter++) {
                    responseFeed.add(feed.get(feedIndex));
                }

                hasMorePages = feedIndex < feed.size();
            }
        }

        return new FeedResponse(responseFeed, hasMorePages);
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

    public StoryResponse getStory(StoryRequest request) {
        // TODO: Generates dummy data. Replace with a real implementation.
        assert request.getLimit() > 0;
        assert request.getLastItemAlias() != null;

        List<Status> story = getDummyStory();
        List<Status> responseStory = new ArrayList<>(request.getLimit());

        boolean hasMorePages = false;

        if(request.getLimit() > 0) {
            if (story != null) {
                int storyIndex = getStoryStartingIndex(request.getLastItemAlias(), story);

                for(int limitCounter = 0; storyIndex < story.size() && limitCounter < request.getLimit(); storyIndex++, limitCounter++) {
                    responseStory.add(story.get(storyIndex));
                }

                hasMorePages = storyIndex < story.size();
            }
        }

        return new StoryResponse(responseStory, hasMorePages);
    }

    private int getStoryStartingIndex(String lastStatusAlias, List<Status> story) {

        int storyIndex = 0;

        if(lastStatusAlias != null) {
            // This is a paged request for something after the first page. Find the first item
            // we should return
            for (int i = 0; i < story.size(); i++) {
                if(lastStatusAlias.equals(story.get(i).getDate())) {
                    // We found the index of the last item returned last time. Increment to get
                    // to the first one we should return
                    storyIndex = i + 1;
                    break;
                }
            }
        }

        return storyIndex;
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
     * Returns the list of dummy followee data. This is written as a separate method to allow
     * mocking of the followees.
     *
     * @return the followers.
     */
    List<Status> getDummyStory() {
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
}
