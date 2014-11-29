package at.pasra.record.remote;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

/**
 * @author rich
 * 23.11.14
 */
public class ExceptionResponse extends RemoteResponse {
    private Throwable throwable;
    public ExceptionResponse(Throwable throwable) {
        super(null);
        this.throwable = throwable;
    }
}
