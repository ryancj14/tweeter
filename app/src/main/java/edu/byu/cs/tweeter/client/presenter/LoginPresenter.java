package edu.byu.cs.tweeter.client.presenter;

import android.util.Log;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.observer.AuthenticateTaskObserver;
import edu.byu.cs.tweeter.client.presenter.view.LoginView;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

/**
 * The presenter for the login functionality of the application.
 */
public class LoginPresenter extends Presenter<LoginView> {

    private static final String LOG_TAG = "LoginPresenter";

    private final UserService userService;

    public LoginPresenter(LoginView view) {
        this.view = view;
        userService = new UserService();
    }

    public void onClick(String aliasStr, String passwordStr) {
        try {
            validateLogin(aliasStr);
            view.informLoginReady();

            // Send the login request.
            userService.login(aliasStr, passwordStr, new LoginObserver());
        } catch (Exception e) {
            view.displayMessage(e.getMessage());
        }
    }

    public void validateLogin(String aliasStr) {
        if (aliasStr.charAt(0) != '@') {
            throw new IllegalArgumentException("Alias must begin with @.");
        }
        if (aliasStr.length() < 2) {
            throw new IllegalArgumentException("Alias must contain 1 or more characters after the @.");
        }
        if (aliasStr.length() == 0) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
    }

    public class LoginObserver implements AuthenticateTaskObserver {
        @Override
        public void handleSuccess(User user, AuthToken authToken) {
            // Cache user session information
            Cache.getInstance().setCurrUser(user);
            Cache.getInstance().setCurrUserAuthToken(authToken);

            view.loginSuccess(user, authToken);
        }

        @Override
        public void handleFailure(String message) {
            String errorMessage = "Failed to login: " + message;
            Log.e(LOG_TAG, errorMessage);
            view.displayMessage(errorMessage);
        }

        @Override
        public void handleException(Exception exception) {
            String errorMessage = "Failed to login because of exception: " + exception.getMessage();
            Log.e(LOG_TAG, errorMessage, exception);
            view.displayMessage(errorMessage);
        }
    }
}