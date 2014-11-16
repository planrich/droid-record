package at.pasra.record.remote;

import android.os.AsyncTask;
import android.util.Base64;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;

/**
 * Created by rich on 10.11.14.
 */
public class RemoteRequest extends AsyncTask<Void, Void, RemoteResponse> {

    private final String route;
    private final ApplicationContext context;
    private final HttpMethod method;
    private final RemoteState state;

    public RemoteRequest(HttpMethod method, String route, RemoteState state) {
        this.method = method;
        this.route = route;
        this.context = state.context;
        this.state = state;
    }

    @Override
    protected RemoteResponse doInBackground(Void... params) {

        HttpRequestBase httpRequest = method.freshRequest(route);

        if (state.useAuth) {
            //httpRequest.setHeader("Authorization", "Basic " + basicAuthBase64(context.getApplication().getBasicAuth()));
        }

        try {
            final HttpHost host = context.getHost();
            final HttpClient client = context.getClient();
            HttpResponse httpResponse = client.execute(host, httpRequest);

            return new RemoteResponse(httpResponse);
        } catch (IOException e) {
        }

        return null;
    }

    private String basicAuthBase64(String basicAuth) {
        return Base64.encodeToString(basicAuth.getBytes(), Base64.DEFAULT);
    }

    private void respond(RemoteResponse response) {
        RemoteCallback userCallback = state.getUserCallback();
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

    @Override
    protected void onPostExecute(RemoteResponse response) {
        if (response == null) {
            response = new RemoteResponse(null);
        }
        respond(response);
    }
}
