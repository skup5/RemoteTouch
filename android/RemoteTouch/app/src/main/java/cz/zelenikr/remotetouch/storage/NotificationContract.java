package cz.zelenikr.remotetouch.storage;

import android.provider.BaseColumns;

/**
 * @author Roman Zelenik
 */
public final class NotificationContract {

  /**
   * Defines the Notification table contents.
   */
  public static class NotificationEntry implements BaseColumns {
    public static final String
            TABLE_NAME = "notification",
            COLUMN_NAME_APP = "app",
            COLUMN_NAME_TIMESTAMP = "timestamp";
  }

  private NotificationContract() {
  }
}
