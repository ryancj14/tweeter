//package edu.byu.cs.tweeter.client.model.service.backgroundTask;
//
//import Android.os.Handler;
//import edu.byu.cs.tweeter.model.domain.AuthToken;
//import edu.byu.cs.tweeter.model.domain.User;
//
//public abstract class PagedTask<T> extends AuthorizedTask {
//
//    public static final String FOLLOWEES_KEY = "followees";
//    public static final String MORE_PAGES_KEY = "more-pages";
//
//    /**
//     * Maximum number of followed users to return (i.e., page size).
//     */
//    private int limit;
//    /**
//     * The last person being followed returned in the previous page of results (can be null).
//     * This allows the new page to begin where the previous page ended.
//     */
//    private T lastItem;
//
//    public PagedTask(Handler messageHandler, AuthToken authToken)
//}
