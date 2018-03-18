package cz.zelenikr.remotetouch.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.security.Hash;
import cz.zelenikr.remotetouch.security.SymmetricKeyGenerator;


/**
 * This helper class simplifies checking values from settings of this application.
 *
 * @author Roman Zelenik
 */
public final class SettingsHelper {

    public static boolean isContactsReadingEnabled(Context context) {
        return PermissionHelper.areContactsPermissionsGranted(context);
    }

    /**
     * Loads actual pair-key. If doesn't exist, generates new.
     *
     * @param context
     * @return actual pair-key value
     */
    public static String getPairKey(Context context) {
        String key = context.getString(R.string.Key_Device_Pair_key);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(key)) {
            return sharedPreferences.getString(key, "");
        } else {
            return regeneratePairKey(context);
        }
    }

    /**
     * Generates, stores and returns new pair-key.
     *
     * @param context
     * @return new generated pair-key value
     */
    public static String regeneratePairKey(Context context) {
        SymmetricKeyGenerator<String> keyGenerator = SecurityHelper.createSymmetricKeyGeneratorInstance();
        String pairKey = keyGenerator.generate();
        setPairKey(context, pairKey);
        return pairKey;
    }

    public static String getToken(Context context) {
        String key = context.getString(R.string.Key_Device_Token);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(key)) {
            return sharedPreferences.getString(key, "");
        } else {
            return refreshToken(context);
        }
    }

    public static String refreshToken(Context context) {
        Hash hash = SecurityHelper.createHashInstance();
        String hashValue = hash.hash(getDeviceName(context) + getPairKey(context));
        setToken(context, hashValue);
        return hashValue;
    }

    public static String getDeviceName(Context context) {
        String key = context.getString(R.string.Key_Device_Name);
        String def = context.getString(R.string.Def_Device_Name);
        return getSharedPreferences(context).getString(key, def);
    }

    private static void setPairKey(Context context, String pairKey) {
        String key = context.getString(R.string.Key_Device_Pair_key);
        getSharedPreferences(context).edit().putString(key, pairKey).apply();
    }

    private static void setToken(Context context, String token) {
        String key = context.getString(R.string.Key_Device_Token);
        getSharedPreferences(context).edit().putString(key, token).apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private SettingsHelper() {
    }
}