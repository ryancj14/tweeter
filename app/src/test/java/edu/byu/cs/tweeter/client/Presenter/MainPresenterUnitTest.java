package edu.byu.cs.tweeter.client.Presenter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.presenter.MainPresenter;
import edu.byu.cs.tweeter.client.presenter.view.MainView;

public class MainPresenterUnitTest {

    private MainView mockView;
    private UserService mockUserService;
    private StatusService mockStatusService;
    private Cache mockCache;

    private MainPresenter mainPresenterSpy;

    @Before
    public void setup() {
        // Create mocks
        mockView = Mockito.mock(MainView.class);
        mockUserService = Mockito.mock(UserService.class);
        mockStatusService = Mockito.mock(StatusService.class);
        mockCache = Mockito.mock(Cache.class);

        mainPresenterSpy = Mockito.spy(new MainPresenter(mockView));

        // Mockito.doReturn(mockStatusService).when(mainPresenterSpy).getStatusService();
        Mockito.when(mainPresenterSpy.getStatusService()).thenReturn(mockStatusService);

        Cache.setInstance(mockCache);
    }

    @Test
    public void testPostStatus_postSuccess() {
        Answer<Void> answer = new Answer<>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                MainPresenter.PostStatusObserver observer = invocation.getArgument(2, MainPresenter.PostStatusObserver.class);
                observer.handleSuccess();
                return null;
            }
        };

        callPostStatus(answer);

        Mockito.verify(mockView).closePostingToast();
        Mockito.verify(mockView).displayMessage("Successfully Posted!");
    }

    @Test
    public void testPostStatus_postFailure() {
        Answer<Void> answer = new Answer<>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                MainPresenter.PostStatusObserver observer = invocation.getArgument(2, MainPresenter.PostStatusObserver.class);
                observer.handleFailure("error message");
                return null;
            }
        };

        callPostStatus(answer);

        Mockito.verify(mockView, Mockito.times(0)).closePostingToast();
        Mockito.verify(mockView).displayMessage("Failed to post status: error message");
    }

    @Test
    public void testPostStatus_postException() {
        Answer<Void> answer = new Answer<>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                MainPresenter.PostStatusObserver observer = invocation.getArgument(2, MainPresenter.PostStatusObserver.class);
                observer.handleException(new Exception("exception message"));
                return null;
            }
        };

        callPostStatus(answer);

        Mockito.verify(mockView, Mockito.times(0)).closePostingToast();
        Mockito.verify(mockView).displayMessage("Failed to post status because of exception: exception message");
    }

    public void callPostStatus(Answer<Void> answer) {
        Mockito.doAnswer(answer).when(mockStatusService).postStatus(Mockito.any(), Mockito.any(), Mockito.any());
        mainPresenterSpy.postStatus("My New Post");
    }
}
