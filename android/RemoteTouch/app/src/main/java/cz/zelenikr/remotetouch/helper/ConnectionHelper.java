package cz.zelenikr.remotetouch.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import cz.zelenikr.remotetouch.data.ConnectionType;
import cz.zelenikr.remotetouch.network.JsonSimpleRestClient;

/**
 * @author Roman Zelenik
 */
public final class ConnectionHelper {

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

    /**
     * Indicates whether network connectivity exists and it is possible to establish connections
     * and pass data. It must be used one of available connection (connection type which user enabled).
     *
     * @param context required to access system service
     * @return
     */
    public static boolean isUsedAvailableConnection(@NonNull Context context) {
        NetworkInfo activeNetwork = getNetworkInfo(context);
        Set<ConnectionType> types = SettingsHelper.getAvailableConnections(context);
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return (types.contains(ConnectionType.ROAMING) && activeNetwork.isRoaming())
                || (types.contains(ConnectionType.MOBILE) && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE && !activeNetwork.isRoaming())
                || (types.contains(ConnectionType.WIFI) && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
        }
        return false;
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
            return new JsonSimpleRestClient(SettingsHelper.getToken(context), new URL(address), context).ping();
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
