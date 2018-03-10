package cz.zelenikr.remotetouch.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * This helper class simplifies checking values from settings of this application.
 *
 * @author Roman Zelenik
 */
public final class SettingsHelper {

    public static boolean isContactsReadingEnabled(Context context) {
        return PermissionHelper.areContactsPermissionsGranted(context);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private SettingsHelper() {
    }
}
