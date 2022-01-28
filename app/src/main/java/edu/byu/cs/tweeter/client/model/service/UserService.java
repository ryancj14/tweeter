package edu.byu.cs.tweeter.client.model.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LoginTask;
import edu.byu.cs.tweeter.client.presenter.FollowersPresenter;
import edu.byu.cs.tweeter.client.presenter.FollowingPresenter;
import edu.byu.cs.tweeter.client.view.main.MainActivity;
import edu.byu.cs.tweeter.client.view.main.following.FollowingFragment;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class UserService {

    public interface GetUserObserver {
        void handleSuccess(User user);
        void handleFailure(String message);
        void handleException(Exception ex);
    }

    public void getUser(AuthToken currUserAuthToken, String userAliasStr, FollowingPresenter.GetUserObserver getUserObserver) {
        GetUserTask getUserTask = new GetUserTask(currUserAuthToken,
                userAliasStr, new GetUserHandler(getUserObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getUserTask);
    }

    public void getUser(AuthToken currUserAuthToken, String userAliasStr, FollowersPresenter.GetUserObserver getUserObserver) {
        GetUserTask getUserTask = new GetUserTask(currUserAuthToken,
                userAliasStr, new GetUserHandler(getUserObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getUserTask);
    }

    /**
     * Message handler (i.e., observer) for GetUserTask.
     */
    private class GetUserHandler extends Handler {

        private UserService.GetUserObserver observer;

        private GetUserHandler(UserService.GetUserObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetUserTask.SUCCESS_KEY);
            if (success) {
                User user = (User) msg.getData().getSerializable(GetUserTask.USER_KEY);

                observer.handleSuccess(user);
            } else if (msg.getData().containsKey(GetUserTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetUserTask.MESSAGE_KEY);

                observer.handleFailure(message);
            } else if (msg.getData().containsKey(GetUserTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetUserTask.EXCEPTION_KEY);

                observer.handleException(ex);
            }
        }
    }

    /**
     * An observer interface to be implemented by observers who want to be notified when
     * asynchronous operations complete.
     */
    public interface LoginObserver {
        void handleSuccess(User user, AuthToken authToken);
        void handleFailure(String message);
        void handleException(Exception exception);
    }

    /**
     * Makes an asynchronous login request.
     *
     * @param username the user's name.
     * @param password the user's password.
     */
    public void login(String username, String password, LoginObserver observer) {
        LoginTask loginTask = getLoginTask(username, password, observer);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(loginTask);
    }

    LoginTask getLoginTask(String username, String password, LoginObserver observer) {
        return new LoginTask(username, password, new LoginHandler(observer));
    }


    /**
     * Handles messages from the background task indicating that the task is done, by invoking
     * methods on the observer.
     */
    private static class LoginHandler extends Handler {

        private final LoginObserver observer;

        LoginHandler(LoginObserver observer) {
            super(Looper.getMainLooper());
            this.observer = observer;
        }

        @Override
        public void handleMessage(Message message) {
            Bundle bundle = message.getData();
            boolean success = bundle.getBoolean(LoginTask.SUCCESS_KEY);
            if (success) {
                User user = (User) bundle.getSerializable(LoginTask.USER_KEY);
                AuthToken authToken = (AuthToken) bundle.getSerializable(LoginTask.AUTH_TOKEN_KEY);
                observer.handleSuccess(user, authToken);
            } else if (bundle.containsKey(LoginTask.MESSAGE_KEY)) {
                String errorMessage = bundle.getString(LoginTask.MESSAGE_KEY);
                observer.handleFailure(errorMessage);
            } else if (bundle.containsKey(LoginTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) bundle.getSerializable(LoginTask.EXCEPTION_KEY);
                observer.handleException(ex);
            }
        }
    }

}
