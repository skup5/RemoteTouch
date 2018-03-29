package cz.zelenikr.remotetouch.network;

import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;

import cz.zelenikr.remotetouch.data.message.MessageContent;
import cz.zelenikr.remotetouch.data.message.MessageDTO;

/**
 * Abstract base class to sending JSON requests to the REST server.
 *
 * @author Roman Zelenik
 */
abstract class BaseJsonRestClient implements RestClient {
    protected static final Gson GSON = new Gson();
    private final HttpTransport httpTransport = new NetHttpTransport();
    protected String clientToken;
    protected URL baseRestUrl;

    BaseJsonRestClient(String clientToken, URL baseRestUrl) {
        this.clientToken = clientToken;
        this.baseRestUrl = baseRestUrl;
    }

    protected static String toJson(Object object) {
        return GSON.toJson(object);
    }

    @Override
    public boolean send(String msg, String path) {
        return postRequest(path, makeJSONContent(new MessageDTO(clientToken, msg)));
    }

    @Override
    public boolean send(MessageContent content, String path) {
        return postRequest(path, makeJSONContent(new MessageDTO(clientToken, content)));
    }

    @Override
    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    @Override
    public void setRestServer(URL url) {
        baseRestUrl = url;
    }

    /**
     * Prepares JSON content of {@link com.google.api.client.http.HttpRequest} from specific {@link MessageDTO}.
     *
     * @param message the given message to converting to JSON
     * @return message like a JSON content
     */
    protected abstract HttpContent makeJSONContent(MessageDTO message);

    protected abstract String getClassName();

    /**
     * Processes received response with success status code. Returns true if this response means
     * really successful request.
     *
     * @param response received successful response
     * @return true if processed response means success, false otherwise
     */
    protected abstract boolean onSuccessResponse(HttpResponse response);

    /**
     * Processes received response with non-success status code.
     *
     * @param response received error response
     */
    protected abstract void onErrorResponse(HttpResponse response);

    private boolean postRequest(String subUrl, HttpContent httpContent) {
        boolean success = false;
        try {
            Log.i(getClassName(), "post request");
            GenericUrl restUrl = new GenericUrl(baseRestUrl);
            if (subUrl != null) {
                if (!subUrl.startsWith("/")) {
                    subUrl = "/" + subUrl;
                }
                restUrl.appendRawPath(subUrl);
            }
            Log.i(getClassName(), "send to " + restUrl);
            HttpResponse httpResponse = httpTransport.createRequestFactory()
                .buildPostRequest(restUrl, httpContent)
                .setThrowExceptionOnExecuteError(false)
                .execute();
            try {
                if (httpResponse.isSuccessStatusCode()) {
                    success = onSuccessResponse(httpResponse);
                } else {
                    onErrorResponse(httpResponse);
                }
            } finally {
                httpResponse.disconnect();
            }
        } catch (IOException e) {
//                e.printStackTrace();
            Log.w(getClassName(), e.toString());
        }
        return success;
    }
}
