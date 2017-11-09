package cz.zelenikr.remotetouch;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.util.Date;
import java.util.List;

/**
 * @author Roman Zelenik
 */
public class MainActivity extends AppCompatActivity {

  private static final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
  private static final Uri CALLS = Uri.parse("content://call_log/calls");

  private static final int MY_PERMISSIONS_REQUEST_CALL_LOG = 1;
  private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 2;

  private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(getLocalClassName(), "Add notification");
        NotificationHelper.test(getApplicationContext(), (int) System.currentTimeMillis());
      }
    });

  }


  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    NotificationHelper.persistent(getApplicationContext(), getResources().getString(R.string.app_name), "Persistent notification", 1);
    enableNotificationHandler();
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
    if (!checkCallLogPermission()) return;
    //checkCallLogPermission();

    ListView list = (ListView) findViewById(R.id.listView);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getCallDetails());
    list.setAdapter(adapter);
  }

  public void onSmsBtClick(View view) {
    if (!checkSmsPermission()) return;

    ListView list = (ListView) findViewById(R.id.listView);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    String[] cols = new String[]{"date", "person", "address", "read", "body"};

    // returns last 3 received sms ordered by date (and unread)
    Cursor cursor = getContentResolver().query(SMS_INBOX, cols,
            "read=0", null, "read, date desc limit 3");

    String message = "";

    while (cursor.moveToNext()) {
      message = cols[0].toUpperCase() + ": " + new Date(Long.valueOf(cursor.getString(0))) + "\n";

      for (int i = 1; i < cols.length; i++)
        message += cols[i].toUpperCase() + ": " + cursor.getString(i) + "\n";

      adapter.add(message);
    }
    list.setAdapter(adapter);
  }

  private List<String> getCallDetails() {
    List<String> calls = new ArrayList<>();
    String callDetail;

    Cursor managedCursor = getContentResolver().query(CALLS, null, null, null, "date desc");
    if (managedCursor == null) {
      calls.add("-empty cursor-");
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

  private boolean checkSmsPermission() {
    // Here, thisActivity is the current activity
    if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED) {

      // Should we show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(this,
              Manifest.permission.READ_SMS)) {

        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.

        Log.i(getLocalClassName(), "checkSmsPermission(): shouldShowRequestPermissionRationale");
      } else {

        // No explanation needed, we can request the permission.

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_SMS},
                MY_PERMISSIONS_REQUEST_READ_SMS);

        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
        Log.i(getLocalClassName(), "checkSmsPermission: request permission");
      }
      return false;
    }
    Log.i(getLocalClassName(), "checkSmsPermission: permission granted");
    return true;
  }

  private boolean checkCallLogPermission() {
    // Here, thisActivity is the current activity
    if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED) {

      // Should we show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(this,
              Manifest.permission.READ_CALL_LOG)) {

        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.

        Log.i(getLocalClassName(), "checkCallLogPermission(): shouldShowRequestPermissionRationale");
      } else {

        // No explanation needed, we can request the permission.

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CALL_LOG},
                MY_PERMISSIONS_REQUEST_CALL_LOG);

        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
        Log.i(getLocalClassName(), "checkCallLogPermission: request permission");
      }
      return false;
    }
    Log.i(getLocalClassName(), "checkCallLogPermission: permission granted");
    return true;
  }

  private void enableNotificationHandler() {
    if (!isNotificationServiceEnabled()) {
      new AlertDialog.Builder(this)
              .setIcon(R.mipmap.ic_launcher)
              .setTitle(R.string.app_name)
              .setMessage(R.string.check_nl_permission)
              .setPositiveButton(
                      R.string.yes,
                      (dialog, which) -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
              )
              .setNegativeButton(R.string.no, (dialog, which) -> {
              })
              .show();
    }
  }

  /**
   * @return true if enabled, false otherwise
   */
  private boolean isNotificationServiceEnabled() {
    String pkgName = getPackageName();
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
}