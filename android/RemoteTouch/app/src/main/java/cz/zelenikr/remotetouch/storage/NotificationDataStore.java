package cz.zelenikr.remotetouch.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cz.zelenikr.remotetouch.data.event.NotificationEventContent;
import cz.zelenikr.remotetouch.storage.NotificationContract.NotificationEntry;

/**
 * This {@link DataStore} implementation allows to store and read {@link NotificationEventContent}
 * objects from SQLite database.
 *
 * @author Roman Zelenik
 */
public class NotificationDataStore implements DataStore<NotificationEventContent> {

    private SQLiteDatabase database;
    private final NotificationDbHelper dbHelper;
    private final String[] allColumns = {
        NotificationEntry._ID,
        NotificationEntry.COLUMN_NAME_NOTIF_ID,
        NotificationEntry.COLUMN_NAME_APP,
        NotificationEntry.COLUMN_NAME_LABEL,
        NotificationEntry.COLUMN_NAME_TEXT,
        NotificationEntry.COLUMN_NAME_TITLE,
        NotificationEntry.COLUMN_NAME_TIMESTAMP,
    };

    public NotificationDataStore(Context context) {
        dbHelper = new NotificationDbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    @Override
    public void add(NotificationEventContent... items) {
        ContentValues contentValues;
        for (NotificationEventContent nec : items) {
            contentValues = new ContentValues();

            contentValues.put(NotificationEntry.COLUMN_NAME_NOTIF_ID, nec.getId());
            contentValues.put(NotificationEntry.COLUMN_NAME_APP, nec.getApp());
            contentValues.put(NotificationEntry.COLUMN_NAME_LABEL, nec.getLabel());
            contentValues.put(NotificationEntry.COLUMN_NAME_TEXT, nec.getText());
            contentValues.put(NotificationEntry.COLUMN_NAME_TITLE, nec.getTitle());
            contentValues.put(NotificationEntry.COLUMN_NAME_TIMESTAMP, nec.getWhen());

            database.insert(NotificationEntry.TABLE_NAME, null, contentValues);
        }
    }

    @Override
    public void update(NotificationEventContent... items) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void remove(NotificationEventContent... items) {
        for (NotificationEventContent nec : items) {
            String whereClause = NotificationEntry.COLUMN_NAME_NOTIF_ID + "=? and " + NotificationEntry.COLUMN_NAME_APP + "=?";
            String[] whereArgs = new String[]{String.valueOf(nec.getId()), nec.getApp()};
            database.delete(NotificationEntry.TABLE_NAME, whereClause, whereArgs);
        }
    }

    @Override
    public List<NotificationEventContent> get(long... id) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public List<NotificationEventContent> getAll() {
        List<NotificationEventContent> notifications = new ArrayList<>();
        Cursor cursor = database.query(
            NotificationEntry.TABLE_NAME,
            allColumns,
            null, null, null, null, null
        );
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            notifications.add(cursorToNotification(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return notifications;
    }

    private NotificationEventContent cursorToNotification(Cursor cursor) {
        return new NotificationEventContent(
            cursor.getInt(cursor.getColumnIndex(NotificationEntry.COLUMN_NAME_NOTIF_ID)),
            cursor.getString(cursor.getColumnIndex(NotificationEntry.COLUMN_NAME_APP)),
            cursor.getString(cursor.getColumnIndex(NotificationEntry.COLUMN_NAME_LABEL)),
            cursor.getString(cursor.getColumnIndex(NotificationEntry.COLUMN_NAME_TITLE)),
            cursor.getString(cursor.getColumnIndex(NotificationEntry.COLUMN_NAME_TEXT)),
            cursor.getLong(cursor.getColumnIndex(NotificationEntry.COLUMN_NAME_TIMESTAMP)));
    }
}
