package cz.zelenikr.remotetouch.network;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpResponse;

import java.io.IOException;
import java.net.URL;

import cz.zelenikr.remotetouch.data.message.MessageDTO;
import cz.zelenikr.remotetouch.helper.SecurityHelper;
import cz.zelenikr.remotetouch.security.SymmetricCipher;

/**
 * Provides secured (content encrypted) JSON message sending to the REST server.
 *
 * @author Roman Zelenik
 */
public class JsonSecureRestClient extends BaseJsonRestClient implements SecureRestClient {
    private static final String TAG = JsonSecureRestClient.class.getSimpleName();

    /**
     * It's used to content encryption.
     */
    private final SymmetricCipher<String> symmetricCipher;

    /**
     * @param clientToken The client identification token.
     * @param baseRestUrl The base server url (like https://myserver.com).
     * @param secureKey   key (like a plain text) for encrypting/decrypting messages
     * @param context
     */
    public JsonSecureRestClient(String clientToken, URL baseRestUrl, String secureKey, Context context) {
        super(clientToken, baseRestUrl, context);
        this.symmetricCipher = SecurityHelper.createSymmetricCipherInstance(secureKey);
    }

    @Override
    public void setSecureKey(String secureKey) {
        symmetricCipher.changeKey(secureKey);
    }

    @Override
    protected HttpContent makeJSONContent(MessageDTO message) {
        // Convert message content to JSON
        String contentJson = toJson(message.getContent());
        // Encrypt content JSON
        String content = symmetricCipher.encrypt(contentJson);
        if (content == null) {
            Log.e(TAG, "Error on content encrypting");
//            content = message.getContent();
            content = "";
        }
        // Create new MessageDTO with encrypted content
        message = new MessageDTO(message.getId(), content);
        String json = toJson(message);
        //System.out.println(json);
        Log.i(TAG, "New Message to " + message.getId());
        HttpContent httpContent = new ByteArrayContent("application/json", json.getBytes());
        return httpContent;
    }

    @Override
    protected String getClassName() {
        return TAG;
    }

    @Override
    protected boolean onSuccessResponse(HttpResponse response) {
        return true;
    }

    @Override
    protected void onErrorResponse(HttpResponse response) {
        try {
            Log.w(getClassName(), response.getStatusCode() + " - " + response.getStatusMessage() + " : " + response.parseAsString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
