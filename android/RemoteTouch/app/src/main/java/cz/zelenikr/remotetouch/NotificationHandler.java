package cz.zelenikr.remotetouch;

import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.Date;

/**
 * @author Roman Zelenik
 */
public class NotificationHandler extends NotificationListenerService {

  private static final String TAG = NotificationHandler.class.getSimpleName();

  @Override
  public void onCreate() {
    super.onCreate();
    Log.i(TAG, "Handler was created");
  }

  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    super.onNotificationPosted(sbn);
    Notification notification = sbn.getNotification();
    int id = sbn.getId();
    String packageName = sbn.getPackageName();
    String tickerText = notification.tickerText != null ? notification.tickerText.toString() : "null";
    String when = new Date(notification.when).toString();

    Log.i(TAG, "* NOTIFICATION INFO *");
    Log.i(TAG, "PACKAGE: " + packageName);
    Log.i(TAG, "ID: " + id);
    Log.i(TAG, "WHEN: " + when);
    Log.i(TAG, "TICKER:" + tickerText);

    // Require API >= 21
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
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

  }

  @Override
  public void onNotificationRemoved(StatusBarNotification sbn) {
    super.onNotificationRemoved(sbn);
    Log.i(TAG, "Notification (" + sbn.getId() + ") removed");
  }
}
