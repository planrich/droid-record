package at.pasra.record.remote;


import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;

/**
 * @author rich
 * 16.11.14
 */
public interface ApplicationContext {

    public HttpHost getHost();

    public HttpClient getClient();

    Credentials getCredentials();
}
