package at.pasra.record.remote;

/**
 * Created by rich on 22.11.14.
 */
public class RemoteException extends RuntimeException {

    public RemoteException() {
    }

    public RemoteException(String detailMessage) {
        super(detailMessage);
    }

    public RemoteException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RemoteException(Throwable throwable) {
        super(throwable);
    }
}
