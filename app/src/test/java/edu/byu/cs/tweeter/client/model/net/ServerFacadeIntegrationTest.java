package edu.byu.cs.tweeter.client.model.net;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.net.TweeterRemoteException;
import edu.byu.cs.tweeter.model.net.request.FollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;

public class ServerFacadeIntegrationTest {

    ServerFacade serverFacade;

    @Before
    public void setup() {
        serverFacade = new ServerFacade();
    }

    @Test
    public void testRegister() {
        try {
            RegisterResponse response = serverFacade.register(new RegisterRequest("a","a","A","B","IMAGE"), "/register");
            assert response.isSuccess();
        } catch (IOException | TweeterRemoteException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void testGetFollowers() {
        try {
            FollowersResponse response = serverFacade.getFollowers(new FollowersRequest(new AuthToken(), "a",  20, null), "/getfollowers");
            assert response.isSuccess();
            assert response.getHasMorePages();
        } catch (IOException | TweeterRemoteException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void testGetStory() {
        try {
            StoryResponse response = serverFacade.getStory(new StoryRequest(new AuthToken(), "a",  20, null), "/getstory");
            assert response.isSuccess();
            assert response.getHasMorePages();
        } catch (IOException | TweeterRemoteException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void testGetFollowersCount() {
        try {
            FollowersCountResponse response = serverFacade.getFollowersCount(new FollowersCountRequest(new AuthToken(), "a"), "/getfollowerscount");
            assert response.isSuccess();
            assert response.getCount() == 21;
        } catch (IOException | TweeterRemoteException e) {
            e.printStackTrace();
            assert false;
        }
    }


}
