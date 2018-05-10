package cz.zelenikr.remotetouch.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import cz.zelenikr.remotetouch.data.wrapper.NotificationWrapper;
import cz.zelenikr.remotetouch.storage.NotificationContract.NotificationEntry;

/**
 * @author Roman Zelenik
 */
public class NotificationDataStore implements DataStore<NotificationWrapper> {

  private SQLiteDatabase database;
  private final NotificationDbHelper dbHelper;
  private final String[] allColumns = {
      NotificationEntry._ID,
      NotificationEntry.COLUMN_NAME_APP,
      NotificationEntry.COLUMN_NAME_TIMESTAMP
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

  public boolean export(File destFile){
    File data = Environment.getDataDirectory();
    FileChannel source = null;
    FileChannel destination = null;
    String currentDBPath = "/data/" + "cz.zelenikr.remotetouch" + "/databases/" + dbHelper.getDatabaseName();
    File currentDB = new File(data, currentDBPath);
    try {
      source = new FileInputStream(currentDB).getChannel();
      destination = new FileOutputStream(destFile).getChannel();
      destination.transferFrom(source, 0, source.size());
      source.close();
      destination.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public void add(NotificationWrapper... items) {
    ContentValues contentValues;
    for (NotificationWrapper wrapper : items) {
      contentValues = new ContentValues();
      contentValues.put(NotificationEntry.COLUMN_NAME_APP, wrapper.getApplication());
      contentValues.put(NotificationEntry.COLUMN_NAME_TIMESTAMP, wrapper.getTimestamp());
      database.insert(NotificationEntry.TABLE_NAME, null, contentValues);
    }
  }

  @Override
  public void update(NotificationWrapper... items) {
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public void remove(NotificationWrapper... items) {
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public List<NotificationWrapper> get(long... id) {
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public List<NotificationWrapper> getAll() {
    List<NotificationWrapper> notificationWrappers = new ArrayList<>();
    Cursor cursor = database.query(
        NotificationEntry.TABLE_NAME,
        allColumns,
        null, null, null, null, null
    );
    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      notificationWrappers.add(cursorToNotificationWrapper(cursor));
      cursor.moveToNext();
    }
    cursor.close();
    return notificationWrappers;
  }

  private NotificationWrapper cursorToNotificationWrapper(Cursor cursor) {
    NotificationWrapper notificationWrapper = new NotificationWrapper(cursor.getString(1), cursor.getLong(2));
    notificationWrapper.setId(cursor.getLong(0));
    return notificationWrapper;
  }
}
