package edu.byu.cs.tweeter.model.net.response;

import java.util.List;
import java.util.Objects;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FeedRequest;

/**
 * A paged response for a {@link FeedRequest}.
 */
public class FeedResponse extends PagedResponse {

    private List<Status> items;

    /**
     * Creates a response indicating that the corresponding request was unsuccessful. Sets the
     * success and more pages indicators to false.
     *
     * @param message a message describing why the request was unsuccessful.
     */
    public FeedResponse(String message) {
        super(false, message, false);
    }

    /**
     * Creates a response indicating that the corresponding request was successful.
     *
     * @param items the followers to be included in the result.
     * @param hasMorePages an indicator of whether more data is available for the request.
     */
    public FeedResponse(List<Status> items, boolean hasMorePages) {
        super(true, hasMorePages);
        this.items = items;
    }

    /**
     * Returns the followers for the corresponding request.
     *
     * @return the followees.
     */
    public List<Status> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object param) {
        if (this == param) {
            return true;
        }

        if (param == null || getClass() != param.getClass()) {
            return false;
        }

        FeedResponse that = (FeedResponse) param;

        return (Objects.equals(items, that.items) &&
                Objects.equals(this.getMessage(), that.getMessage()) &&
                this.isSuccess() == that.isSuccess());
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }
}
