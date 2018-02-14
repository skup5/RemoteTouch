package cz.zelenikr.remotetouch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import cz.zelenikr.remotetouch.data.EEventType;
import cz.zelenikr.remotetouch.helper.ConnectionHelper;
import cz.zelenikr.remotetouch.network.SimpleRestClient;

/**
 * @author Roman Zelenik
 */
public class EventReceiver extends BroadcastReceiver {

  private static final String TAG = EventReceiver.class.getSimpleName();
  private final SimpleRestClient restClient;

  public EventReceiver() {
    super();
    try {
      this.restClient = new SimpleRestClient(loadClientToken(), new URL(loadRestUrl()));
    } catch (MalformedURLException e) {
      Log.e(TAG, e.getLocalizedMessage());
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    final String packageName = intent.getStringExtra("packageName");
    final String eventName = intent.getStringExtra("event");
    EEventType eventType = EEventType.valueOf(eventName);
    Log.i(TAG, "received event " + eventName + " from " + packageName);
    if (ConnectionHelper.isConnected(context)) {
      try {
        boolean result = new AsyncTask<Void, Void, Boolean>() {
          @Override
          protected Boolean doInBackground(Void... voids) {
            return restClient.send(packageName, eventType);
          }
        }.execute().get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  private String loadClientToken() {
    // TODO: get from authentication
    return "1";
  }

  private String loadRestUrl() {
    // TODO: load from Preferences
    return "http://10.0.0.46:4000";
  }
}
