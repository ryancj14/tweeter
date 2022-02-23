package edu.byu.cs.tweeter.client.model.service.handler;

import android.os.Bundle;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.AuthenticateTask;
import edu.byu.cs.tweeter.client.model.service.observer.AuthenticateTaskObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class AuthenticateTaskHandler extends BackgroundTaskHandler<AuthenticateTaskObserver> {
    public AuthenticateTaskHandler(AuthenticateTaskObserver observer) {
        super(observer);
    }

    @Override
    protected void handleSuccess(Bundle data, AuthenticateTaskObserver observer) {
        User user = (User) data.getSerializable(AuthenticateTask.USER_KEY);
        AuthToken authToken = (AuthToken) data.getSerializable(AuthenticateTask.AUTH_TOKEN_KEY);
        observer.handleSuccess(user, authToken);
    }

}
