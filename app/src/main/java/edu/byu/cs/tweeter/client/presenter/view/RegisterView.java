package edu.byu.cs.tweeter.client.presenter.view;

import edu.byu.cs.tweeter.model.domain.User;

public interface RegisterView extends View {
    void informRegisterReady();
    void registerSuccess(User registeredUser, String name);
    void notifyException(String message);
}
