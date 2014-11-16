package at.pasra.record.remote.serialize;

/**
 * Created by rich on 13.11.14.
 */
public class SerializeException extends RuntimeException {

    public SerializeException() {
    }

    public SerializeException(String detailMessage) {
        super(detailMessage);
    }

    public SerializeException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SerializeException(Throwable throwable) {
        super(throwable);
    }
}
