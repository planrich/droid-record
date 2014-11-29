package at.pasra.record.remote;


import org.apache.http.auth.Credentials;

/**
 * @author rich
 * 10.11.14
 */
public abstract class RemoteState implements RemoteCallback {

    protected RemoteCallback callback;
    protected ApplicationContext context;

    public RemoteState(ApplicationContext ctx) {
        this.context = ctx;
    }

    public abstract RemoteRequest prepare();

    /**
     * Provide your web app credentials here. Per default the credentials
     * from the app context are returned.
     *
     * @return null, if no auth. should be added, credentials instead
     */
    public Credentials getCredentials() {
        return context.getCredentials();
    }

    public void setCallback(RemoteCallback callback) {
        this.callback = callback;
    }

    public RemoteCallback getCallback() {
        return callback;
    }
}
