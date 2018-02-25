package cz.zelenikr.remotetouch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Roman Zelenik
 */
public class PhoneStateReceiver extends BroadcastReceiver {

  private static final String TAG = PhoneStateReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
      String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
      if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
        Log.i(TAG, "Ringing State Number is -" + incomingNumber);
        Toast.makeText(context, "Ringing State Number is -" + incomingNumber, Toast.LENGTH_SHORT).show();
      }
      if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))) {
        Log.i(TAG, "Received State");
        Toast.makeText(context, "Received State", Toast.LENGTH_SHORT).show();
      }
      if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
        Log.i(TAG, "Idle State");
        Toast.makeText(context, "Idle State", Toast.LENGTH_SHORT).show();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
