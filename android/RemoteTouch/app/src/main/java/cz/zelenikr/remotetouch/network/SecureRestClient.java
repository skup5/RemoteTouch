package cz.zelenikr.remotetouch.network;

import android.util.Log;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import cz.zelenikr.remotetouch.data.EventType;
import cz.zelenikr.remotetouch.data.dto.EventContent;
import cz.zelenikr.remotetouch.data.dto.MessageDTO;
import cz.zelenikr.remotetouch.helper.SecurityHelper;
import cz.zelenikr.remotetouch.security.SymmetricCipher;

/**
 * Provides secured JSON message sending to the REST server.
 *
 * @author Roman Zelenik
 */
public class SecureRestClient implements RestClient {
    private static final Gson GSON = new Gson();
    private static final String TAG = SecureRestClient.class.getSimpleName();

    private final String clientToken;
    private final HttpTransport httpTransport = new NetHttpTransport();
    private final URL baseRestUrl;
    private final SymmetricCipher symmetricCipher;

    /**
     * @param clientToken
     * @param baseRestUrl
     * @param secureKey   key (like a plain text) for encrypting/decrypting messages
     */
    public SecureRestClient(String clientToken, URL baseRestUrl, String secureKey) {
        this.clientToken = clientToken;
        this.baseRestUrl = baseRestUrl;
        this.symmetricCipher = SecurityHelper.createSymmetricCipherInstance(secureKey);
    }

    @Override
    public boolean send(String msg, EventType event) {
        return postRequest(null, makeJSONContent(new MessageDTO(clientToken, event, msg)));
    }

    @Override
    public boolean send(EventContent content, EventType event) {
        return postRequest(null, makeJSONContent(new MessageDTO(clientToken, event, content)));
    }

    private HttpContent makeJSONContent(MessageDTO message) {
        // Convert message content to JSON
        String contentJson = toJson(message.getContent());
        // Encrypt content JSON
        Serializable content = symmetricCipher.encrypt(contentJson);
        if (content == null) {
            Log.e(TAG, "Error on content encrypting");
            content = message.getContent();
        }
        // Create new MessageDTO with encrypted content
        message = new MessageDTO(message.getId(), message.getEvent(), content);
        String json = toJson(message);
        //System.out.println(json);
        Log.i(TAG, "New Message to " + message.getId());
        HttpContent httpContent = new ByteArrayContent("application/json", json.getBytes());
        return httpContent;
    }

    private boolean postRequest(String subUrl, HttpContent httpContent) {
        boolean success = false;
        try {
            Log.i(TAG, "post request");
            GenericUrl restUrl = new GenericUrl(baseRestUrl);
            if (subUrl != null) {
                if (!subUrl.startsWith("/")) {
                    subUrl = "/" + subUrl;
                }
                restUrl.appendRawPath(subUrl);
            }
            Log.i(TAG, "send to " + restUrl);
            HttpResponse httpResponse = httpTransport.createRequestFactory()
                .buildPostRequest(restUrl, httpContent)
                .setThrowExceptionOnExecuteError(false)
                .execute();
            try {
                if (httpResponse.isSuccessStatusCode()) {
                    success = true;
                } else {
                    Log.w(TAG, httpResponse.getStatusCode() + " - " + httpResponse.getStatusMessage() + ": " + httpResponse.parseAsString());
                }
            } finally {
                httpResponse.disconnect();
            }
        } catch (IOException e) {
//                e.printStackTrace();
            Log.w(TAG, e.toString());
        }
        return success;
    }

    private static String toJson(Object object) {
        return GSON.toJson(object);
    }
}
