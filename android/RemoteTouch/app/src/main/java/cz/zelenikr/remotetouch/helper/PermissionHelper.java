package cz.zelenikr.remotetouch.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * @author Roman Zelenik
 */
public class PermissionHelper {

  public static final int
      MY_PERMISSIONS_REQUEST_CALL_LOG = 1,
      MY_PERMISSIONS_REQUEST_SMS = 2;


  // READ CALL LOG ////////////////////////////////////////////////

  public static boolean checkCallLogPermission(Activity activity) {
    if (!isReadCallLogPermissionGranted(activity)) {
      requestReadCallLogPermission(activity);
      return false;
    }
    Log.i(getLocalClassName(), activity.getLocalClassName() + " checkCallLogPermission: permission granted");
    return true;
  }

  public static boolean isReadCallLogPermissionGranted(Context context) {
    return isPermissionGranted(context, Manifest.permission.READ_CALL_LOG);
  }

  public static void requestReadCallLogPermission(Activity activity) {
    // Should we show an explanation?
    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALL_LOG)) {

      // Show an explanation to the user *asynchronously* -- don't block
      // this thread waiting for the user's response! After the user
      // sees the explanation, try again to request the permission.

      Log.i(getLocalClassName(), activity.getLocalClassName() + " checkCallLogPermission(): shouldShowRequestPermissionRationale");
    } else {

      // No explanation needed, we can request the permission.

      ActivityCompat.requestPermissions(activity,
          new String[]{Manifest.permission.READ_CALL_LOG}, MY_PERMISSIONS_REQUEST_CALL_LOG);

      // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
      // app-defined int constant. The callback method gets the
      // result of the request.
      Log.i(getLocalClassName(), activity.getLocalClassName() + " checkCallLogPermission: request permission");
    }
  }

  // SMS ////////////////////////////////////////////////////

  public static boolean checkSmsPermissions(Activity activity) {
    if (!areSmsPermissionsGranted(activity)) {
      requestSmsPermissions(activity);
      return false;
    }
    Log.i(getLocalClassName(), activity.getLocalClassName() + " checkSmsPermissions: permissions granted");
    return true;
  }

  public static boolean areSmsPermissionsGranted(Context context) {
    return arePermissionsGranted(context, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS);
  }

  public static void requestSmsPermissions(Activity activity) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS) ||
        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECEIVE_SMS) ||
        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.SEND_SMS)) {
      // Show an non-blocking explanation here.

      Log.i(getLocalClassName(), activity.getLocalClassName() + " requestSmsPermissions(): shouldShowRequestPermissionRationale");
    } else {
      ActivityCompat.requestPermissions(activity,
          new String[]{
              Manifest.permission.READ_SMS,
              Manifest.permission.RECEIVE_SMS,
              Manifest.permission.SEND_SMS
          },
          MY_PERMISSIONS_REQUEST_SMS);

      Log.i(getLocalClassName(), activity.getLocalClassName() + " requestSmsPermissions: request permission");
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  private static boolean isPermissionGranted(Context context, String permission) {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
  }

  private static boolean arePermissionsGranted(Context context, String... permissions) {
    for (String permission : permissions) {
      if (!isPermissionGranted(context, permission)) return false;
    }
    return true;
  }

  private static String getLocalClassName() {
    return PermissionHelper.class.getSimpleName();
  }

  private PermissionHelper() {
  }
}
