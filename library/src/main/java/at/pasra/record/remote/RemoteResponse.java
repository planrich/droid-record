package at.pasra.record.remote;

import com.google.gson.JsonArray;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by rich on 10.11.14.
 */
public class RemoteResponse {
    private final HttpResponse response;
    private byte[] bytes;

    public RemoteResponse(HttpResponse httpResponse) {
        this.response = httpResponse;
        if (response != null) {
            try {
                InputStream inputStream = this.response.getEntity().getContent();
                BufferedInputStream bin = new BufferedInputStream(inputStream);
                byte[] tmp = new byte[512];
                int read = 0;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                while ((read = bin.read(tmp)) != -1) {
                    output.write(tmp, 0, read);
                }

                bytes = output.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int getStatusCode() {
        if (response == null || response.getStatusLine() == null) {
            return -1;
        } else {
            return response.getStatusLine().getStatusCode();
        }
    }

    public JsonElement getJson() {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(new String(bytes));
        return element;
    }

    public JsonObject getJsonObject() {
        return getJson().getAsJsonObject();
    }

    public JsonArray getJsonArray() {
        return getJson().getAsJsonArray();
    }
}
