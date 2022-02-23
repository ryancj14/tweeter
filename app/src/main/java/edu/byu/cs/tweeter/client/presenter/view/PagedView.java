package edu.byu.cs.tweeter.client.presenter.view;

import java.util.List;
import edu.byu.cs.tweeter.model.domain.User;

public interface PagedView<T> extends View {
    void addItems(List<T> story);
    void setLoadingStatus(boolean b);
    void navigateToUser(User user);
}
