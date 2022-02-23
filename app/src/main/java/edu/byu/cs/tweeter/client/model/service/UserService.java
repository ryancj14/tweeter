package edu.byu.cs.tweeter.client.model.service;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LoginTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LogoutTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.RegisterTask;
import edu.byu.cs.tweeter.client.model.service.handler.GetUserHandler;
import edu.byu.cs.tweeter.client.model.service.handler.SimpleTaskHandler;
import edu.byu.cs.tweeter.client.model.service.handler.AuthenticateTaskHandler;
import edu.byu.cs.tweeter.client.model.service.observer.UserObserver;
import edu.byu.cs.tweeter.client.model.service.observer.SimpleTaskObserver;
import edu.byu.cs.tweeter.client.model.service.observer.AuthenticateTaskObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;

public class UserService extends Executes {

    // GET USER: lines 29-82
    // (GetUserObserver interface,
    //  GetUser function
    //  GetUserHandler class)

    public void getUser(AuthToken currUserAuthToken, String userAliasStr, UserObserver getUserObserver) {
        GetUserTask getUserTask = new GetUserTask(currUserAuthToken,
                userAliasStr, new GetUserHandler(getUserObserver));
        execute(getUserTask);
    }

    // REGISTER
    // register function (for register fragment)
    public void register(String firstName, String lastName, String alias, String password, String imageBytesBase64,
             AuthenticateTaskObserver registerObserver) {
        RegisterTask registerTask = new RegisterTask(firstName, lastName,
             alias, password, imageBytesBase64, new AuthenticateTaskHandler(registerObserver));
        execute(registerTask);
    }

    // LOGIN
    // login function (for login fragment)
    public void login(String username, String password, AuthenticateTaskObserver observer) {
        LoginTask loginTask = new LoginTask(username, password, new AuthenticateTaskHandler(observer));
        execute(loginTask);
    }

    // LOGOUT
    // logout function (for login fragment)
    public void logout(AuthToken currUserAuthToken, SimpleTaskObserver logoutObserver) {
        LogoutTask logoutTask = new LogoutTask(currUserAuthToken, new SimpleTaskHandler(logoutObserver));
        execute(logoutTask);
    }

}
