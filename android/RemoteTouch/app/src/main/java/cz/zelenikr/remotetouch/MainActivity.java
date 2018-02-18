package cz.zelenikr.remotetouch;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cz.zelenikr.remotetouch.data.NotificationWrapper;
import cz.zelenikr.remotetouch.helper.NotificationHelper;
import cz.zelenikr.remotetouch.helper.PermissionHelper;
import cz.zelenikr.remotetouch.service.EventService;
import cz.zelenikr.remotetouch.storage.NotificationDataStore;

import static cz.zelenikr.remotetouch.helper.PermissionHelper.MY_PERMISSIONS_REQUEST_CALL_LOG;
import static cz.zelenikr.remotetouch.helper.PermissionHelper.MY_PERMISSIONS_REQUEST_READ_SMS;

/**
 * @author Roman Zelenik
 */
public class MainActivity extends AppCompatActivity {

  private static final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
  private static final Uri CALLS = Uri.parse("content://call_log/calls");

  private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

  private final NotificationDataStore notificationDataStore = new NotificationDataStore(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(getLocalClassName(), "Add notification");
        NotificationHelper.test(getApplicationContext(), (int) System.currentTimeMillis());
      }
    });

    notificationDataStore.open();
  }

  @Override
  protected void onDestroy() {
    notificationDataStore.close();
    super.onDestroy();
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    startService(new Intent(this, EventService.class));

    if (enableNotificationHandler()) {
      startService(new Intent(this, NotificationAccessService.class));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         String permissions[], int[] grantResults) {
    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST_CALL_LOG: {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

          // permission was granted, yay! Do the
          // contacts-related task you need to do.

        } else {

          // permission denied, boo! Disable the
          // functionality that depends on this permission.
        }
        return;
      }
      case MY_PERMISSIONS_REQUEST_READ_SMS: {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

          onSmsBtClick(findViewById(R.id.smsBt));
        } else {

          // permission denied, boo! Disable the
          // functionality that depends on this permission.
        }
        return;
      }

      // other 'case' lines to check for other
      // permissions this app might request
    }
  }

  public void onCallsBtClick(View view) {
    if (!PermissionHelper.checkCallLogPermission(this)) return;
    //checkCallLogPermission();

    fillListView(getCallDetails());
  }

  public void onSmsBtClick(View view) {
    if (!PermissionHelper.checkSmsPermission(this)) return;

    List<String> messageList = new ArrayList<>();
    String[] cols = new String[]{"date", "person", "address", "read", "body"};

    // returns last 3 received sms ordered by date (and unread)
    Cursor cursor = getContentResolver().query(SMS_INBOX, cols,
            "read=0", null, "read, date desc limit 3");

    if (cursor == null) {
      messageList.add(getResourceString(R.string.empty));
    } else {
      String message = "";

      while (cursor.moveToNext()) {
        message = cols[0].toUpperCase() + ": " + new Date(Long.valueOf(cursor.getString(0))) + "\n";

        for (int i = 1; i < cols.length; i++)
          message += cols[i].toUpperCase() + ": " + cursor.getString(i) + "\n";

        messageList.add(message);
      }
    }

    fillListView(messageList);
  }

  public void onNotificationsBtClick(View view) {
    List<String> notificationList;

    // Load from shared preferences
//    notificationList = loadPreferences(NotificationAccessService.getLocalClassName());

    // Load from sqlite db
    notificationList = loadStoredNotifications();

    if (notificationList.isEmpty())
      notificationList.add(getResourceString(R.string.empty));

    fillListView(notificationList);
  }

  private List<String> getCallDetails() {
    List<String> calls = new ArrayList<>();
    String callDetail;

    Cursor managedCursor = getContentResolver().query(CALLS, null, null, null, "date desc");
    if (managedCursor == null) {
      calls.add(getResourceString(R.string.empty));
      return calls;
    }
    int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
    int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
    int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
    int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
    int newCall = managedCursor.getColumnIndex(CallLog.Calls.NEW);

    while (managedCursor.moveToNext()) {
      String phNumber = managedCursor.getString(number);
      String callType = managedCursor.getString(type);
      String callDate = managedCursor.getString(date);
      Date callDayTime = new Date(Long.valueOf(callDate));
      String callDuration = managedCursor.getString(duration);
      int callIsNew = managedCursor.getInt(newCall);
      String dir = null;
      int dircode = Integer.parseInt(callType);
      switch (dircode) {
        case CallLog.Calls.OUTGOING_TYPE:
          dir = "OUTGOING";
          break;

        case CallLog.Calls.INCOMING_TYPE:
          dir = "INCOMING";
          break;

        case CallLog.Calls.MISSED_TYPE:
          dir = "MISSED";
          break;
      }
      callDetail = "\nIs new:---" + (callIsNew == 1 ? "yes" : "no")
              + "\nPhone Number:--- " + phNumber + " \nCall Type:--- "
              + dir + " \nCall Date:--- " + callDayTime
              + " \nCall duration in sec :--- " + callDuration;
      calls.add(callDetail);
    }
    managedCursor.close();
    return calls;

  }

  private List<String> loadPreferences(String className) {
    List<String> preferences = new ArrayList<>();
    Map<String, ?> prefs = getSharedPreferences(className, MODE_PRIVATE).getAll();
    for (Map.Entry entry : prefs.entrySet()) {
      preferences.add(entry.getKey() + " : " + entry.getValue());
    }
    return preferences;
  }

  private List<String> loadStoredNotifications() {
    List<String> notificationList = new ArrayList<>();
    List<NotificationWrapper> notifications = notificationDataStore.getAll();
    // Sort descending by ID
    Collections.sort(notifications, (o1, o2) -> {
      return (int) (o2.getId() - o1.getId());
    });
    // Map to strings
    for (NotificationWrapper wrapper : notifications) {
      notificationList.add(new Date(wrapper.getTimestamp()).toString() + " " + wrapper.getApplication());
    }

    return notificationList;
  }

  private void fillListView(List<String> items) {
    /*
    // Optimized list for large data set
    RecyclerView list = findViewById(R.id.recyclerView);
    list.setLayoutManager(new LinearLayoutManager(this));
    // Init data source
    RecyclerView.Adapter recyclerAdapter = new ArrayRecyclerAdapter(items);
    // Set data source
    list.setAdapter(recyclerAdapter);
    */

    ListView list = findViewById(R.id.listView);
    list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
  }

  /**
   * If isn't enabled, shows dialog to user. User can open system settings and enable NotificationAccessService.
   *
   * @return true if enabled, false otherwise
   */
  private boolean enableNotificationHandler() {
    if (!isNotificationServiceEnabled()) {
      new AlertDialog.Builder(this)
              .setIcon(R.mipmap.ic_launcher)
              .setTitle(R.string.Application_Name)
              .setMessage(R.string.check_nl_permission)
              .setPositiveButton(
                      R.string.Actions_OK,
                      (dialog, which) -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
              )
              .setNegativeButton(R.string.Actions_No, (dialog, which) -> {
              })
              .show();
      return isNotificationServiceEnabled();
    }
    return true;
  }

  /**
   * @return true if enabled, false otherwise
   */
  private boolean isNotificationServiceEnabled() {
    final String pkgName = getPackageName();
    final String flat = Settings.Secure.getString(getContentResolver(),
            ENABLED_NOTIFICATION_LISTENERS);
    if (!TextUtils.isEmpty(flat)) {
      final String[] names = flat.split(":");
      for (int i = 0; i < names.length; i++) {
        final ComponentName cn = ComponentName.unflattenFromString(names[i]);
        if (cn != null) {
          if (TextUtils.equals(pkgName, cn.getPackageName())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @NonNull
  private String getResourceString(int id) {
    return getResources().getString(id);
  }

}