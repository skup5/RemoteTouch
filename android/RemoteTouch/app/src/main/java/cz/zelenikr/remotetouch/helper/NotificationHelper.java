package cz.zelenikr.remotetouch.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Set;

import cz.zelenikr.remotetouch.R;

/**
 * @author Roman Zelenik
 */
public class NotificationHelper {

    public static final int
        APP_ICON_ID = android.R.drawable.sym_def_app_icon,
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
    public static void createNotificationChannel(Context context, String id, String name, @Nullable String description) {
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
     * @param title
     * @param text
     * @param id
     */
    public static void notify(Context context, String title, String text, int id) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(APP_ICON_ID)
            .setShowWhen(true)
            .setAutoCancel(true);
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
     * Shows immediately persistent notification on status bar
     *
     * @param context
     * @param title
     * @param text
     * @param id
     */
    public static void persistent(Context context, String title, String text, int id) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(APP_ICON_ID)
            .setOngoing(true)
            .setShowWhen(true)
            .setAutoCancel(false);
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
     * @param id
     */
    public static void test(Context context, int id) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Content title")
            .setContentText("Content text")
            .setDefaults(Notification.DEFAULT_ALL)
            .setSubText("Sub text")
            .setTicker("Ticker")
//            .setCategory(Notification.CATEGORY_SERVICE)             // Require api level 21
//            .setVisibility(Notification.VISIBILITY_SECRET)          // Require api level 21
            .setSmallIcon(APP_ICON_ID)
            .setShowWhen(true)
            .setAutoCancel(true);
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
     * @param context
     * @return
     */
    public static boolean isNotificationListenerEnabled(Context context) {
        Set<String> enabledSet = NotificationManagerCompat.getEnabledListenerPackages(context);
    /*System.out.println("EnabledListenerPackages:");
    for (String pckg:enabledSet
         ) {
      System.out.println(pckg);
    }
    System.out.println("========================");*/
        return enabledSet.contains(context.getPackageName());
    }

    private static NotificationManager getManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private NotificationHelper() {
    }
}
