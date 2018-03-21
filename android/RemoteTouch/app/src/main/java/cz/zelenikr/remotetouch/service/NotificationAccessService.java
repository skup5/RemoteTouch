package cz.zelenikr.remotetouch.service;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.util.ArraySet;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Set;

import cz.zelenikr.remotetouch.helper.AndroidAppHelper;
import cz.zelenikr.remotetouch.data.EventType;
import cz.zelenikr.remotetouch.data.NotificationWrapper;
import cz.zelenikr.remotetouch.data.dto.EventDTO;
import cz.zelenikr.remotetouch.data.dto.NotificationEventContent;
import cz.zelenikr.remotetouch.helper.ApiHelper;
import cz.zelenikr.remotetouch.helper.SettingsHelper;
import cz.zelenikr.remotetouch.storage.NotificationDataStore;

/**
 * This service is handling notifications of other applications.
 *
 * @author Roman Zelenik
 */
public class NotificationAccessService extends NotificationListenerService {

    private static final String TAG = getLocalClassName();
    private static final EventType EVENT_TYPE = EventType.NOTIFICATION;

    private Set<String> appsFilterSet = new ArraySet<>();
    private NotificationDataStore dataStore = new NotificationDataStore(this);
    private final boolean makeTousts = false;
    private boolean isConnected = false;

    public static String getLocalClassName() {
        return NotificationAccessService.class.getSimpleName();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handleStart();

        Log.i(TAG, "Was created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        handleDestroy();

        Log.i(TAG, "Was destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i(TAG, "Is running");

    /*if (!isConnected
            && NotificationHelper.isNotificationListenerEnabled(this)
            && ApiHelper.checkCurrentApiLevel(24)) {
      requestRebind(COMPONENT_NAME);
    }*/

        handleStart();

        // Restart service if is killed
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!isEnabled()) return;

        // We care only about apps in filter
        if (!appsFilterSet.contains(sbn.getPackageName())) {
            return;
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
        if (!isEnabled()) return;

        Log.i(TAG, "Notification (" + sbn.getPackageName() + ") removed");

        if (makeTousts)
            Toast.makeText(this, "Notification removed (" + sbn.getPackageName() + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListenerConnected() {
        Log.i(TAG, "Is connected");
        isConnected = true;
    }

    @Override
    public void onListenerDisconnected() {
        Log.i(TAG, "Is disconnected");
        isConnected = false;
    }

    private boolean isEnabled() {
        return SettingsHelper.areNotificationsEnabled(this);
    }

    private void handleStart() {
        this.appsFilterSet = loadFilterSet();
        this.dataStore.open();
    }

    private void handleDestroy() {
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

            Object appInfoObj = extras.get("android.appInfo");
            if (appInfoObj != null && appInfoObj instanceof ApplicationInfo) {
                ApplicationInfo appInfo = (ApplicationInfo) appInfoObj;
                PackageManager packageManager = getApplicationContext().getPackageManager();
                CharSequence appName = packageManager.getApplicationLabel(appInfo);
                Log.i(TAG, "appInfo.label: " + appName);
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

    private void sendEvent(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        long when = notification.when;
        String app = sbn.getPackageName();
        String ticker = notification.tickerText != null ? notification.tickerText.toString() : "";
        String title = "";
        String text = "";
        String label = AndroidAppHelper.getAppLabelByPackageName(this, app);

        // Require API >= 19
        if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.KITKAT)) {
            Bundle extras = notification.extras;
            title = extras.getString(Notification.EXTRA_TITLE, "");
            text = extras.getString(Notification.EXTRA_TEXT, "");
        }

        Intent intent = new Intent(this, EventService.class);
        intent.putExtra(EventService.INTENT_EXTRA_EVENT, true);
        intent.putExtra(
            EventService.INTENT_EXTRA_NAME,
            new EventDTO(EVENT_TYPE, new NotificationEventContent(app, label, ticker, title, text, when))
        );

        startService(intent);
    }
}
