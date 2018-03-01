package cz.zelenikr.remotetouch.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import cz.zelenikr.remotetouch.MainActivity;
import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.dto.EventDTO;
import cz.zelenikr.remotetouch.helper.ConnectionHelper;
import cz.zelenikr.remotetouch.network.RestClient;
import cz.zelenikr.remotetouch.network.SimpleRestClient;

import static cz.zelenikr.remotetouch.helper.NotificationHelper.APP_ICON_ID;

/**
 * @author Roman Zelenik
 */
public class EventService extends Service {

  public static final String
      INTENT_EXTRA_EVENT = "event",
      INTENT_EXTRA_CONTENT = "content",
      INTENT_EXTRA_NAME = "cz.zelenikr.remotetouch.Event";

  private static final int ONGOING_NOTIFICATION_ID = 1;
  private static final String TAG = EventService.class.getSimpleName();
  private Looper serviceLooper;
  private EventHandler eventHandler;
  private RestClient restClient;
  // private SmsReceiver smsReceiver = new SmsReceiver();


  @Override
  public void onCreate() {
    super.onCreate();

    // Start this service in the foreground and show persistent notification
    showNotification();

    // Try initialize rest client
    try {
      this.restClient = new SimpleRestClient(loadClientToken(), new URL(loadRestUrl()));
    } catch (MalformedURLException e) {
      Log.e(TAG, e.getLocalizedMessage());
      throw new RuntimeException(e);
    }

    // Initialize HandlerThread and Looper and use it for EventHandler
    HandlerThread handlerThread = new HandlerThread("EventServiceHandlerThread",
        Process.THREAD_PRIORITY_BACKGROUND);
    handlerThread.start();
    serviceLooper = handlerThread.getLooper();
    eventHandler = new EventHandler(serviceLooper);

    // Register SmsReceiver
//    IntentFilter intentFilter = new IntentFilter(SmsReceiver.ACTION);
//    intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
    //  registerReceiver(smsReceiver, intentFilter);

    Log.i(TAG, "Was created");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && intent.getBooleanExtra(INTENT_EXTRA_EVENT, false)) {
      eventHandler.obtainMessage(0, intent).sendToTarget();
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

    //unregisterReceiver(smsReceiver);

    Log.i(TAG, "Was destroyed");
  }

  private String loadClientToken() {
    // TODO: get from authentication
    return "1";
  }

  private String loadRestUrl() {
    // TODO: load from Preferences
//    return "http://10.0.0.46:4000";
    return "http://remote-touch.azurewebsites.net/event";
  }

  private void showNotification() {
    Intent notificationIntent = new Intent(this, MainActivity.class);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Notification notification =
        new Notification.Builder(this)
            .setContentTitle(getString(R.string.Application_Name) + " (EventService)")
            .setContentText(getString(R.string.EventService_PersistentNotification_Text))
            .setSmallIcon(APP_ICON_ID)
            .setShowWhen(true)
            // .setAutoCancel(false)
            .setPriority(Notification.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build();

    startForeground(ONGOING_NOTIFICATION_ID, notification);
  }

  private boolean isConnected() {
    return ConnectionHelper.isConnected(this);
  }

  private final class EventHandler extends Handler {

    public EventHandler(Looper looper) {
      super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
      if (msg.obj instanceof Intent) handleEvent((Intent) msg.obj);

      // Stop the service using the startId, so that we don't stop
      // the service in the middle of handling another job
      //stopSelf(msg.arg1);
    }

    private void handleEvent(Intent intent) {
      final Serializable serializableExtra = intent.getSerializableExtra(INTENT_EXTRA_NAME);
      if (serializableExtra == null) {
        Log.w(TAG, "EventDTO is null");
        return;
      }
      if (serializableExtra instanceof EventDTO) {
        EventDTO event = (EventDTO) serializableExtra;
        Log.i(TAG, "received event " + event.getType().name() + ": " + event.getContent().toString());
        if (isConnected()) {
          boolean result = restClient.send(event.getContent(), event.getType());
        }
      } else {
        Log.w(TAG, serializableExtra.getClass().getSimpleName() + " isn't instance of " + EventDTO.class.getSimpleName());
      }
    }

  }
}
