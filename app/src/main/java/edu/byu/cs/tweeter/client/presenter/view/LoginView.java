package edu.byu.cs.tweeter.client.presenter.view;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public interface LoginView extends View {
    void informLoginReady();
    void loginSuccess(User user, AuthToken authToken);
}
