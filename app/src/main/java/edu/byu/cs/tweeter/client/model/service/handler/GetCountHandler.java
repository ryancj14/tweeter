package edu.byu.cs.tweeter.client.model.service.handler;

import android.os.Bundle;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingCountTask;
import edu.byu.cs.tweeter.client.model.service.observer.GetCountObserver;

public class GetCountHandler extends BackgroundTaskHandler<GetCountObserver> {

    public GetCountHandler(GetCountObserver observer) {
        super(observer);
    }

    @Override
    protected void handleSuccess(Bundle data, GetCountObserver observer) {
        int count = data.getInt(GetFollowingCountTask.COUNT_KEY);
        observer.handleSuccess(count);
    }

}
