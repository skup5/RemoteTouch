package cz.zelenikr.remotetouch.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.message.MessageContent;
import cz.zelenikr.remotetouch.data.message.MessageDTO;

/**
 * Abstract base class to sending JSON requests to the REST server.
 *
 * @author Roman Zelenik
 */
abstract class BaseJsonRestClient implements RestClient {
    protected static final Gson GSON = new Gson();
    private static final String PING_PATH = "/ping";
    private final HttpTransport httpTransport;
    protected String clientToken;
    protected URL baseRestUrl;

    /**
     * @param clientToken The client identification token.
     * @param baseRestUrl The base server url (like https://myserver.com).
     * @param context
     */
    BaseJsonRestClient(String clientToken, URL baseRestUrl, Context context) {
        this.clientToken = clientToken;
        this.baseRestUrl = baseRestUrl;
        try {
            this.httpTransport = new NetHttpTransport.Builder().setSslSocketFactory(initSocketFactory(context)).build();
        } catch (CertificateException | IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String toJson(Object object) {
        return GSON.toJson(object);
    }

    @Override
    public boolean ping() {
        HttpContent httpContent = new EmptyContent();
        return postRequest(PING_PATH, httpContent);
    }

    @Override
    public boolean send(String msg, String path) {
        if (msg == null) msg = "";
        return postRequest(path, makeJSONContent(new MessageDTO(clientToken, msg)));
    }

    @Override
    public boolean send(@NonNull MessageContent content, String path) {
        return postRequest(path, makeJSONContent(new MessageDTO(clientToken, content)));
    }

    @Override
    public boolean sendAll(@NonNull MessageContent[] contents, String path) {
        return postRequest(path, makeJSONContent(new MessageDTO(clientToken, contents)));
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
     * @param subUrl
     * @param httpContent content of POST request
     * @return true if successful response was received
     */
    protected boolean postRequest(String subUrl, HttpContent httpContent) {
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

    private SSLSocketFactory initSocketFactory(Context context) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate ca;

        // Load CAs from an InputStream
        try (InputStream caInput = context.getResources().openRawResource(R.raw.certificate)) {
            ca = cf.generateCertificate(caInput);
            //System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }
}
