package cz.zelenikr.remotetouch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

import cz.zelenikr.remotetouch.data.EEventType;
import cz.zelenikr.remotetouch.data.dto.EventDTO;
import cz.zelenikr.remotetouch.data.dto.NotificationEventContent;
import cz.zelenikr.remotetouch.data.dto.SmsEventContent;
import cz.zelenikr.remotetouch.helper.ApiHelper;
import cz.zelenikr.remotetouch.service.EventService;

/**
 * @author Roman Zelenik
 */
public class SmsReceiver extends BroadcastReceiver {

  // public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

  private static final String TAG = SmsReceiver.class.getSimpleName();
  private static final EEventType EVENT_TYPE = EEventType.SMS;


  public SmsReceiver() {
    super();
    Log.i(TAG, "Was initialized");
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "New SMS");
    if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
      String smsSender = "";
      StringBuilder smsBody = new StringBuilder();
      long smsWhen;
      SmsMessage[] messages = null;

      // On KITKAT and newer
      if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.KITKAT)) {
        messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
      }

      // Older then KITKAT
      else {
        Bundle smsBundle = intent.getExtras();
        if (smsBundle != null) {
          Object[] pdus = (Object[]) smsBundle.get("pdus");
          if (pdus == null) {
            // Display some error to the user
            Log.e(TAG, "SmsBundle had no pdus key");
            return;
          }
          messages = new SmsMessage[pdus.length];
          // Get messages
          for (int i = 0; i < messages.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
          }
        }
      }

      // Get info from messages
      if (messages != null && messages.length > 0) {
        // Concat sms content
        for (SmsMessage smsMessage : messages) {
          smsBody.append(smsMessage.getMessageBody());
        }
        smsSender = messages[0].getDisplayOriginatingAddress();
        smsWhen = messages[0].getTimestampMillis();
      }
      // There are none sms
      else {
        return;
      }

      Log.i(TAG, "From: " + smsSender);
      Log.i(TAG, "At: " + new Date(smsWhen).toString());
      Log.i(TAG, "Text: " + smsBody);

      //Toast.makeText(context, "SMS from " + smsSender, Toast.LENGTH_LONG).show();

      // Send SMS test
//      SmsManager.getDefault().sendTextMessage(smsSender, null, smsBody.toUpperCase(),null,null);

      sendEvent(context, new SmsEventContent(smsSender, smsBody.toString(), smsWhen));

    }
  }

  private void sendEvent(Context context, SmsEventContent content) {
    Intent intent = new Intent(context, EventService.class);
    intent.putExtra(EventService.INTENT_EXTRA_EVENT, true);
    intent.putExtra(
        EventService.INTENT_EXTRA_NAME,
        new EventDTO(EVENT_TYPE, content)
    );

    context.startService(intent);
  }

}
