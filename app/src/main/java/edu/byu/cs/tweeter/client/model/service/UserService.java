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
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LogoutTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.RegisterTask;
import edu.byu.cs.tweeter.client.presenter.FeedPresenter;
import edu.byu.cs.tweeter.client.presenter.FollowersPresenter;
import edu.byu.cs.tweeter.client.presenter.FollowingPresenter;
import edu.byu.cs.tweeter.client.presenter.RegisterPresenter;
import edu.byu.cs.tweeter.client.presenter.StoryPresenter;
import edu.byu.cs.tweeter.client.view.main.MainActivity;
import edu.byu.cs.tweeter.client.view.main.following.FollowingFragment;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class UserService {

    // GET USER: lines 29-82
    // (GetUserObserver interface,
    //  GetUser following function,
    //  GetUser followers function,
    //  GetUserHandler class)
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

    public void getUser(AuthToken currUserAuthToken, String userAliasStr, StoryPresenter.GetUserObserver getUserObserver) {
        GetUserTask getUserTask = new GetUserTask(currUserAuthToken,
                userAliasStr, new GetUserHandler(getUserObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getUserTask);
    }

    public void getUser(AuthToken currUserAuthToken, String userAliasStr, FeedPresenter.GetUserObserver getUserObserver) {
        GetUserTask getUserTask = new GetUserTask(currUserAuthToken,
                userAliasStr, new GetUserHandler(getUserObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getUserTask);
    }

    private class GetUserHandler extends Handler {

        private GetUserObserver observer;

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

    // Register lines 84-128
    // (RegisterObserver interface,
    //  Register registerFragment function,
    //  RegisterHandler class)
    public interface RegisterObserver {
        void handleSuccess(User registeredUser, AuthToken authToken);
        void handleFailure(String message);
        void handleException(Exception ex);
    }

    public void register(String firstName, String lastName, String alias, String password, String imageBytesBase64,
             RegisterPresenter.RegisterObserver registerObserver) {
        RegisterTask registerTask = new RegisterTask(firstName, lastName,
             alias, password, imageBytesBase64, new RegisterHandler(registerObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(registerTask);
    }

    private class RegisterHandler extends Handler {

        private RegisterObserver observer;

        private RegisterHandler(UserService.RegisterObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(RegisterTask.SUCCESS_KEY);
            if (success) {
                User registeredUser = (User) msg.getData().getSerializable(RegisterTask.USER_KEY);
                AuthToken authToken = (AuthToken) msg.getData().getSerializable(RegisterTask.AUTH_TOKEN_KEY);

                observer.handleSuccess(registeredUser, authToken);
            } else if (msg.getData().containsKey(RegisterTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(RegisterTask.MESSAGE_KEY);

                observer.handleFailure(message);
            } else if (msg.getData().containsKey(RegisterTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(RegisterTask.EXCEPTION_KEY);

                observer.handleException(ex);
            }
        }
    }

    // Login lines 130-1
    // (LoginObserver interface,
    //  login function (for login fragment),
    //  LoginHandler class)
    public interface LoginObserver {
        void handleSuccess(User loggedInUser, AuthToken authToken);
        void handleFailure(String message);
        void handleException(Exception exception);
    }

    public void login(String username, String password, LoginObserver observer) {
        LoginTask loginTask = new LoginTask(username, password, new LoginHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(loginTask);
    }

    private static class LoginHandler extends Handler {

        private LoginObserver observer;

        private LoginHandler(LoginObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(Message message) {
            boolean success = message.getData().getBoolean(LoginTask.SUCCESS_KEY);
            if (success) {
                User user = (User) message.getData().getSerializable(LoginTask.USER_KEY);
                AuthToken authToken = (AuthToken) message.getData().getSerializable(LoginTask.AUTH_TOKEN_KEY);

                observer.handleSuccess(user, authToken);
            } else if (message.getData().containsKey(LoginTask.MESSAGE_KEY)) {
                String errorMessage = message.getData().getString(LoginTask.MESSAGE_KEY);

                observer.handleFailure(errorMessage);
            } else if (message.getData().containsKey(LoginTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) message.getData().getSerializable(LoginTask.EXCEPTION_KEY);

                observer.handleException(ex);
            }
        }
    }

    // Logout lines 187-2
    // (LogoutObserver interface,
    //  logout function (for login fragment),
    //  LogoutHandler class)
    public interface LogoutObserver {
        void handleSuccess();
        void handleFailure(String message);
        void handleException(Exception ex);
    }

    public void logout(AuthToken currUserAuthToken, UserService.LogoutObserver logoutObserver) {
        LogoutTask logoutTask = new LogoutTask(currUserAuthToken, new UserService.LogoutHandler(logoutObserver));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(logoutTask);
    }

    private class LogoutHandler extends Handler {

        private LogoutObserver observer;

        private LogoutHandler(LogoutObserver observer) {
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(LogoutTask.SUCCESS_KEY);
            if (success) {
                observer.handleSuccess();
            } else if (msg.getData().containsKey(LogoutTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(LogoutTask.MESSAGE_KEY);

                observer.handleFailure(message);
            } else if (msg.getData().containsKey(LogoutTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(LogoutTask.EXCEPTION_KEY);

                observer.handleException(ex);
            }
        }
    }







}
