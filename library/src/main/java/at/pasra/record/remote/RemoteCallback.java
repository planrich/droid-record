package at.pasra.record.remote;

/**
 * @author rich
 * 10.11.14
 */
public interface RemoteCallback {

    /**
     * The request was a great success. 2XX was returned by the server.
     *
     * Executed on the UI thread. You can modify the UI here.
     * This method should not be blocking.
     *
     * @param response never null
     */
    public void onRequestOk(RemoteResponse response);

    /**
     * The request failed. Either 5XX or 4XX might the cause of this.
     *
     * Executed on the UI thread. You can modify the UI here.
     * This method should not be blocking.
     *
     * @param response never null
     */
    public void onRequestFailed(RemoteResponse response);
}
