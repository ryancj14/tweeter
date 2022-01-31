package edu.byu.cs.tweeter.client.presenter;

import android.text.Editable;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LoginTask;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

/**
 * The presenter for the login functionality of the application.
 */
public class LoginPresenter implements UserService.LoginObserver {

    private static final String LOG_TAG = "LoginPresenter";

    private final LoginPresenter.View view;

    private UserService userService;

    public LoginPresenter(View view) {
        this.view = view;
        userService = new UserService();
    }

    public interface View {
        void loginSuccessful(User user, AuthToken authToken);
        void loginUnsuccessful(String message);
        void informLoginReady();
    }

    public void onClick(String aliasStr, String passwordStr) {
        try {
            validateLogin(aliasStr);
            view.informLoginReady();

            // Send the login request.
            userService.login(aliasStr, passwordStr, this);
        } catch (Exception e) {
            view.loginUnsuccessful(e.getMessage());
        }
    }

    /**
     * Invoked when the login request completes if the login was successful. Notifies the view of
     * the successful login.
     *
     * @param user the logged-in user.
     * @param authToken the session auth token.
     */
    @Override
    public void handleSuccess(User user, AuthToken authToken) {
        // Cache user session information
        Cache.getInstance().setCurrUser(user);
        Cache.getInstance().setCurrUserAuthToken(authToken);

        view.loginSuccessful(user, authToken);
    }

    /**
     * Invoked when the login request completes if the login request was unsuccessful. Notifies the
     * view of the unsuccessful login.
     *
     * @param message error message.
     */
    @Override
    public void handleFailure(String message) {
        String errorMessage = "Failed to login: " + message;
        Log.e(LOG_TAG, errorMessage);
        view.loginUnsuccessful(errorMessage);
    }

    /**
     * A callback indicating that an exception occurred in an asynchronous method this class is
     * observing.
     *
     * @param exception the exception.
     */
    @Override
    public void handleException(Exception exception) {
        String errorMessage = "Failed to login because of exception: " + exception.getMessage();
        Log.e(LOG_TAG, errorMessage, exception);
        view.loginUnsuccessful(errorMessage);
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
}