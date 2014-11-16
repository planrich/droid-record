package at.pasra.record.remote;

/**
 * Created by rich on 10.11.14.
 */
public abstract class RemoteState implements RemoteCallback {

    protected RemoteCallback userCallback;
    protected ApplicationContext context;
    protected boolean useAuth;

    public RemoteState(ApplicationContext ctx) {
        this(ctx, false);
    }

    public RemoteState(ApplicationContext ctx, boolean useAuth) {
        this.context = ctx;
        //this.app = context.getApplication();
        this.useAuth = useAuth;
    }

    public abstract RemoteRequest invoke();

    public void setCallback(RemoteCallback callback) {
        this.userCallback = callback;
    }

    public RemoteCallback getUserCallback() {
        return userCallback;
    }
}
