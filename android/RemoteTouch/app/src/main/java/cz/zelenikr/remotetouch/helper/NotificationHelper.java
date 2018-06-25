package cz.zelenikr.remotetouch.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.event.NotificationEventContent;
import cz.zelenikr.remotetouch.storage.NotificationDataStore;

/**
 * This class simplifies using {@link Notification}.
 *
 * @author Roman Zelenik
 */
public final class NotificationHelper {

    public static final int
        APP_ICON_ID = R.drawable.ic_stat_rt,
        ONGOING_NOTIFICATION_CHANNEL_ID = R.string.Application_NotificationChannel_Id,
        ONGOING_NOTIFICATION_CHANNEL_NAME = R.string.Application_NotificationChannel_Name,
        ONGOING_NOTIFICATION_CHANNEL_DESCRIPTION = R.string.Application_NotificationChannel_Description;

    /**
     * Creates a notification channel that notifications can be posted to.
     * This can also be used to restore a deleted channel and to update an existing channel's name,
     * description, and/or importance.
     *
     * @param context
     * @param id          The id of the channel. Must be unique per package. The value may be truncated if it is too long.
     * @param name        The user visible name of the channel. You can rename this channel
     *                    when the system locale changes by listening for the Intent.ACTION_LOCALE_CHANGED broadcast.
     *                    The recommended maximum length is 40 characters; the value may be truncated if it is too long.
     * @param description The user visible description of this channel. The recommended maximum length is 300 characters;
     *                    the value may be truncated if it is too long.
     */
    @RequiresApi(26)
    public static void createNotificationChannel(@NonNull Context context, @NonNull String id, @NonNull String name, @Nullable String description) {
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        channel.enableLights(false);
        channel.enableVibration(false);
        getManager(context).createNotificationChannel(channel);
    }

    /**
     * Shows immediately notification on status bar
     *
     * @param context
     * @param title   a content title
     * @param text    a content text
     * @param id      an identifier for this notification unique within your application
     */
    public static void notify(@NonNull Context context, @NonNull String title, @NonNull String text, int id) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(APP_ICON_ID)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0));
        if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.LOLLIPOP)) {
            builder.setColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, context.getTheme()));
        }
        if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.O)) {
            NotificationHelper.createNotificationChannel(
                context,
                context.getString(ONGOING_NOTIFICATION_CHANNEL_ID),
                context.getString(ONGOING_NOTIFICATION_CHANNEL_NAME),
                context.getString(ONGOING_NOTIFICATION_CHANNEL_DESCRIPTION)
            );
            builder.setChannelId(context.getString(ONGOING_NOTIFICATION_CHANNEL_ID));
        }
        getManager(context).notify(id, builder.build());
    }

    /**
     * Shows immediately test notification with mostly texts
     *
     * @param context
     * @param id      an identifier for this notification unique within your application
     */
    public static void test(@NonNull Context context, int id) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(context.getString(R.string.Notifications_Test_Title))
            .setContentText(context.getString(R.string.Notifications_Test_Text))
            .setDefaults(Notification.DEFAULT_ALL)
            .setTicker(context.getString(R.string.Notifications_Test_Title))
            .setSmallIcon(APP_ICON_ID)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0));
        if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.LOLLIPOP)) {
            builder.setColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, context.getTheme()));
        }
        if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.O)) {
            NotificationHelper.createNotificationChannel(
                context,
                context.getString(ONGOING_NOTIFICATION_CHANNEL_ID),
                context.getString(ONGOING_NOTIFICATION_CHANNEL_NAME),
                context.getString(ONGOING_NOTIFICATION_CHANNEL_DESCRIPTION)
            );
            builder.setChannelId(context.getString(ONGOING_NOTIFICATION_CHANNEL_ID));
        }
        getManager(context).notify(id, builder.build());
    }

    /**
     * If user added instance of {@link android.service.notification.NotificationListenerService} class
     * of this application in enabled applications this method returns true.
     *
     * @param context
     * @return true service is enabled
     */
    public static boolean isNotificationListenerEnabled(@NonNull Context context) {
        Set<String> enabledSet = NotificationManagerCompat.getEnabledListenerPackages(context);
    /*System.out.println("EnabledListenerPackages:");
    for (String pckg:enabledSet
         ) {
      System.out.println(pckg);
    }
    System.out.println("========================");*/
        return enabledSet.contains(context.getPackageName());
    }

    /**
     * Returns list of new (active) notifications that the user is interested in.
     *
     * @param context
     * @return
     */
    public static List<NotificationEventContent> getAllNewNotifications(@NonNull Context context) {
        List<NotificationEventContent> notificationList = new ArrayList<>();

        // Get notifications from status bar
        NotificationDataStore dataStore = new NotificationDataStore(context);
        dataStore.open();
        List<NotificationEventContent> activeNotifications = dataStore.getAll();

        // Get app list that the user is interested in
        Set<String> filter = SettingsHelper.getNotificationsApps(context);

        for (NotificationEventContent notification : activeNotifications) {
            // Filter notifications
            if (filter.contains(notification.getApp())) {
                notificationList.add(notification);
            }
        }

        return notificationList;
    }

    private static NotificationManager getManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private NotificationHelper() {
    }
}
