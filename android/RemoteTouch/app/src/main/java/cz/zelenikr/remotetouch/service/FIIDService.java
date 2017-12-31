package cz.zelenikr.remotetouch.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * A service that extends FirebaseInstanceIdService to handle the creation, rotation,
 * and updating of registration tokens. This is required for sending to specific devices
 * or for creating device groups.
 *
 * @author Roman Zelenik
 */
public class FIIDService extends FirebaseInstanceIdService {

  private static final String TAG = FIIDService.class.getSimpleName();

  /**
   * Called if InstanceID token is updated. This may occur if the security of
   * the previous token had been compromised. Note that this is called when the InstanceID token
   * is initially generated so this is where you would retrieve the token.
   */
  @Override
  public void onTokenRefresh() {
    // Get updated InstanceID token.
    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
    Log.d(TAG, "Refreshed token: " + refreshedToken);

    // If you want to send messages to this application instance or
    // manage this apps subscriptions on the server side, send the
    // Instance ID token to your app server.
    sendRegistrationToServer(refreshedToken);
  }

  /**
   * Persist token to third-party servers.
   *
   * @param token The new token.
   */
  private void sendRegistrationToServer(String token) {
    // TODO: Implement this method to send token to your app server.
  }
}
