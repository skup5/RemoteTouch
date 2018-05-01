package cz.zelenikr.remotetouch.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import cz.zelenikr.remotetouch.NavigationActivity;
import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.event.EventDTO;
import cz.zelenikr.remotetouch.helper.ApiHelper;
import cz.zelenikr.remotetouch.helper.ConnectionHelper;
import cz.zelenikr.remotetouch.helper.NotificationHelper;
import cz.zelenikr.remotetouch.helper.SettingsHelper;
import cz.zelenikr.remotetouch.network.JsonSecureRestClient;
import cz.zelenikr.remotetouch.network.SecureRestClient;
import cz.zelenikr.remotetouch.security.exception.UnsupportedCipherException;

import static cz.zelenikr.remotetouch.helper.NotificationHelper.APP_ICON_ID;
import static cz.zelenikr.remotetouch.helper.NotificationHelper.ONGOING_NOTIFICATION_CHANNEL_DESCRIPTION;
import static cz.zelenikr.remotetouch.helper.NotificationHelper.ONGOING_NOTIFICATION_CHANNEL_ID;
import static cz.zelenikr.remotetouch.helper.NotificationHelper.ONGOING_NOTIFICATION_CHANNEL_NAME;

/**
 * @author Roman Zelenik
 */
public class MessageSenderService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String
        INTENT_EXTRA_IS_MSG = "isMsg",
        INTENT_EXTRA_NAME = "cz.zelenikr.remotetouch.MessageSender";

    private static final String EVENT_REST_PATH = "/event";

    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static final String TAG = MessageSenderService.class.getSimpleName();
    private static final int RESENT_MSG_ATTEMPTS = 3, RESENT_MSG_DELAY = 5000;

    private Looper serviceLooper;
    private MessageHandler serviceMessageHandler;
    private SecureRestClient restClient;


    @Override
    public void onCreate() {
        super.onCreate();

        // Start this service in the foreground and show persistent notification
        showNotification();

        // Try initialize rest client
        initRestClient(loadClientToken(), loadRestUrl(), loadSecureKey());

        // Initialize HandlerThread and Looper and use it for MessageHandler
        initServiceLooper();
        initServiceHandler(serviceLooper);

        registerOnPreferenceChangedListener();

        Log.i(TAG, "Was created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra(INTENT_EXTRA_IS_MSG, false)) {
            serviceMessageHandler.obtainMessage(0, intent).sendToTarget();
        }

        // Restart after kill
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterOnPreferenceChangedListener();
        Log.i(TAG, "Was destroyed");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.Key_Device_Pair_key)) && sharedPreferences.contains(key)) {
            restClient.setSecureKey(sharedPreferences.getString(key, ""));
//            Log.i(TAG, "onSharedPreferenceChanged: pairKey");
        } else if (key.equals(getString(R.string.Key_Device_Token)) && sharedPreferences.contains(key)) {
            restClient.setClientToken(sharedPreferences.getString(key, ""));
//            Log.i(TAG, "onSharedPreferenceChanged: token");
        } else if (key.equals(getString(R.string.Key_Connection_Server)) && sharedPreferences.contains(key)) {
            Log.i(TAG, "onSharedPreferenceChanged: server url");
            try {
                URL url = new URL(sharedPreferences.getString(key, ""));
                restClient.setRestServer(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes {@link SecureRestClient} using the specific client token, server url and encryption key.
     *
     * @param token the given client identification token
     * @param url   the given  base rest server url
     * @param key   the given encryption key
     */
    private void initRestClient(String token, String url, String key) {
        try {
//      this.restClient = new JsonSimpleRestClient(loadClientToken(), new URL(loadRestUrl()));
            this.restClient = new JsonSecureRestClient(token, new URL(url), key, this);
        } catch (MalformedURLException | UnsupportedCipherException e) {
            Log.e(TAG, e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    private void initServiceLooper() {
        HandlerThread handlerThread = new HandlerThread("EventServiceHandlerThread",
            Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        serviceLooper = handlerThread.getLooper();
    }

    private void initServiceHandler(Looper looper) {
        serviceMessageHandler = new MessageHandler(looper);
        serviceMessageHandler.setAttempts(RESENT_MSG_ATTEMPTS);
        serviceMessageHandler.setDelay(RESENT_MSG_DELAY);
    }

    /**
     * Reads client token from device store.
     *
     * @return client token or empty string if error occurs
     */
    private String loadClientToken() {
        return SettingsHelper.getToken(this);
//        return "1";
    }

    /**
     * Reads rest server url from device store.
     *
     * @return url or empty string if error occurs
     */
    private String loadRestUrl() {
        return SettingsHelper.getServerUrl(this);
    }

    /**
     * Reads secure (encryption) key from device store.
     *
     * @return key or empty string if error occurs
     */
    private String loadSecureKey() {
        String key = SettingsHelper.getPairKey(this);
        if (key.isEmpty()) Log.e(TAG, "loadSecureKey: key is missing");
        return key;
    }

    /**
     * Make this service run in the foreground, supplying the ongoing notification
     * to be shown to the user while in this state.
     */
    private void showNotification() {
        Intent notificationIntent = new Intent(this, NavigationActivity.class);
        PendingIntent pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification.Builder builder =
            new Notification.Builder(this)
                .setContentTitle(getString(R.string.Application_Name) + " (MessageSenderService)")
                .setContentText(getString(R.string.MsgSenderService_PersistentNotification_Text))
                .setSmallIcon(APP_ICON_ID)
                .setShowWhen(true)
                // .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_HIGH)
//                .addAction()
                .setContentIntent(pendingIntent);

        if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.O)) {
            NotificationHelper.createNotificationChannel(
                this,
                getString(ONGOING_NOTIFICATION_CHANNEL_ID),
                getString(ONGOING_NOTIFICATION_CHANNEL_NAME),
                getString(ONGOING_NOTIFICATION_CHANNEL_DESCRIPTION)
            );
            builder.setChannelId(getString(ONGOING_NOTIFICATION_CHANNEL_ID));
        }

        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    /**
     * Checks if device is connected with a remote client. Device is connected if it has access
     * to a network and remote client is online.
     *
     * @return true if device is connected with a remote client
     */
    private boolean isConnected() {
        return ConnectionHelper.isUsedAvailableConnection(this) && SettingsHelper.isRemoteClientConnected(this);
    }

    private void registerOnPreferenceChangedListener() {
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).registerOnSharedPreferenceChangeListener(this);
    }

    private void unregisterOnPreferenceChangedListener() {
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).unregisterOnSharedPreferenceChangeListener(this);
    }


    /**
     * This {@link Handler} processes intents received by {@link MessageSenderService} service.
     */
    private final class MessageHandler extends Handler {

        /**
         * millis delay for message resent
         */
        private long delay = 0;

        /**
         * current processed message
         */
        private Message currentMsg;

        /**
         * number of attempts to sent message
         */
        private int attempts = 1;

        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            currentMsg = msg;

            // is new msg, set number of attempts
            if (msg.arg1 == 0) msg.arg1 = getAttempts();

            // msg has right content
            if (msg.obj instanceof Intent) {
                final Intent intent = (Intent) msg.obj;
                final Serializable serializableExtra = intent.getSerializableExtra(INTENT_EXTRA_NAME);
                // data is missing
                if (serializableExtra == null) {
                    Log.w(TAG, "DTO is null");
                    return;
                }
                // valid data type
                if (serializableExtra instanceof EventDTO) {
                    handleEvent((EventDTO) serializableExtra);
                } else {
                    Log.w(TAG, serializableExtra.getClass().getSimpleName() + " isn't available DTO instance.");
                }
            }
        }

        public int getAttempts() {
            return attempts;
        }

        /**
         * Sets message resent attempts.
         *
         * @param attempts should be greater then 0
         */
        public void setAttempts(int attempts) {
            if (attempts <= 0)
                throw new IllegalArgumentException("Value has to be greater then 0");

            this.attempts = attempts;
        }

        public long getDelay() {
            return delay;
        }

        /**
         * Sets delay for message resent.
         *
         * @param delay (milliseconds) should be positive digit or zero
         */
        public void setDelay(long delay) {
            if (delay < 0)
                throw new IllegalArgumentException("Value can be only positive digit or zero.");

            this.delay = delay;
        }

        /**
         * Processes the specific {@link EventDTO} and sends it to the server.
         *
         * @param event the given event
         */
        private void handleEvent(EventDTO event) {
            Log.i(TAG, "received event " + event.getType().name() + ": " + event.getContent().toString());
            // sends message only if some remote client is connected
            if (isConnected()) {
                // message has not been sent
                if (!restClient.send(event, EVENT_REST_PATH)) {
                    // msg has enough number of resent attempts
                    if (currentMsg.arg1 - 1 > 0) {
                        currentMsg.arg1--;
                        // return msg into the message queue
                        sendMessageDelayed(copyOf(currentMsg), getDelay());
                        Log.i(TAG, "Resent EVENT after " + getDelay() / 1000 + "s");
                    }
                }
            } else {
                Log.i(TAG, "handleEvent: device is not connected to network or remote client is offline");
            }
        }

        private Message copyOf(Message msg) {
            return obtainMessage(msg.what, msg.arg1, msg.arg2, msg.obj);
        }
    }
}
