package cz.zelenikr.remotetouch.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.ConnectionType;
import cz.zelenikr.remotetouch.security.Hash;
import cz.zelenikr.remotetouch.security.SymmetricKeyGenerator;


/**
 * This helper class simplifies reading values from settings of this application.
 *
 * @author Roman Zelenik
 */
public final class SettingsHelper {

    public static final String TAG = SettingsHelper.class.getSimpleName();

    public static boolean isContactsReadingEnabled(@NonNull Context context) {
        return PermissionHelper.areContactsPermissionsGranted(context);
    }

    /**
     * Returns true if user was enabled calls handling, processing and resending.
     *
     * @param context
     * @return
     */
    public static boolean areCallsEnabled(@NonNull Context context) {
        String key = context.getString(R.string.Key_Calls_Enabled);
        Boolean def = context.getString(R.string.Def_Calls_Enabled).equals("true");
        return getSharedPreferences(context).getBoolean(key, def);
    }

    /**
     * Returns true if user has enabled notifications handling, processing and resending.
     *
     * @param context
     * @return
     */
    public static boolean areNotificationsEnabled(@NonNull Context context) {
        String key = context.getString(R.string.Key_Notifications_Enabled);
        Boolean def = context.getString(R.string.Def_Notifications_Enabled).equals("true");
        return getSharedPreferences(context).getBoolean(key, def);
    }

    /**
     * Returns true if user was enabled sms handling, processing and resending.
     *
     * @param context
     * @return
     */
    public static boolean areSmsEnabled(@NonNull Context context) {
        String key = context.getString(R.string.Key_Sms_Enabled);
        Boolean def = context.getString(R.string.Def_Sms_Enabled).equals("true");
        return getSharedPreferences(context).getBoolean(key, def);
    }

    /**
     * Loads actual pair-key. If doesn't exist, generates new.
     *
     * @param context
     * @return actual pair-key value
     */
    public static String getPairKey(@NonNull Context context) {
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
    public static String regeneratePairKey(@NonNull Context context) {
        SymmetricKeyGenerator<String> keyGenerator = SecurityHelper.createSymmetricKeyGeneratorInstance();
        String pairKey = keyGenerator.generate();
        setPairKey(context, pairKey);
        refreshToken(context);
        return pairKey;
    }

    public static String getToken(@NonNull Context context) {
        String key = context.getString(R.string.Key_Device_Token);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(key)) {
            return sharedPreferences.getString(key, "");
        } else {
            return refreshToken(context);
        }
    }

    public static String refreshToken(@NonNull Context context) {
//        Log.i(TAG, "refreshToken");
        Hash hash = SecurityHelper.createHashInstance();
        String hashValue = hash.hash(getDeviceName(context) + getPairKey(context));
        setToken(context, hashValue);
        return hashValue;
    }

    public static String getDeviceName(@NonNull Context context) {
        String key = context.getString(R.string.Key_Device_Name);
        String def = context.getString(R.string.Def_Device_Name);
        return getSharedPreferences(context).getString(key, def);
    }

    public static String getServerUrl(@NonNull Context context) {
        String key = context.getString(R.string.Key_Connection_Server);
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences.contains(key)) {
            return preferences.getString(key, "");
        } else {
            String def = context.getString(R.string.Def_Server_Url);
            setServerUrl(context, def);
            return def;
        }
    }

    /**
     * Returns set of application package names that the user selected.
     *
     * @param context
     * @return
     */
    public static Set<String> getNotificationsApps(@NonNull Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        String[] defSelection = context.getResources().getStringArray(R.array.Def_AppList_Selected_apps);
        String key = context.getString(R.string.Key_Notifications_Selected_apps);
        Set<String> apps = new ArraySet<>(Arrays.asList(defSelection));
        apps.addAll(preferences.getStringSet(key, Collections.emptySet()));
        return apps;
    }

    public static Set<ConnectionType> getAvailableConnections(@NonNull Context context) {
        String key = context.getString(R.string.Key_Connection_Type);
        SharedPreferences preferences = getSharedPreferences(context);
        Set<String> strTypes = new ArraySet<>();
        strTypes.addAll(preferences.getStringSet(key, Collections.emptySet()));
        Set<ConnectionType> types = new ArraySet<>();
        for (String strType : strTypes) types.add(ConnectionType.valueOf(strType));
        return types;
    }

    public static boolean isRemoteClientConnected(@NonNull Context context) {
        String key = context.getString(R.string.Key_RemoteClient_Connected);
        return getSharedPreferences(context).getBoolean(key, false);
    }

    public static void storeRemoteClientConnected(@NonNull Context context, boolean connected) {
        String key = context.getString(R.string.Key_RemoteClient_Connected);
        getSharedPreferences(context).edit().putBoolean(key, connected).apply();
    }

    private static void setPairKey(Context context, String pairKey) {
        String key = context.getString(R.string.Key_Device_Pair_key);
        getSharedPreferences(context).edit().putString(key, pairKey).apply();
    }

    private static void setToken(Context context, String token) {
        String key = context.getString(R.string.Key_Device_Token);
        getSharedPreferences(context).edit().putString(key, token).apply();
    }

    private static void setServerUrl(Context context, String serverUrl) {
        String key = context.getString(R.string.Key_Connection_Server);
        getSharedPreferences(context).edit().putString(key, serverUrl).apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private SettingsHelper() {
    }
}
