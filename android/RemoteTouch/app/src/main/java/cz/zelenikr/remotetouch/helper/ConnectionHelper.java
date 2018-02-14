package cz.zelenikr.remotetouch.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Roman Zelenik
 */
public class ConnectionHelper {

  public static boolean isConnected(Context context) {
    NetworkInfo activeNetwork = getNetworkInfo(context);
    return activeNetwork != null && activeNetwork.isConnected();
  }

  public static boolean isWiFiConnected(Context context) {
    NetworkInfo activeNetwork = getNetworkInfo(context);
    return activeNetwork != null && activeNetwork.isConnected() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
  }

  private static NetworkInfo getNetworkInfo(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo();
  }

  private ConnectionHelper(){}
}
