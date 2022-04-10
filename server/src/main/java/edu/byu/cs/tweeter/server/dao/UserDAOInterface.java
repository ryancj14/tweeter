package edu.byu.cs.tweeter.server.dao;

import edu.byu.cs.tweeter.model.domain.User;

public interface UserDAOInterface extends DAOInterface {
    public void addUser(String userAlias, String password, String firstName, String lastName, String image);

    public boolean invalidPassword(String userAlias, String password);

    public User getUser(String userAlias);
}
