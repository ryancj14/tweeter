package edu.byu.cs.tweeter.client.model.service.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.FollowTask;
import edu.byu.cs.tweeter.client.model.service.observer.ServiceObserver;

public abstract class BackgroundTaskHandler<T extends ServiceObserver> extends Handler {

    private T observer;

    public BackgroundTaskHandler(T observer) {
        this.observer = observer;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        boolean success = msg.getData().getBoolean(FollowTask.SUCCESS_KEY);
        if (success) {
            handleSuccess(msg.getData(), observer);
        } else if (msg.getData().containsKey(FollowTask.MESSAGE_KEY)) {
            String message = msg.getData().getString(FollowTask.MESSAGE_KEY);

            observer.handleFailure(message);
        } else if (msg.getData().containsKey(FollowTask.EXCEPTION_KEY)) {
            Exception ex = (Exception) msg.getData().getSerializable(FollowTask.EXCEPTION_KEY);

            observer.handleException(ex);
        }
    }

    protected abstract void handleSuccess(Bundle data, T observer);
}
