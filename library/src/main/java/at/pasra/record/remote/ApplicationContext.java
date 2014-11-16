package at.pasra.record.remote;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;

/**
 * Created by rich on 16.11.14.
 */
public interface ApplicationContext {

    public HttpHost getHost();

    public HttpClient getClient();
}
