package cz.zelenikr.remotetouch.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * This helper class contains static methods to checking and requesting specific permissions.
 *
 * @author Roman Zelenik
 */
public final class PermissionHelper {

    private static final String TAG = PermissionHelper.class.getSimpleName();

    public static final int
        MY_PERMISSIONS_REQUEST_CALLING = 1,
        MY_PERMISSIONS_REQUEST_SMS = 2,
        MY_PERMISSIONS_REQUEST_CONTACTS = 3;


    // CALLING ////////////////////////////////////////////////

    public static boolean checkCallingPermissions(Activity activity) {
        if (!areCallingPermissionsGranted(activity)) {
            requestCallingPermissions(activity);
            return false;
        }
        Log.i(TAG, activity.getLocalClassName() + " checkCallingPermissions: permission granted");
        return true;
    }

    public static boolean areCallingPermissionsGranted(Context context) {
        return arePermissionsGranted(context,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS);
    }

    public static void requestCallingPermissions(Activity activity) {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALL_LOG) ||
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE) ||
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.PROCESS_OUTGOING_CALLS)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            Log.i(TAG, activity.getLocalClassName() + " checkCallingPermissions(): shouldShowRequestPermissionRationale");
        }

        // No explanation needed, we can request the permission.

        ActivityCompat.requestPermissions(activity,
            new String[]{
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.PROCESS_OUTGOING_CALLS
            },
            MY_PERMISSIONS_REQUEST_CALLING);

        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
        Log.i(TAG, activity.getLocalClassName() + " checkCallingPermissions: request permission");
    }

    public static void requestCallingPermissions(Fragment fragment) {
        // Should we show an explanation?
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.READ_CALL_LOG) ||
            fragment.shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) ||
            fragment.shouldShowRequestPermissionRationale(Manifest.permission.PROCESS_OUTGOING_CALLS)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            Log.i(TAG, fragment.getClass().getSimpleName() + " checkCallingPermissions(): shouldShowRequestPermissionRationale");
        }

        // No explanation needed, we can request the permission.

        fragment.requestPermissions(
            new String[]{
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.PROCESS_OUTGOING_CALLS
            },
            MY_PERMISSIONS_REQUEST_CALLING);

        Log.i(TAG, fragment.getClass().getSimpleName() + " checkCallingPermissions: request permission");
    }

    // SMS ////////////////////////////////////////////////////

    public static boolean checkSmsPermissions(Activity activity) {
        if (!areSmsPermissionsGranted(activity)) {
            requestSmsPermissions(activity);
            return false;
        }
        Log.i(TAG, activity.getLocalClassName() + " checkSmsPermissions: permissions granted");
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

            Log.i(TAG, activity.getLocalClassName() + " requestSmsPermissions(): shouldShowRequestPermissionRationale");
        }

        ActivityCompat.requestPermissions(activity,
            new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS
            },
            MY_PERMISSIONS_REQUEST_SMS);

        Log.i(TAG, activity.getLocalClassName() + " requestSmsPermissions: request permission");
    }

    public static void requestSmsPermissions(Fragment fragment) {
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS) ||
            fragment.shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS) ||
            fragment.shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
            // Show an non-blocking explanation here.

            Log.i(TAG, fragment.getClass().getSimpleName() + " requestSmsPermissions(): shouldShowRequestPermissionRationale");
        }

        fragment.requestPermissions(
            new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS
            },
            MY_PERMISSIONS_REQUEST_SMS);

        Log.i(TAG, fragment.getClass().getSimpleName() + " requestSmsPermissions: request permission");
    }

    // CONTACTS //////////////////////////////////////////////////

    public static boolean checkContactsPermissions(Activity activity) {
        if (!areContactsPermissionsGranted(activity)) {
            requestContactsPermissions(activity);
            return false;
        }
        Log.i(TAG, activity.getLocalClassName() + " checkContactsPermissions: permissions granted");
        return true;
    }

    public static boolean areContactsPermissionsGranted(Context context) {
        return isPermissionGranted(context, Manifest.permission.READ_CONTACTS);
    }

    public static void requestContactsPermissions(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)) {
            // Show an non-blocking explanation here.

            Log.i(TAG, activity.getLocalClassName() + " requestContactsPermissions(): shouldShowRequestPermissionRationale");
        }

        ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.READ_CONTACTS},
            MY_PERMISSIONS_REQUEST_CONTACTS);

        Log.i(TAG, activity.getLocalClassName() + " requestContactsPermissions: request permission");
    }

    public static void requestContactsPermissions(Fragment fragment) {
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            // Show an non-blocking explanation here.

            Log.i(TAG, fragment.getClass().getSimpleName() + " requestContactsPermissions(): shouldShowRequestPermissionRationale");
        }

        fragment.requestPermissions(
            new String[]{Manifest.permission.READ_CONTACTS},
            MY_PERMISSIONS_REQUEST_CONTACTS);

        Log.i(TAG, fragment.getClass().getSimpleName() + " requestContactsPermissions: request permission");
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

    private PermissionHelper() {
    }
}
