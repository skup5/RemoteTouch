package cz.zelenikr.remotetouch;

import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.util.ArraySet;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Set;

/**
 * This service is handling notifications of other applications.
 *
 * @author Roman Zelenik
 */
public class NotificationHandler extends NotificationListenerService {

  private static final String TAG = NotificationHandler.class.getSimpleName();
  
  private Set<String> appsFilterSet = new ArraySet<>();

  @Override
  public void onCreate() {
    super.onCreate();
    Log.i(TAG, "Handler was created");
    
    onStarted();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "Handler is running");

    onStarted();
    
    // Restart service if is killed
    return START_REDELIVER_INTENT;
//    return START_STICKY;
  }

  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    super.onNotificationPosted(sbn);
    Notification notification = sbn.getNotification();
    int id = sbn.getId();
    String packageName = sbn.getPackageName();
    // We don't care about apps in filter
    if(appsFilterSet.contains(packageName)){
      return;
    }
    String tickerText = notification.tickerText != null ? notification.tickerText.toString() : "null";
    String when = new Date(notification.when).toString();

    Log.i(TAG, "* NOTIFICATION INFO *");
    Log.i(TAG, "PACKAGE: " + packageName);
    Log.i(TAG, "ID: " + id);
    Log.i(TAG, "WHEN: " + when);
    Log.i(TAG, "TICKER:" + tickerText);

    // Require API >= 21
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      String category = notification.category != null ? notification.category : "null";
      String visibility = notification.visibility == Notification.VISIBILITY_PRIVATE ? "PRIVATE" : notification.visibility == Notification.VISIBILITY_PUBLIC ? "PUBLIC" : "SECRET";
      Log.i(TAG, "CATEGORY: " + category);
      Log.i(TAG, "VISIBILITY: " + visibility);
    }

    // Require API >= 19
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
      Bundle extras = sbn.getNotification().extras;

      Log.i(TAG, "EXTRAS:");
      for (String extraKey : extras.keySet()) {
        Log.i(TAG, extraKey + ": " + extras.get(extraKey));
      }
    }

    Toast.makeText(this, "Notification posted ("+packageName+")", Toast.LENGTH_LONG).show();
  }

  @Override
  public void onNotificationRemoved(StatusBarNotification sbn) {
    super.onNotificationRemoved(sbn);
    Log.i(TAG, "Notification (" + sbn.getPackageName() + ") removed");
    Toast.makeText(this, "Notification removed ("+sbn.getPackageName()+")", Toast.LENGTH_LONG).show();
  }

  private void onStarted(){
    // Show persistent notification
    NotificationHelper.persistent(
            getApplicationContext(),
            getResourceString(R.string.Application_Name),
            getResourceString(R.string.NotificationHandler_PersistentNotification_Text),
            1);
  
    this.appsFilterSet = loadFilterSet();
  }
  
  private Set<String> loadFilterSet(){
    // TODO: 26.12.2017 Replace by reading from file

    ArraySet<String> filterSet = new ArraySet<>(1);
    filterSet.add(getPackageName());
    return filterSet;
  }
  
  private String getResourceString(int id) {
    return getResources().getString(id);
  }
}
