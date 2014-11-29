package at.pasra.record.remote;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import java.io.IOException;

/**
 * Created by rich on 10.11.14.
 */
public class RemoteRequest extends AsyncTask<Void, Void, RemoteResponse> {

    private final String route;
    private final ApplicationContext context;
    private final HttpMethod method;
    private final RemoteState state;
    private HttpHost host;
    private HttpClient client;

    public RemoteRequest(HttpMethod method, String route, RemoteState state) {
        this.method = method;
        this.route = RouteDSL.resolveDSL(route, this);
        this.context = state.context;
        this.state = state;
    }

    @Override
    protected RemoteResponse doInBackground(Void... params) {

        if (host == null) {
            host = context.getHost();
        }
        if (client == null) {
            client = context.getClient();
        }

        HttpRequestBase httpRequest = method.newRequest(route);

        Credentials credentials = state.getCredentials();
        if (credentials != null) {
            if (credentials instanceof UsernamePasswordCredentials) {
                UsernamePasswordCredentials upc = (UsernamePasswordCredentials)credentials;
                String upw = String.format("%s:%s", upc.getUserName(), upc.getPassword());
                String base64 = Base64.encodeToString(upw.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
                httpRequest.setHeader("Authorization", "Basic " + base64);
            }
        }

        try {
            HttpResponse httpResponse = client.execute(host, httpRequest);

            return new RemoteResponse(httpResponse);
        } catch (ClientProtocolException e) {
            Log.wtf("DR", "protocol exception", e);
            return new ExceptionResponse(e);
        } catch (IOException e) {
            return new ExceptionResponse(e);
        }
    }

    @Override
    protected void onPostExecute(RemoteResponse response) {
        RemoteCallback userCallback = state.getCallback();
        if (response.getStatusCode() == 200) {
            state.onRequestOk(response);
            if (userCallback != null) {
                userCallback.onRequestOk(response);
            }
        } else {
            state.onRequestFailed(response);
            if (userCallback != null) {
                userCallback.onRequestFailed(response);
            }
        }
    }
}
