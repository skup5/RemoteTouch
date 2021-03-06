package cz.zelenikr.remotetouch.service;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import cz.zelenikr.remotetouch.data.command.Command;
import cz.zelenikr.remotetouch.data.command.CommandDTO;
import cz.zelenikr.remotetouch.receiver.ServerCmdReceiver;

/**
 * A service that extends FirebaseInstanceIdService to handle the creation, rotation,
 * and updating of registration tokens. This is required for sending to specific devices
 * or for creating device groups.
 *
 * @author Roman Zelenik
 */
public class FIIDService extends FirebaseInstanceIdService {

    private static final String TAG = FIIDService.class.getSimpleName();
    private static final boolean SHOW_TOASTS = false;

    public static String getFirebaseToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = getFirebaseToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        toast("Got FCM token", Toast.LENGTH_LONG);

        // Subscribe to receiving broadcasts for android clients from your server.
        FirebaseMessaging.getInstance().subscribeToTopic(getPackageName());

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
        Intent intent = new Intent(this, ServerCmdReceiver.class);
        intent.putExtra(ServerCmdReceiver.INTENT_EXTRAS, new CommandDTO(Command.FCM_SIGN_UP));
        sendBroadcast(intent);
    }

    private void toast(String msg, int duration) {
        if (SHOW_TOASTS)
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(FIIDService.this, msg, duration).show());
    }
}
