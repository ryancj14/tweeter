package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.presenter.view.PagedView;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowingPresenter extends PagedPresenter<User> {

    private FollowService followService;

    public FollowingPresenter(PagedView<User> view) {
        super();
        this.view = view;
        this.followService = new FollowService();
    }

    @Override
    public void getItems(AuthToken authToken, User user, int pageSize, User lastItem) {
        followService.getFollowing(authToken, user, pageSize, lastItem, new PageObserver());
    }

    @Override
    public String getDescription() {
        return "followees";
    }

}
