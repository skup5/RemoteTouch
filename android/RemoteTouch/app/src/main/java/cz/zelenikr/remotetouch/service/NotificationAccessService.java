package cz.zelenikr.remotetouch.service;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.util.ArraySet;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.event.EventDTO;
import cz.zelenikr.remotetouch.data.event.EventType;
import cz.zelenikr.remotetouch.data.event.NotificationEventContent;
import cz.zelenikr.remotetouch.helper.ApiHelper;
import cz.zelenikr.remotetouch.helper.SettingsHelper;
import cz.zelenikr.remotetouch.processor.SBNProcessor;
import cz.zelenikr.remotetouch.storage.NotificationDataStore;

/**
 * This service is handling notifications of other applications.
 *
 * @author Roman Zelenik
 */
public class NotificationAccessService extends NotificationListenerService
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = getLocalClassName();
    private static final EventType EVENT_TYPE = EventType.NOTIFICATION;

    private final NotificationDataStore dataStore = new NotificationDataStore(this);
    private final SBNProcessor sbnProcessor = new SBNProcessor();
    private final boolean makeTousts = false;

    private Set<String> appsFilterSet = new ArraySet<>();
    private boolean isConnected = false;

    public static String getLocalClassName() {
        return NotificationAccessService.class.getSimpleName();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        dataStore.open();
        setAppsFilterSet(loadFilterSet());
        registerOnPreferenceChangedListener();

        Log.i(TAG, "Was created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        dataStore.close();
        unregisterOnPreferenceChangedListener();

        Log.i(TAG, "Was destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i(TAG, "Is running");

        // Restart service if is killed
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        NotificationEventContent notificationEventContent = sbnProcessor.process(this, sbn);

        dataStore.add(notificationEventContent);

        // Continue if service should sending notifications
        if (!isEnabled()) return;

        // We care only about apps in filter
        if (!appsFilterSet.contains(sbn.getPackageName())) {
            return;
        }

        // Log to console
        // logNotification(sbn); // Verbose
        Log.i(TAG, "Notification posted (" + sbn.getPackageName() + ")");

        // Send to REST server
        sendEvent(notificationEventContent);

        if (makeTousts)
            Toast.makeText(this, "Notification posted (" + sbn.getPackageName() + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        dataStore.remove(sbnProcessor.process(this, sbn));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "onSharedPreferenceChanged ");
        // Update notifications filter
        if (key.equals(getString(R.string.Key_Notifications_Selected_apps))) {
            setAppsFilterSet(sharedPreferences.getStringSet(key, Collections.emptySet()));
        }
    }

    /**
     * Returns true if the user has enabled alerting to the new notifications.
     *
     * @return true if alerting to the new notification is enabled
     */
    private boolean isEnabled() {
        return SettingsHelper.areNotificationsEnabled(this);
    }

    /**
     * Loads and returns set of selected apps, that user is interested in.
     *
     * @return set of application package names
     */
    private Set<String> loadFilterSet() {
        return SettingsHelper.getNotificationsApps(this);
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

    /**
     * Processes the specific {@link StatusBarNotification} and forwards it
     * to the events sender ({@link MessageSenderService}).
     *
     * @param sbn the given new notification
     */
    private void sendEvent(NotificationEventContent notificationEventContent) {
        Intent intent = new Intent(this, MessageSenderService.class);
        intent.putExtra(MessageSenderService.INTENT_EXTRA_IS_MSG, true);
        intent.putExtra(
            MessageSenderService.INTENT_EXTRA_NAME,
            new EventDTO(EVENT_TYPE, notificationEventContent)
        );

        startService(intent);
    }

    private void setAppsFilterSet(Set<String> appsFilterSet) {
        Log.i(TAG, "setAppsFilterSet: " + Arrays.toString(appsFilterSet.toArray()));
        this.appsFilterSet = appsFilterSet;
    }

    private void registerOnPreferenceChangedListener() {
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).registerOnSharedPreferenceChangeListener(this);
    }

    private void unregisterOnPreferenceChangedListener() {
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

}
