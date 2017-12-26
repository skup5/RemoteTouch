package cz.zelenikr.remotetouch;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

/**
 * @author Roman Zelenik
 */
public class NotificationHelper {

  private static final int APP_ICON_ID = android.R.drawable.sym_def_app_icon;

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
            .setAutoCancel(true);
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
            .setAutoCancel(false);
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
//            .setCategory(Notification.CATEGORY_SERVICE)             // Require api level 21
            .setContentInfo("Content info")
            .setDefaults(Notification.DEFAULT_ALL)
            .setSubText("Sub text")
            .setTicker("Ticker")
//            .setVisibility(Notification.VISIBILITY_SECRET)              // Require api level 21
            .setSmallIcon(APP_ICON_ID)
            .setAutoCancel(true);
    getManager(context).notify(id, builder.build());
  }

  private static NotificationManager getManager(Context context) {
    return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  private NotificationHelper() {
  }
}
