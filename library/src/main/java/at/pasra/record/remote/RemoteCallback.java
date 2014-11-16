package at.pasra.record.remote;

/**
 * Created by rich on 10.11.14.
 */
public interface RemoteCallback {

    public void onRequestOk(RemoteResponse response);

    public void onRequestFailed(RemoteResponse response);
}
