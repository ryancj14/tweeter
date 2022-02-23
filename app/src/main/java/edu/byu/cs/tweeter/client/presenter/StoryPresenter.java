package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.presenter.view.PagedView;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class StoryPresenter extends PagedPresenter<Status> {

    private StatusService statusService;

    public StoryPresenter(PagedView<Status> view) {
        super();
        this.view = view;
        this.statusService = new StatusService();
    }

    @Override
    public void getItems(AuthToken authToken, User user, int pageSize, Status lastItem) {
        statusService.getStory(authToken, user, pageSize, lastItem, new PageObserver());
    }

    @Override
    public String getDescription() {
        return "story";
    }

}
