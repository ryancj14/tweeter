package edu.byu.cs.tweeter.client.model.service.handler;

import android.os.Bundle;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.model.service.observer.UserObserver;
import edu.byu.cs.tweeter.model.domain.User;

public class GetUserHandler extends BackgroundTaskHandler<UserObserver> {
    public GetUserHandler(UserObserver observer) {
            super(observer);
        }

    @Override
    protected void handleSuccess(Bundle data, UserObserver observer) {
        User user = (User) data.getSerializable(GetUserTask.USER_KEY);
        observer.handleSuccess(user);
    }
}
