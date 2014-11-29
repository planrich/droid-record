package at.pasra.record.remote;

import com.google.gson.JsonArray;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author rich
 * 10.11.14
 */
public class RemoteResponse {
    private final HttpResponse response;
    private byte[] byteBody;
    private String stringBody;
    private JsonElement jsonElementBody;

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

                byteBody = output.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            byteBody = new byte[0];
        }
    }

    /**
     * @return the status code from the webserver. -1 if the request did not reach the server at all
     */
    public int getStatusCode() {
        if (response == null || response.getStatusLine() == null) {
            return -1;
        } else {
            return response.getStatusLine().getStatusCode();
        }
    }

    /**
     * @return The body as utf-8 encoded string.
     */
    public String getStringBody() {
        if (stringBody == null) {
            stringBody = new String(byteBody);
        }
        return stringBody;
    }

    /**
     * @return a json element. most likely an object/array.
     * @throws com.google.gson.JsonParseException
     */
    public JsonElement getJsonBody() {
        if (jsonElementBody == null) {
            JsonParser parser = new JsonParser();
            jsonElementBody = parser.parse(new String(byteBody));
        }
        return jsonElementBody;
    }

    /**
     * @return a json object. if this request is not a json response
     *         a <code>JsonParseException</code> will be thrown.
     *         never returns null. in case it is not an object, an empty
     *         object is returned
     * @throws com.google.gson.JsonParseException
     */
    public JsonObject getJsonObjectBody() {
        JsonElement element = getJsonBody();
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        return new JsonObject();
    }

    /**
     * @return a json array. if this request is not a json response
     *         a <code>JsonParseException</code> will be thrown.
     *         never returns null. in case it is not an array, an empty
     *         array is returned
     * @throws com.google.gson.JsonParseException
     */
    public JsonArray getJsonArrayBody() {
        JsonElement element = getJsonBody();
        if (element.isJsonArray()) {
            return element.getAsJsonArray();
        }
        return new JsonArray();
    }
}
