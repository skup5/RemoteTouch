package cz.zelenikr.remotetouch.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import cz.zelenikr.remotetouch.NavigationActivity;
import cz.zelenikr.remotetouch.data.command.Command;
import cz.zelenikr.remotetouch.data.command.CommandDTO;
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

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
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

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        toast("FCM msg from: " + remoteMessage.getFrom(), Toast.LENGTH_LONG);

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            toast("FCM data: " + remoteMessage.getData(), Toast.LENGTH_LONG);

            if (/* Check if data needs to be processed by long running job */ false) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow(remoteMessage);
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {
        // [START dispatch_job]
//    FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
//    Job myJob = dispatcher.newJobBuilder()
//            .setService(MyJobService.class)
//            .setTag("my-job-tag")
//            .build();
//    dispatcher.schedule(myJob);
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow(RemoteMessage remoteMessage) {
        Log.i(TAG, "handleNow");
        String cmd = remoteMessage.getData().get("cmd");
        if (cmd != null) {
            try {
                Command command = Command.valueOf(cmd);
                Intent intent = new Intent(this, ServerCmdReceiver.class);
                intent.putExtra(ServerCmdReceiver.INTENT_EXTRAS, new CommandDTO(command));
                sendBroadcast(intent);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "handleNow: unknown command " + cmd);
            }
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder notificationBuilder =
            new Notification.Builder(this)
//                    .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void toast(String msg, int duration) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(FCMService.this, msg, duration).show());
    }
}
