package cz.zelenikr.remotetouch.service;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import cz.zelenikr.remotetouch.data.command.Command;
import cz.zelenikr.remotetouch.data.command.CommandDTO;
import cz.zelenikr.remotetouch.helper.NotificationHelper;
import cz.zelenikr.remotetouch.receiver.ServerCmdReceiver;

/**
 * A service that extends FirebaseMessagingService. This is required if you want to do any message
 * handling beyond receiving notifications on apps in the background.
 * To receive notifications in foregrounded apps, to receive data payload,
 * to send upstream messages, and so on, you must extend this service.
 *
 * @author Roman Zelenik
 */
public class FCMService extends FirebaseMessagingService {

    private static final String TAG = FCMService.class.getSimpleName();
    private static int remoteNotificationId = 0;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        toast("FCM msg from: " + remoteMessage.getFrom(), Toast.LENGTH_LONG);

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            toast("FCM data: " + remoteMessage.getData(), Toast.LENGTH_LONG);

            // Handle message within 10 seconds
            handleDataMessage(remoteMessage);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            NotificationHelper.notify(this, remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), remoteNotificationId);
        }

    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleDataMessage(RemoteMessage remoteMessage) {
        Log.i(TAG, "handleDataMessage");
        String cmd = remoteMessage.getData().get("cmd");
        if (cmd != null) {
            try {
                Command command = Command.valueOf(cmd);
                Intent intent = new Intent(this, ServerCmdReceiver.class);
                intent.putExtra(ServerCmdReceiver.INTENT_EXTRAS, new CommandDTO(command));
                sendBroadcast(intent);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "handleDataMessage: unknown command " + cmd);
            }
        }
    }

    private void toast(String msg, int duration) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(FCMService.this, msg, duration).show());
    }
}
