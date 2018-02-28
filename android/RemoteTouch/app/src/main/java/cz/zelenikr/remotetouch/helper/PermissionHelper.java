package cz.zelenikr.remotetouch.helper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import cz.zelenikr.remotetouch.MainActivity;

/**
 * Created by Roman on 26.12.2017.
 */

public class PermissionHelper {


  public static final int
      MY_PERMISSIONS_REQUEST_CALL_LOG = 1,
      MY_PERMISSIONS_REQUEST_READ_SMS = 2;
  public static final int MY_PERMISSIONS_REQUEST_SD_CARD_ACCESS = 3;

  public static boolean checkSmsPermission(Activity activity) {
    // Here, thisActivity is the current activity
    if (ContextCompat.checkSelfPermission(activity,
        Manifest.permission.READ_SMS)
        != PackageManager.PERMISSION_GRANTED) {

      // Should we show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
          Manifest.permission.READ_SMS)) {

        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.

        Log.i(getLocalClassName(), activity.getLocalClassName() + " checkSmsPermission(): shouldShowRequestPermissionRationale");
      } else {

        // No explanation needed, we can request the permission.

        ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.READ_SMS},
            MY_PERMISSIONS_REQUEST_READ_SMS);

        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
        Log.i(getLocalClassName(), activity.getLocalClassName() + " checkSmsPermission: request permission");
      }
      return false;
    }
    Log.i(getLocalClassName(), activity.getLocalClassName() + " checkSmsPermission: permission granted");
    return true;
  }

  public static boolean checkCallLogPermission(Activity activity) {
    // Here, thisActivity is the current activity
    if (ContextCompat.checkSelfPermission(activity,
        Manifest.permission.READ_CALL_LOG)
        != PackageManager.PERMISSION_GRANTED) {

      // Should we show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
          Manifest.permission.READ_CALL_LOG)) {

        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.

        Log.i(getLocalClassName(), activity.getLocalClassName() + " checkCallLogPermission(): shouldShowRequestPermissionRationale");
      } else {

        // No explanation needed, we can request the permission.

        ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.READ_CALL_LOG},
            MY_PERMISSIONS_REQUEST_CALL_LOG);

        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
        Log.i(getLocalClassName(), activity.getLocalClassName() + " checkCallLogPermission: request permission");
      }
      return false;
    }
    Log.i(getLocalClassName(), activity.getLocalClassName() + " checkCallLogPermission: permission granted");
    return true;
  }

  private static String getLocalClassName() {
    return PermissionHelper.class.getSimpleName();
  }

  public static boolean checkSDCardPermissions(Activity activity) {
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {

      // Should we show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
          Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

        Log.i(getLocalClassName(), activity.getLocalClassName() + " checkSDCardPermissions(): shouldShowRequestPermissionRationale");
      } else {
        ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            MY_PERMISSIONS_REQUEST_SD_CARD_ACCESS);

        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
        Log.i(getLocalClassName(), activity.getLocalClassName() + " checkSDCardPermissions: request permission");
      }
      return false;
    }
    Log.i(getLocalClassName(), activity.getLocalClassName() + " checkSDCardPermissions: permission granted");
    return true;

  }

  private PermissionHelper() {
  }
}
