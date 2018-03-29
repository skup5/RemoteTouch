package cz.zelenikr.remotetouch.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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
import cz.zelenikr.remotetouch.helper.ConnectionHelper;
import cz.zelenikr.remotetouch.helper.SettingsHelper;
import cz.zelenikr.remotetouch.network.JsonSecureRestClient;
import cz.zelenikr.remotetouch.network.SecureRestClient;
import cz.zelenikr.remotetouch.security.exception.UnsupportedCipherException;

import static cz.zelenikr.remotetouch.helper.NotificationHelper.APP_ICON_ID;

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
    private Looper serviceLooper;
    private MessageHandler messageHandler;
    private SecureRestClient restClient;

    // TODO: 29.3.2018 Queue for unsent messages

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
            messageHandler.obtainMessage(0, intent).sendToTarget();
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

    private void initRestClient(String token, String url, String key) {
        try {
//      this.restClient = new JsonSimpleRestClient(loadClientToken(), new URL(loadRestUrl()));
            this.restClient = new JsonSecureRestClient(token, new URL(url), key);
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
        messageHandler = new MessageHandler(looper);
    }

    private String loadClientToken() {
        return SettingsHelper.getToken(this);
//        return "1";
    }

    private String loadRestUrl() {
        return SettingsHelper.getServerUrl(this);
    }

    private String loadSecureKey() {
        String key = SettingsHelper.getPairKey(this);
        if (key.isEmpty()) Log.e(TAG, "loadSecureKey: key is missing");
        return key;
    }

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

        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    private boolean isConnected() {
        return ConnectionHelper.isConnected(this);
    }

    private void registerOnPreferenceChangedListener() {
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).registerOnSharedPreferenceChangeListener(this);
    }

    private void unregisterOnPreferenceChangedListener() {
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).unregisterOnSharedPreferenceChangeListener(this);
    }


    private final class MessageHandler extends Handler {

        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof Intent) {
                final Intent intent = (Intent) msg.obj;
                final Serializable serializableExtra = intent.getSerializableExtra(INTENT_EXTRA_NAME);
                if (serializableExtra == null) {
                    Log.w(TAG, "DTO is null");
                    return;
                }
                if (serializableExtra instanceof EventDTO) {
                    handleEvent((EventDTO) serializableExtra);
                } else {
                    Log.w(TAG, serializableExtra.getClass().getSimpleName() + " isn't available DTO instance.");
                }
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }

        private void handleEvent(EventDTO event) {
            Log.i(TAG, "received event " + event.getType().name() + ": " + event.getContent().toString());
            if (isConnected()) {
                boolean result = restClient.send(event, EVENT_REST_PATH);
            } else {
                Log.i(TAG, "handleEvent: device is not connected to network");
            }
        }

    }
}
