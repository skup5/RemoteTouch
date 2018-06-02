package cz.zelenikr.remotetouch.network;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpResponse;

import java.net.URL;

import cz.zelenikr.remotetouch.data.message.MessageDTO;

/**
 * Provides unsecured (content unencrypted) JSON message sending to the REST server.
 *
 * @author Roman Zelenik
 */
public class JsonSimpleRestClient extends BaseJsonRestClient {

    private static final String CLASS_NAME = JsonSimpleRestClient.class.getSimpleName();

    /**
     * @param clientToken The client identification token.
     * @param baseRestUrl The base server url (like https://myserver.com).
     * @param context
     */
    public JsonSimpleRestClient(String clientToken, URL baseRestUrl, Context context) {
        super(clientToken, baseRestUrl, context);
    }

    @Override
    protected HttpContent makeJSONContent(MessageDTO message) {
        String json = toJson(message);
        //System.out.println(json);
        HttpContent httpContent = new ByteArrayContent("application/json", json.getBytes());
        return httpContent;

    }

    @Override
    protected String getClassName() {
        return CLASS_NAME;
    }

    @Override
    protected boolean onSuccessResponse(HttpResponse response) {
        return true;
    }

    @Override
    protected void onErrorResponse(HttpResponse response) {
        Log.w(getClassName(), response.getStatusCode() + " - " + response.getStatusMessage());
    }

}
