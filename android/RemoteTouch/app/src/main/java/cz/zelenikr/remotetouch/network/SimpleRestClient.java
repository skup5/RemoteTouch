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
import java.net.URL;

import cz.zelenikr.remotetouch.data.EEventType;
import cz.zelenikr.remotetouch.data.MessageDTO;

/**
 * Provides unsecured JSON message sending to the REST server.
 *
 * @author Roman Zelenik
 */
public class SimpleRestClient implements RestClient { // TODO: make a Singleton

  private static final Gson GSON = new Gson();
  private static final String CLASS_NAME = SimpleRestClient.class.getSimpleName();

  private final String clientToken;
  private final HttpTransport httpTransport = new NetHttpTransport();
  private final URL baseRestUrl;

  public SimpleRestClient(String clientToken, URL baseRestUrl) {
    this.clientToken = clientToken;
    this.baseRestUrl = baseRestUrl;
  }

  @Override
  public boolean send(String msg, EEventType event) {
    return postRequest(event.toString(), makeJSONContent(new MessageDTO(clientToken, event, msg)));
  }

  private HttpContent makeJSONContent(Object pojo) {
    String json = toJson(pojo);
    HttpContent httpContent = new ByteArrayContent("application/json", json.getBytes());
    return httpContent;

  }

  private boolean postRequest(String subUrl, HttpContent httpContent) {
    boolean success = false;
    try {
      Log.i(CLASS_NAME, "post request");
      GenericUrl restUrl = new GenericUrl(baseRestUrl);
      if (!subUrl.startsWith("/")) {
        subUrl = "/" + subUrl;
      }
      restUrl.appendRawPath(subUrl);
      Log.i(CLASS_NAME, "send to " + restUrl);
      HttpResponse httpResponse = httpTransport.createRequestFactory()
              .buildPostRequest(restUrl, httpContent)
              .setThrowExceptionOnExecuteError(false)
              .execute();
      try {
        if (httpResponse.isSuccessStatusCode()) {
          success = true;
        } else {
          Log.w(CLASS_NAME, httpResponse.getStatusCode() + " - " + httpResponse.getStatusMessage());
        }
      } finally {
        httpResponse.disconnect();
      }
    } catch (IOException e) {
//                e.printStackTrace();
      Log.w(CLASS_NAME, e.toString());
    }
    return success;
  }

  private String toJson(Object object) {
    return GSON.toJson(object);
  }
}
