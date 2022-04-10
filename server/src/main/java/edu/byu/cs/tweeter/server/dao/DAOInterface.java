package edu.byu.cs.tweeter.server.dao;

public interface DAOInterface {
    void createTable() throws DataAccessException;

    void deleteTable() throws DataAccessException;
}
