package cz.zelenikr.remotetouch;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Roman on 20.10.2017.
 */

public class PersistentService {

  private static final int NOTIFICATION_ID = 1;

  private NotificationManager notificationManager;
  private NotificationCompat.Builder  notificationBuilder;
  private Context context;

  public PersistentService(Context context) {
    this.context = context;

    setupNotification();
  }

  private void setupNotification(){
    if (notificationManager == null) {
      notificationManager = (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);
    }

    notificationBuilder = new NotificationCompat.Builder(getContext());
  }

  private Context getContext(){return context;}

  public void showNotification(){
//    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class));

    notificationBuilder
        .setOngoing(true)
//        .setContentIntent(contentIntent)
        .setWhen(System.currentTimeMillis())
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//        .addAction()
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Notification title")
        .setContentText("Notification text");

    if(notificationManager != null){
      notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
  }
}
