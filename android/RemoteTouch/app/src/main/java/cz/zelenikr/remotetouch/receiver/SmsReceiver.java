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

import cz.zelenikr.remotetouch.data.EEventType;
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
      String smsBody = "";
      if (ApiHelper.checkCurrentApiLevel(Build.VERSION_CODES.KITKAT)) {
        for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
          smsSender = smsMessage.getDisplayOriginatingAddress();
          smsBody += smsMessage.getMessageBody();
        }
      } else {
        Bundle smsBundle = intent.getExtras();
        if (smsBundle != null) {
          Object[] pdus = (Object[]) smsBundle.get("pdus");
          if (pdus == null) {
            // Display some error to the user
            Log.e(TAG, "SmsBundle had no pdus key");
            return;
          }
          SmsMessage[] messages = new SmsMessage[pdus.length];
          for (int i = 0; i < messages.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            smsBody += messages[i].getMessageBody();
          }
          smsSender = messages[0].getOriginatingAddress();
        }
      }

      Log.i(TAG, "From: " + smsSender);
      Log.i(TAG, "Text: " + smsBody);

      Toast.makeText(context, "SMS from " + smsSender, Toast.LENGTH_LONG).show();

      // Send SMS test
//      SmsManager.getDefault().sendTextMessage(smsSender, null, smsBody.toUpperCase(),null,null);

      sendEvent(context, smsSender + ": " + smsBody);

      /*if (smsSender.equals(serviceProviderNumber) && smsBody.startsWith(serviceProviderSmsCondition)) {
        if (listener != null) {
          listener.onTextReceived(smsBody);
        }
      }*/
    }
  }

  private void sendEvent(Context context, String content) {
    Intent intent = new Intent(context, EventService.class);
    intent.putExtra("packageName", content);
    intent.putExtra("event", EVENT_TYPE.name());

    context.startService(intent);
  }

}
