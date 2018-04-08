package cz.zelenikr.remotetouch.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

import cz.zelenikr.remotetouch.network.JsonSimpleRestClient;

/**
 * @author Roman Zelenik
 */
public class ConnectionHelper {

    /**
     * Indicates whether network connectivity exists and it is possible to establish connections
     * and pass data.
     *
     * @param context required to access system service
     * @return true if network connectivity exists, false otherwise
     */
    public static boolean isConnected(@NonNull Context context) {
        NetworkInfo activeNetwork = getNetworkInfo(context);
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static boolean isWiFiConnected(@NonNull Context context) {
        NetworkInfo activeNetwork = getNetworkInfo(context);
        return activeNetwork != null && activeNetwork.isConnected() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Tries connect to the sever with specific address.
     *
     * @param context
     * @param address the given address (including port)
     * @return true if address is valid and server was responded
     */
    public static boolean tryServer(@NonNull Context context, @NonNull String address) {
        try {
            return new JsonSimpleRestClient(SettingsHelper.getToken(context), new URL(address)).ping();
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private static NetworkInfo getNetworkInfo(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    private ConnectionHelper() {
    }

}
