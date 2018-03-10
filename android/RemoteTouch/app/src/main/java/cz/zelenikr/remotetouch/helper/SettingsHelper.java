package cz.zelenikr.remotetouch.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cz.zelenikr.remotetouch.R;

/**
 * This helper class simplifies checking values from settings of this application.
 *
 * @author Roman Zelenik
 */
public final class SettingsHelper {

    public static boolean isCallReceiverEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.Key_Calls_Enabled), false)
            && PermissionHelper.areCallingPermissionsGranted(context);
    }

    public static boolean isContactsReadingEnabled(Context context) {
        return PermissionHelper.areContactsPermissionsGranted(context);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private SettingsHelper() {
    }
}
