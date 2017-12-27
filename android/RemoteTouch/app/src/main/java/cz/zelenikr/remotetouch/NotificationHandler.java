package cz.zelenikr.remotetouch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Set;

import cz.zelenikr.remotetouch.helper.NotificationHelper;

import static cz.zelenikr.remotetouch.helper.NotificationHelper.APP_ICON_ID;

/**
 * This service is handling notifications of other applications.
 *
 * @author Roman Zelenik
 */
public class NotificationHandler extends NotificationListenerService {

  private static final String TAG = NotificationHandler.class.getSimpleName();
  private static final int PERSISTENT_NOTIFICATION_ID = 1;

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
//    return START_REDELIVER_INTENT;
    return START_STICKY;
  }

  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    super.onNotificationPosted(sbn);

    // We don't care about apps in filter
    if (appsFilterSet.contains(sbn.getPackageName())) {
      return;
    }

    // Log to console
    logNotification(sbn);

    // Increment notification counter for statistics
    incrementNotificationCounter(sbn);

    Toast.makeText(this, "Notification posted (" + sbn.getPackageName() + ")", Toast.LENGTH_LONG).show();
  }

  @Override
  public void onNotificationRemoved(StatusBarNotification sbn) {
    super.onNotificationRemoved(sbn);
    Log.i(TAG, "Notification (" + sbn.getPackageName() + ") removed");
    Toast.makeText(this, "Notification removed (" + sbn.getPackageName() + ")", Toast.LENGTH_LONG).show();
  }

  private void onStarted() {
    // Show persistent notification
    showPersistentNotification();

    this.appsFilterSet = loadFilterSet();
  }

  private Set<String> loadFilterSet() {
    // TODO: 26.12.2017 Replace by reading from file

    ArraySet<String> filterSet = new ArraySet<>(1);
    filterSet.add(getPackageName());
    return filterSet;
  }

  @NonNull
  private String getResourceString(int id) {
    return getResources().getString(id);
  }

  private String getLocalClassName() {
    return getClass().getSimpleName();
  }

  private void logNotification(StatusBarNotification sbn) {
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
  }

  private void incrementNotificationCounter(StatusBarNotification sbn) {
    SharedPreferences sharedPreferences = getSharedPreferences(getLocalClassName(), MODE_PRIVATE);
    int count = sharedPreferences.getInt(sbn.getPackageName(), 0);
    count++;
    sharedPreferences.edit().putInt(sbn.getPackageName(), count).apply();
  }

  private void showPersistentNotification() {
    // Creates an Intent for the Activity
    Intent notifyIntent = new Intent(this, MainActivity.class);

    // Sets the Activity to start in a new task
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    // Creates the PendingIntent
    PendingIntent notifyPendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );

    Notification.Builder builder = new Notification.Builder(getApplicationContext());

    // Prepare notification
    builder.setContentTitle(getResourceString(R.string.Application_Name))
        .setContentText(getResourceString(R.string.NotificationHandler_PersistentNotification_Text))
        .setSmallIcon(APP_ICON_ID)
        .setShowWhen(true)
        .setAutoCancel(false)
        // Set persistent
        .setOngoing(true)
        .setContentIntent(notifyPendingIntent);

    // Get manager and show notification
    ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(PERSISTENT_NOTIFICATION_ID, builder.build());
  }
}
