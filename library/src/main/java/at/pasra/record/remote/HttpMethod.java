package at.pasra.record.remote;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Created by rich on 10.11.14.
 */
public enum HttpMethod {
    GET,
    POST,
    UPDATE,
    DELETE;

    public HttpRequestBase freshRequest(String url) {
        switch (this) {
            case GET: return new HttpGet(url);
            case POST: return new HttpPost(url);
            default: return null;
        }
    }

}
