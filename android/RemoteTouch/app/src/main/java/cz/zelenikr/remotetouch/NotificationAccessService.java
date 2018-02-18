package cz.zelenikr.remotetouch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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

import cz.zelenikr.remotetouch.data.EEventType;
import cz.zelenikr.remotetouch.data.NotificationWrapper;
import cz.zelenikr.remotetouch.helper.ApiHelper;
import cz.zelenikr.remotetouch.receiver.EventReceiver;
import cz.zelenikr.remotetouch.service.EventService;
import cz.zelenikr.remotetouch.storage.NotificationDataStore;

import static cz.zelenikr.remotetouch.helper.NotificationHelper.APP_ICON_ID;

/**
 * This service is handling notifications of other applications.
 *
 * @author Roman Zelenik
 */
public class NotificationAccessService extends NotificationListenerService {

  private static final String TAG = getLocalClassName();
  private static final int PERSISTENT_NOTIFICATION_ID = 1;
  private static final EEventType EVENT_TYPE = EEventType.NOTIFICATION;

  private Set<String> appsFilterSet = new ArraySet<>();
  private NotificationDataStore dataStore = new NotificationDataStore(this);
  private EventReceiver eventReceiver =new EventReceiver();
  private final boolean makeTousts = false;

  public static String getLocalClassName() {
    return NotificationAccessService.class.getSimpleName();
  }

  @Override
  public void onCreate() {
    super.onCreate();

    onStarted();

//    LocalBroadcastManager.getInstance(this).registerReceiver(eventReceiver, new IntentFilter(getString(R.string.Intent_Action_NewEvent)));

    Log.i(TAG, "Handler was created");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    onDestroyed();

//    LocalBroadcastManager.getInstance(this).unregisterReceiver(eventReceiver);

    Log.i(TAG, "Handler was destroyed");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);

    Log.i(TAG, "Handler is running");

    onStarted();

    // Restart service if is killed
    return START_REDELIVER_INTENT;
  }

  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    // Current API level has to be greater then min API level (18)
    if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.JELLY_BEAN_MR2 + 1)) {
      super.onNotificationPosted(sbn);
    }

    // We care only about apps in filter
    if (!appsFilterSet.contains(sbn.getPackageName())) {
      // return;
    }

    // Log to console
    logNotification(sbn);

    // Send to REST server
    sendEvent(sbn);

    // Increment notification counter for statistics
    incrementNotificationCounter(sbn);

    if (makeTousts)
      Toast.makeText(this, "Notification posted (" + sbn.getPackageName() + ")", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onNotificationRemoved(StatusBarNotification sbn) {
    // Current API level has to be greater then min API level (18)
    if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.JELLY_BEAN_MR2 + 1)) {
      super.onNotificationRemoved(sbn);
    }

    Log.i(TAG, "Notification (" + sbn.getPackageName() + ") removed");

    if (makeTousts)
      Toast.makeText(this, "Notification removed (" + sbn.getPackageName() + ")", Toast.LENGTH_SHORT).show();
  }

  private void onStarted() {
    // Show persistent notification
    //showPersistentNotification();

    this.appsFilterSet = loadFilterSet();
    this.dataStore.open();
  }

  private void onDestroyed() {
    //removePersistentNotification();
    this.dataStore.close();
  }

  private Set<String> loadFilterSet() {
    // TODO: 26.12.2017 Replace by reading from file

    ArraySet<String> filterSet = new ArraySet<>(1);
    filterSet.add(getPackageName());
    return filterSet;
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
    if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.LOLLIPOP)) {
      String category = notification.category != null ? notification.category : "null";
      String visibility = notification.visibility == Notification.VISIBILITY_PRIVATE ? "PRIVATE" : notification.visibility == Notification.VISIBILITY_PUBLIC ? "PUBLIC" : "SECRET";
      Log.i(TAG, "CATEGORY: " + category);
      Log.i(TAG, "VISIBILITY: " + visibility);
    }

    // Require API >= 19
    if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.KITKAT)) {
      Bundle extras = sbn.getNotification().extras;

      Log.i(TAG, "EXTRAS:");
      for (String extraKey : extras.keySet()) {
        Log.i(TAG, extraKey + ": " + extras.get(extraKey));
      }
    }
  }

  private void incrementNotificationCounter(StatusBarNotification sbn) {
//    SharedPreferences sharedPreferences = getSharedPreferences(getLocalClassName(), MODE_PRIVATE);
//    int count = sharedPreferences.getInt(sbn.getPackageName(), 0);
//    count++;
//    sharedPreferences.edit().putInt(sbn.getPackageName(), count).apply();

    NotificationWrapper wrapper = new NotificationWrapper(sbn.getPackageName(), sbn.getPostTime());
    dataStore.add(wrapper);
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
    builder.setContentTitle(getString(R.string.Application_Name))
            .setContentText(getString(R.string.NotificationAccessService_PersistentNotification_Text))
            .setSmallIcon(APP_ICON_ID)
            .setShowWhen(true)
            .setAutoCancel(false)
            // Set persistent
            .setOngoing(true)
            .setContentIntent(notifyPendingIntent);

    // Get manager and show notification
    ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(PERSISTENT_NOTIFICATION_ID, builder.build());
  }

  private void removePersistentNotification() {
    // Get manager and remove notification
    ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(PERSISTENT_NOTIFICATION_ID);
  }

  private void sendEvent(StatusBarNotification sbn) {
//    Intent intent = new Intent(getString(R.string.Intent_Action_NewEvent));
//    Intent intent = new Intent(this, EventReceiver.class);
    Intent intent = new Intent(this, EventService.class);
    intent.putExtra("packageName", sbn.getPackageName());
    intent.putExtra("event", EVENT_TYPE.name());

    //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    startService(intent);

  }
}
