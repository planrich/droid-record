package at.pasra.record;

import org.apache.http.client.HttpClient;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by rich on 10/6/13.
 */
public abstract class AbstractRemoteSession {

    protected String host = null;
    protected int port = -1;
    protected String protocol = "http";

    public boolean postSyncronous(String uri, JSONObject object) {
        String json = object.toString();

        try {
            URL url = new URL(uri);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /*!
     * @remote|providing_paths Path parameters
     *
     */
    public String hostUrl(String path, Map<String, Object> param) {

        if (param != null && param.size() > 0) {

        }

        if (port > 0) {
            return String.format("%s://%s:%d/%s", protocol, host, port, path);
        } else {
            return String.format("%s://%s/%s", protocol, host, path);
        }
    }
}
