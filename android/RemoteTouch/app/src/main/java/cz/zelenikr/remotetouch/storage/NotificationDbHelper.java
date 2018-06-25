package cz.zelenikr.remotetouch.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cz.zelenikr.remotetouch.storage.NotificationContract.NotificationEntry;

/**
 * A helper class to manage notification database creation and version management.
 *
 * @author Roman Zelenik
 */
public class NotificationDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + NotificationEntry.TABLE_NAME + " (" +
            NotificationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            NotificationEntry.COLUMN_NAME_NOTIF_ID + " INTEGER NOT NULL," +
            NotificationEntry.COLUMN_NAME_APP + " TEXT NOT NULL," +
            NotificationEntry.COLUMN_NAME_LABEL + " TEXT NOT NULL," +
            NotificationEntry.COLUMN_NAME_TEXT + " TEXT NOT NULL," +
            NotificationEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL," +
            NotificationEntry.COLUMN_NAME_TIMESTAMP + " INTEGER NOT NULL)";

    private static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + NotificationEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Notifications.db";

    public NotificationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only simple counter, so there will not be any upgrade
        return;
    }
}
