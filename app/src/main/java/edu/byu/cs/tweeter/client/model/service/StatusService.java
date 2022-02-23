package edu.byu.cs.tweeter.client.model.service;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFeedTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetStoryTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.PostStatusTask;
import edu.byu.cs.tweeter.client.model.service.handler.PagedTaskHandler;
import edu.byu.cs.tweeter.client.model.service.handler.SimpleTaskHandler;
import edu.byu.cs.tweeter.client.presenter.FeedPresenter;
import edu.byu.cs.tweeter.client.presenter.MainPresenter;
import edu.byu.cs.tweeter.client.presenter.PagedPresenter;
import edu.byu.cs.tweeter.client.presenter.StoryPresenter;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class StatusService extends Executes {

    // GET STORY
    // GetStory function (for StoryFragment)
    public void getStory(AuthToken currUserAuthToken, User user, int pageSize, Status lastStatus, PagedPresenter<Status>.PageObserver getStoryObserver) {
        GetStoryTask getStoryTask = new GetStoryTask(currUserAuthToken, user, pageSize, lastStatus,
                new PagedTaskHandler<>(getStoryObserver));
        execute(getStoryTask);
    }

    // GET FEED
    // GetFeed function (for StoryFragment)
    public void getFeed(AuthToken currUserAuthToken, User user, int pageSize, Status lastStatus, PagedPresenter<Status>.PageObserver getFeedObserver) {
        GetFeedTask getFeedTask = new GetFeedTask(currUserAuthToken, user, pageSize, lastStatus,
                new PagedTaskHandler<>(getFeedObserver));
        execute(getFeedTask);
    }

    // POST STATUS
    // postStatus function (for MainActivity)
    public void postStatus(AuthToken currUserAuthToken, Status newStatus, MainPresenter.PostStatusObserver postStatusObserver) {
        PostStatusTask statusTask = new PostStatusTask(currUserAuthToken, newStatus,
                new SimpleTaskHandler(postStatusObserver));
        execute(statusTask);
    }

}
