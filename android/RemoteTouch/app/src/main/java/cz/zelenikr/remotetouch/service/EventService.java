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

import java.net.MalformedURLException;
import java.net.URL;

import cz.zelenikr.remotetouch.MainActivity;
import cz.zelenikr.remotetouch.R;
import cz.zelenikr.remotetouch.data.EEventType;
import cz.zelenikr.remotetouch.helper.ConnectionHelper;
import cz.zelenikr.remotetouch.network.SimpleRestClient;

import static cz.zelenikr.remotetouch.helper.NotificationHelper.APP_ICON_ID;

/**
 * @author Roman Zelenik
 */
public class EventService extends Service {

  private static final int ONGOING_NOTIFICATION_ID = 1;
  private static final String TAG = EventService.class.getSimpleName();
  private Looper serviceLooper;
  private EventHandler eventHandler;
  private SimpleRestClient restClient;


  @Override
  public void onCreate() {
    super.onCreate();

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

    // Start this service in the foreground and show persistent notification
    showNotification();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && intent.getStringExtra("event") != null) {
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

  private String loadClientToken() {
    // TODO: get from authentication
    return "1";
  }

  private String loadRestUrl() {
    // TODO: load from Preferences
//    return "http://10.0.0.46:4000";
    return "http://remote-touch.azurewebsites.net";
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

  private boolean isConnected(){
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
      final String packageName = intent.getStringExtra("packageName");
      final String eventName = intent.getStringExtra("event");
      EEventType eventType = EEventType.valueOf(eventName);
      Log.i(TAG, "received event " + eventName + " from " + packageName);
      if (isConnected()) {
        boolean result = restClient.send(packageName, eventType);
      }
    }

  }
}
