package cz.zelenikr.remotetouch.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import cz.zelenikr.remotetouch.MainActivity;
import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.EEventType;
import cz.zelenikr.remotetouch.helper.ConnectionHelper;
import cz.zelenikr.remotetouch.network.SimpleRestClient;

import static cz.zelenikr.remotetouch.helper.NotificationHelper.APP_ICON_ID;

/**
 * @author Roman Zelenik
 */
public class EventService extends Service {

  private static final int ONGOING_NOTIFICATION_ID = 1;
  private static final String TAG = EventService.class.getSimpleName();
  private SimpleRestClient restClient;


  @Override
  public void onCreate() {
    super.onCreate();
    try {
      this.restClient = new SimpleRestClient(loadClientToken(), new URL(loadRestUrl()));
    } catch (MalformedURLException e) {
      Log.e(TAG, e.getLocalizedMessage());
      throw new RuntimeException(e);
    }
    showNotification();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && intent.getStringExtra("event") != null) {
      handleEvent(intent);
    }
    return START_NOT_STICKY;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  public void handleEvent(Intent intent) {
    final String packageName = intent.getStringExtra("packageName");
    final String eventName = intent.getStringExtra("event");
    EEventType eventType = EEventType.valueOf(eventName);
    Log.i(TAG, "received event " + eventName + " from " + packageName);
    if (ConnectionHelper.isConnected(this)) {
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
//    return "http://10.0.0.46:4000";
    return "http://remote-touch.azurewebsites.net";
  }

  private void showNotification() {
    Intent notificationIntent = new Intent(this, MainActivity.class);
    PendingIntent pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Notification notification =
            new Notification.Builder(this)
                    .setContentTitle(getString(R.string.Application_Name) + " (EventService)")
                    .setContentText(getString(R.string.EventService_PersistentNotification_Text))
                    .setSmallIcon(APP_ICON_ID)
                    .setShowWhen(true)
                    // .setAutoCancel(false)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .build();

    startForeground(ONGOING_NOTIFICATION_ID, notification);
  }
}
